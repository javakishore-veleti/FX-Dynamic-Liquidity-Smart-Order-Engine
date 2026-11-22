package com.jk.apps.banking.fx.sor.topology.grpc;

import com.jk.apps.banking.fx.sor.topology.api.dto.BlastRadiusRequest;
import com.jk.apps.banking.fx.sor.topology.api.dto.BlastRadiusResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.IpcEdgeDto;
import com.jk.apps.banking.fx.sor.topology.api.dto.IpcGraphResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.NetworkEdgeDto;
import com.jk.apps.banking.fx.sor.topology.api.dto.NetworkGraphResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.TopologyDiffResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.TraceOutlierDto;
import com.jk.apps.banking.fx.sor.topology.api.dto.TraceOutlierResponse;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyEdgeMutation;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyEdgeMutationRepository;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.DiffRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.IpcGraphRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.MergedGraphRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.MergedEdgeList;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.MutationList;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.MutationsRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkAtRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkEdgeList;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkGraphRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkPageRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkPageResponse;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.RootCandidate;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.RootSourceList;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.RootSourceRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.TopologyReadGrpc;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.TraceOutliersRequest;
import com.jk.apps.banking.fx.sor.topology.service.LayerMerge;
import com.jk.apps.banking.fx.sor.topology.service.RootSource;
import com.jk.apps.banking.fx.sor.topology.service.RootSourceQuery;
import com.jk.apps.banking.fx.sor.topology.service.TopologyQueryService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Read-only gRPC API for the topology service, generated contract fx.topology.v1.TopologyRead.
 * Enabled by fx.features.grpc-enabled (env FX_GRPC_ENABLED). Each operation returns the same
 * data as the matching REST endpoint. Writes are not exposed here.
 */
@GrpcService
@ConditionalOnProperty(name = "fx.features.grpc-enabled", havingValue = "true")
public class GrpcReadServer extends TopologyReadGrpc.TopologyReadImplBase {

    private final TopologyQueryService query;
    private final RootSourceQuery rootSource;
    private final TopologyEdgeMutationRepository mutations;

    public GrpcReadServer(TopologyQueryService query, RootSourceQuery rootSource,
                          TopologyEdgeMutationRepository mutations) {
        this.query = query;
        this.rootSource = rootSource;
        this.mutations = mutations;
    }

    @Override
    public void networkGraph(NetworkGraphRequest r, StreamObserver<com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkGraphResponse> o) {
        run(o, () -> {
            NetworkGraphResponse out = query.networkGraph(blankToNull(r.getService()), depthOr(r.getDepth(), 3), r.getFreshMinutes());
            var b = com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkGraphResponse.newBuilder()
                    .setService(nz(out.service())).setDepth(out.depth());
            out.edges().forEach(e -> b.addEdges(network(e)));
            return b.build();
        });
    }

    @Override
    public void networkAt(NetworkAtRequest r, StreamObserver<NetworkEdgeList> o) {
        run(o, () -> {
            var b = NetworkEdgeList.newBuilder();
            query.networkAt(r.getBucket()).forEach(e -> b.addEdges(network(e)));
            return b.build();
        });
    }

    @Override
    public void networkPage(NetworkPageRequest r, StreamObserver<NetworkPageResponse> o) {
        run(o, () -> {
            List<NetworkEdgeDto> all = query.networkGraph(blankToNull(r.getService()), depthOr(r.getDepth(), 3), 0).edges();
            int size = Math.max(1, Math.min(r.getSize() == 0 ? 50 : r.getSize(), 500));
            int page = Math.max(0, r.getPage());
            int from = Math.min(all.size(), page * size);
            int to = Math.min(all.size(), from + size);
            var b = NetworkPageResponse.newBuilder().setPage(page).setSize(size).setTotal(all.size());
            all.subList(from, to).forEach(e -> b.addEdges(network(e)));
            return b.build();
        });
    }

    @Override
    public void mergedGraph(MergedGraphRequest r, StreamObserver<MergedEdgeList> o) {
        run(o, () -> {
            var b = MergedEdgeList.newBuilder();
            for (LayerMerge.Merged m : query.mergedGraph(r.getFreshMinutes())) {
                b.addEdges(com.jk.apps.banking.fx.sor.topology.grpc.v1.MergedEdge.newBuilder()
                        .setSrc(m.src()).setDst(m.dst()).addAllLayers(m.layers()).setAgreement(m.agreement()));
            }
            return b.build();
        });
    }

    @Override
    public void ipcGraph(IpcGraphRequest r, StreamObserver<com.jk.apps.banking.fx.sor.topology.grpc.v1.IpcGraphResponse> o) {
        run(o, () -> {
            IpcGraphResponse out = query.ipcGraph(blankToNull(r.getCcyPair()), blankToNull(r.getWindow()));
            var b = com.jk.apps.banking.fx.sor.topology.grpc.v1.IpcGraphResponse.newBuilder()
                    .setCcyPair(nz(out.ccyPair())).setWindow(nz(out.window()));
            out.edges().forEach(e -> b.addEdges(ipc(e)));
            return b.build();
        });
    }

