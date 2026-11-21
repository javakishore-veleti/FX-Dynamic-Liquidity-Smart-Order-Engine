package com.jk.apps.banking.fx.sor.topology.service;

import com.jk.apps.banking.fx.sor.topology.api.dto.BlastRadiusRequest;
import com.jk.apps.banking.fx.sor.topology.api.dto.BlastRadiusResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.IpcEdgeDto;
import com.jk.apps.banking.fx.sor.topology.api.dto.IpcGraphResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.NetworkEdgeDto;
import com.jk.apps.banking.fx.sor.topology.api.dto.NetworkGraphResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.SnapshotSummaryDto;
import com.jk.apps.banking.fx.sor.topology.api.dto.TopologyDiffResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.TraceOutlierDto;
import com.jk.apps.banking.fx.sor.topology.api.dto.TraceOutlierResponse;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyIpcEdge;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyNetworkEdge;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologySnapshot;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyTraceOutlier;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyIpcEdgeRepository;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyNetworkEdgeRepository;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologySnapshotRepository;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyTraceOutlierRepository;
import com.jk.apps.banking.fx.sor.topology.eventing.DomainEventPublisher;
import com.jk.apps.banking.fx.sor.topology.metrics.ServiceMetrics;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class TopologyQueryService {

    private static final String TOPIC_BLAST_RADIUS = "fx.topology.blast-radius-alert.v1";
    private static final double DEGRADED_P99_MS = 10.0;

    private final TopologyNetworkEdgeRepository networkEdgeRepository;
    private final TopologyIpcEdgeRepository ipcEdgeRepository;
    private final TopologyTraceOutlierRepository traceOutlierRepository;
    private final TopologySnapshotRepository snapshotRepository;
    private final ServiceMetrics metrics;
    private final DomainEventPublisher domainEvents;

    public TopologyQueryService(TopologyNetworkEdgeRepository networkEdgeRepository,
                                TopologyIpcEdgeRepository ipcEdgeRepository,
                                TopologyTraceOutlierRepository traceOutlierRepository,
                                TopologySnapshotRepository snapshotRepository,
                                ServiceMetrics metrics,
                                DomainEventPublisher domainEvents) {
        this.networkEdgeRepository = networkEdgeRepository;
        this.ipcEdgeRepository = ipcEdgeRepository;
        this.traceOutlierRepository = traceOutlierRepository;
        this.snapshotRepository = snapshotRepository;
        this.metrics = metrics;
        this.domainEvents = domainEvents;
    }

    public NetworkGraphResponse networkGraph(String service, int depth) {
        Timer.Sample sample = Timer.start();
        try {
            List<TopologyNetworkEdge> all = networkEdgeRepository.findAll();
            List<TopologyNetworkEdge> scoped = expandNetwork(all, service, depth);
            metrics.networkGraphQueries().increment();
            return new NetworkGraphResponse(service, depth, scoped.stream().map(this::toNetworkDto).toList());
        } finally {
            sample.stop(metrics.queryLatency());
        }
    }

    public IpcGraphResponse ipcGraph(String ccyPair, String window) {
        Timer.Sample sample = Timer.start();
        try {
            List<TopologyIpcEdge> edges = ipcEdgeRepository.findByCcyPairAndWindow(ccyPair, window);
            metrics.ipcGraphQueries().increment();
            return new IpcGraphResponse(ccyPair, window, edges.stream().map(this::toIpcDto).toList());
        } finally {
            sample.stop(metrics.queryLatency());
        }
    }

    public TraceOutlierResponse outliers(double minLatencyMs, String serviceName) {
        Timer.Sample sample = Timer.start();
        try {
            List<TopologyTraceOutlier> outliers = traceOutlierRepository.findOutliers(minLatencyMs, serviceName);
            metrics.outlierTraceQueries().increment();
            return new TraceOutlierResponse(
                    minLatencyMs,
                    serviceName,
                    outliers.stream().map(this::toTraceDto).toList());
        } finally {
            sample.stop(metrics.queryLatency());
        }
    }

    public BlastRadiusResponse blastRadius(BlastRadiusRequest request) {
        Timer.Sample sample = Timer.start();
        long started = System.currentTimeMillis();
        try {
            String serviceName = Objects.requireNonNull(request.serviceName(), "serviceName is required");
            int depth = request.depth() == null ? 3 : Math.max(1, request.depth());

            List<TopologyNetworkEdge> network = expandNetwork(networkEdgeRepository.findAll(), serviceName, depth);
            List<TopologyIpcEdge> ipc = ipcEdgeRepository.findByCcyPairAndWindow(request.ccyPair(), null).stream()
                    .filter(e -> e.getSrcService().equals(serviceName)
                            || e.getDstService().equals(serviceName)
                            || network.stream().anyMatch(n ->
                            n.getSrcService().equals(e.getSrcService()) || n.getDstService().equals(e.getDstService())))
                    .toList();

            Set<String> impacted = new LinkedHashSet<>();
            network.forEach(e -> {
                impacted.add(e.getSrcService());
                impacted.add(e.getDstService());
            });
            ipc.forEach(e -> {
                impacted.add(e.getSrcService());
                impacted.add(e.getDstService());
            });
            impacted.remove(serviceName);

            metrics.blastRadiusQueries().increment();
            maybeEmitBlastRadiusFromIpc(serviceName, ipc, impacted);
            return new BlastRadiusResponse(
                    serviceName,
                    request.ccyPair(),
                    depth,
                    List.copyOf(impacted),
                    network.stream().map(this::toNetworkDto).toList(),
                    ipc.stream().map(this::toIpcDto).toList(),
                    System.currentTimeMillis() - started);
        } finally {
            sample.stop(metrics.queryLatency());
        }
    }

    public TopologyDiffResponse diff(Instant from, Instant to) {
        Timer.Sample sample = Timer.start();
        try {
            List<TopologySnapshot> inRange = snapshotRepository.findBetween(from, to);
            TopologySnapshot fromSnap = nearestAtOrBefore(from);
            TopologySnapshot toSnap = nearestAtOrBefore(to);

            Set<String> fromNodes = parseNodes(fromSnap == null ? null : fromSnap.getNodeSummary());
            Set<String> toNodes = parseNodes(toSnap == null ? null : toSnap.getNodeSummary());

            List<String> added = toNodes.stream().filter(n -> !fromNodes.contains(n)).sorted().toList();
            List<String> removed = fromNodes.stream().filter(n -> !toNodes.contains(n)).sorted().toList();

            Integer fromEdges = fromSnap == null ? null : fromSnap.getEdgeCount();
            Integer toEdges = toSnap == null ? null : toSnap.getEdgeCount();
            Integer delta = (fromEdges == null || toEdges == null) ? null : toEdges - fromEdges;

            metrics.topologyDiffQueries().increment();
            if (!added.isEmpty() || (delta != null && delta > 0)) {
                String source = fromSnap == null ? "unknown" : fromSnap.getLayer();
                String target = added.isEmpty() ? "edge-delta" : added.getFirst();
                emitBlastRadiusAlert(
                        source,
                        target,
                        fromSnap == null ? "EBPF" : fromSnap.getLayer(),
                        delta == null ? 0.0 : delta.doubleValue(),
                        List.copyOf(added.isEmpty() ? Set.of("DESK_FX_G10") : new LinkedHashSet<>(added)));
            }
            return new TopologyDiffResponse(
                    from,
                    to,
                    fromEdges,
                    toEdges,
                    delta,
                    added,
                    removed,
                    inRange.stream().map(this::toSnapshotDto).toList());
        } finally {
            sample.stop(metrics.queryLatency());
        }
    }

    private void maybeEmitBlastRadiusFromIpc(
            String serviceName, List<TopologyIpcEdge> ipc, Set<String> impacted) {
        for (TopologyIpcEdge edge : ipc) {
            Double p99 = edge.getP99Ms();
            if (p99 == null || p99 < DEGRADED_P99_MS) {
                continue;
            }
            List<String> desks = edge.getDeskOwner() == null || edge.getDeskOwner().isBlank()
                    ? List.of("DESK_FX_G10")
                    : List.of(edge.getDeskOwner());
            emitBlastRadiusAlert(
                    edge.getSrcService() == null ? serviceName : edge.getSrcService(),
                    edge.getDstService() == null ? "unknown" : edge.getDstService(),
                    "IPC",
                    p99,
                    desks.isEmpty() ? List.copyOf(impacted) : desks);
            break;
        }
    }

    private void emitBlastRadiusAlert(
            String sourceService,
            String targetService,
            String layer,
            double p99DeltaMs,
            List<String> affectedDeskOwners) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("sourceService", sourceService);
        payload.put("targetService", targetService);
        payload.put("layer", layer);
        payload.put("p99DeltaMs", p99DeltaMs);
        payload.put("affectedDeskOwners", affectedDeskOwners);
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_BLAST_RADIUS, payload);
    }

    private TopologySnapshot nearestAtOrBefore(Instant ts) {
        List<TopologySnapshot> matches = snapshotRepository.findBySnapshotTsLessThanEqualOrderBySnapshotTsDesc(ts);
        return matches.isEmpty() ? null : matches.getFirst();
    }

    private List<TopologyNetworkEdge> expandNetwork(List<TopologyNetworkEdge> all, String service, int depth) {
        if (service == null || service.isBlank()) {
            return all;
        }
        Set<String> frontier = new HashSet<>();
        frontier.add(service);
        Set<String> visited = new HashSet<>(frontier);
        List<TopologyNetworkEdge> result = new ArrayList<>();
        Set<TopologyNetworkEdge> selected = new HashSet<>();

        ArrayDeque<String> queue = new ArrayDeque<>(frontier);
        for (int level = 0; level < depth && !queue.isEmpty(); level++) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                String current = queue.poll();
                for (TopologyNetworkEdge edge : all) {
                    if (edge.getSrcService().equals(current) || edge.getDstService().equals(current)) {
                        if (selected.add(edge)) {
                            result.add(edge);
                        }
                        String next = edge.getSrcService().equals(current) ? edge.getDstService() : edge.getSrcService();
                        if (visited.add(next)) {
                            queue.add(next);
                        }
                    }
                }
            }
        }
        return result;
    }

    private Set<String> parseNodes(String summary) {
        if (summary == null || summary.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(summary.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private NetworkEdgeDto toNetworkDto(TopologyNetworkEdge e) {
        return new NetworkEdgeDto(
                e.getId(),
                e.getSrcService(),
                e.getDstService(),
                e.getTimeBucket(),
                e.getPodNamespace(),
                e.getAzZone(),
                e.getEgressLpGateway(),
                e.getSocketRetransmits(),
                e.getFlowCount(),
                e.getObservedAt());
    }

    private IpcEdgeDto toIpcDto(TopologyIpcEdge e) {
        return new IpcEdgeDto(
                e.getId(),
                e.getSrcService(),
                e.getDstService(),
                e.getCcyPair(),
                e.getWindowLabel(),
                e.getAvailabilityTier(),
                e.getDeskOwner(),
                e.getP99Ms(),
                e.getStpFillPct(),
                e.getCircuitState(),
                e.getObservedAt());
    }

    private TraceOutlierDto toTraceDto(TopologyTraceOutlier t) {
        return new TraceOutlierDto(
                t.getId(),
                t.getServiceName(),
                t.getClientOrderId(),
                t.getDroolsRuleVersion(),
                t.getKnockoutReason(),
                t.getWinningLpId(),
                t.getLatencyMs(),
                t.getObservedAt());
    }

    private SnapshotSummaryDto toSnapshotDto(TopologySnapshot s) {
        return new SnapshotSummaryDto(
                s.getId(),
                s.getSnapshotTs(),
                s.getLayer(),
                s.getEdgeCount(),
                s.getNodeSummary());
    }
}