    @Override
    public void traceOutliers(TraceOutliersRequest r, StreamObserver<com.jk.apps.banking.fx.sor.topology.grpc.v1.TraceOutliersResponse> o) {
        run(o, () -> {
            TraceOutlierResponse out = query.outliers(r.getMinLatencyMs(), blankToNull(r.getServiceName()));
            var b = com.jk.apps.banking.fx.sor.topology.grpc.v1.TraceOutliersResponse.newBuilder()
                    .setMinLatencyMs(out.minLatencyMs()).setServiceName(nz(out.serviceName()));
            for (TraceOutlierDto t : out.outliers()) {
                b.addOutliers(com.jk.apps.banking.fx.sor.topology.grpc.v1.TraceOutlier.newBuilder()
                        .setId(t.id().toString()).setServiceName(nz(t.serviceName()))
                        .setClientOrderId(nz(t.clientOrderId())).setKnockoutReason(nz(t.knockoutReason()))
                        .setWinningLpId(nz(t.winningLpId())).setLatencyMs(t.latencyMs()));
            }
            return b.build();
        });
    }

    @Override
    public void rootSource(RootSourceRequest r, StreamObserver<RootSourceList> o) {
        run(o, () -> {
            var b = RootSourceList.newBuilder();
            for (RootSource.Candidate c : rootSource.rank(r.getThresholdMs())) {
                b.addCandidates(RootCandidate.newBuilder().setService(c.service()).setDegradedCallers(c.degradedCallers()));
            }
            return b.build();
        });
    }

    @Override
    public void mutations(MutationsRequest r, StreamObserver<MutationList> o) {
        run(o, () -> {
            var b = MutationList.newBuilder();
            for (TopologyEdgeMutation m : mutations.findBySrcServiceAndDstServiceOrderByChangedAtAsc(r.getSrc(), r.getDst())) {
                b.addMutations(com.jk.apps.banking.fx.sor.topology.grpc.v1.Mutation.newBuilder()
                        .setId(m.getId().toString()).setSrcService(m.getSrcService()).setDstService(m.getDstService())
                        .setProperty(m.getProperty()).setOldValue(nz(m.getOldValue()))
                        .setNewValue(m.getNewValue()).setChangedAt(m.getChangedAt().toString()));
            }
            return b.build();
        });
    }

    @Override
    public void diff(DiffRequest r, StreamObserver<com.jk.apps.banking.fx.sor.topology.grpc.v1.DiffResponse> o) {
        run(o, () -> {
            TopologyDiffResponse out = query.diff(Instant.parse(r.getFrom()), Instant.parse(r.getTo()));
            var b = com.jk.apps.banking.fx.sor.topology.grpc.v1.DiffResponse.newBuilder()
                    .setFrom(out.from().toString()).setTo(out.to().toString())
                    .setFromEdgeCount(nullInt(out.fromEdgeCount())).setToEdgeCount(nullInt(out.toEdgeCount()))
                    .setEdgeDelta(nullInt(out.edgeDelta()));
            if (out.nodesAdded() != null) {
                b.addAllNodesAdded(out.nodesAdded());
            }
            if (out.nodesRemoved() != null) {
                b.addAllNodesRemoved(out.nodesRemoved());
            }
            return b.build();
        });
    }

    @Override
    public void blastRadius(com.jk.apps.banking.fx.sor.topology.grpc.v1.BlastRadiusRequest r, StreamObserver<com.jk.apps.banking.fx.sor.topology.grpc.v1.BlastRadiusResponse> o) {
        run(o, () -> {
            BlastRadiusResponse out = query.blastRadius(new BlastRadiusRequest(
                    blankToNull(r.getServiceName()), blankToNull(r.getCcyPair()), depthOr(r.getDepth(), 3)));
            var b = com.jk.apps.banking.fx.sor.topology.grpc.v1.BlastRadiusResponse.newBuilder()
                    .setServiceName(nz(out.serviceName())).setCcyPair(nz(out.ccyPair())).setDepth(out.depth());
            if (out.impactedServices() != null) {
                b.addAllImpactedServices(out.impactedServices());
            }
            if (out.networkEdges() != null) {
                out.networkEdges().forEach(e -> b.addNetworkEdges(network(e)));
            }
            if (out.ipcEdges() != null) {
                out.ipcEdges().forEach(e -> b.addIpcEdges(ipc(e)));
            }
            return b.build();
        });
    }

    private static com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkEdge network(NetworkEdgeDto e) {
        return com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkEdge.newBuilder()
                .setId(e.id() == null ? "" : e.id().toString())
                .setSrcService(nz(e.srcService())).setDstService(nz(e.dstService()))
                .setTimeBucket(nz(e.timeBucket())).setPodNamespace(nz(e.podNamespace()))
                .setAzZone(nz(e.azZone())).setEgressLpGateway(nz(e.egressLpGateway()))
                .setSocketRetransmits(e.socketRetransmits()).setFlowCount(e.flowCount())
                .build();
    }

    private static com.jk.apps.banking.fx.sor.topology.grpc.v1.IpcEdge ipc(IpcEdgeDto e) {
        return com.jk.apps.banking.fx.sor.topology.grpc.v1.IpcEdge.newBuilder()
                .setId(e.id() == null ? "" : e.id().toString())
                .setSrcService(nz(e.srcService())).setDstService(nz(e.dstService()))
                .setCcyPair(nz(e.ccyPair())).setWindow(nz(e.window()))
                .setAvailabilityTier(nz(e.availabilityTier())).setDeskOwner(nz(e.deskOwner()))
                .setStpFillPct(e.stpFillPct() == null ? 0 : e.stpFillPct())
                .setCircuitState(nz(e.circuitState()))
                .build();
    }

    private static <T> void run(StreamObserver<T> observer, java.util.function.Supplier<T> call) {
        try {
            T reply = call.get();
            observer.onNext(reply);
            observer.onCompleted();
        } catch (IllegalArgumentException ex) {
            observer.onError(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            observer.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    private static int depthOr(int depth, int fallback) {
        return depth <= 0 ? fallback : depth;
    }

    private static int nullInt(Integer v) {
        return v == null ? 0 : v;
    }
}
