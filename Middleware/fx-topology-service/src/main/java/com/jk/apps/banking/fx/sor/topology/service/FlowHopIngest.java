package com.jk.apps.banking.fx.sor.topology.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyNetworkEdge;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyNetworkEdgeRepository;
import com.jk.apps.banking.fx.sor.topology.resolve.IntermediaryResolver;
import com.jk.apps.banking.fx.sor.topology.resolve.IntermediaryResolver.DirectEdge;
import com.jk.apps.banking.fx.sor.topology.resolve.IntermediaryResolver.FlowHop;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Premises P2, P5, P7 (Netflix TechBlog): stream ingest with backpressure, then resolution and write.
 *
 * accept() parses one flow-hop message and queues it. When the bounded queue is full, accept() blocks
 * the consumer thread (backpressure, P7). drainOnce() takes everything queued, resolves intermediaries
 * (P4), and writes one network edge per direct pair into the 5-minute time bucket (P10).
 */
@Component
public class FlowHopIngest {

    static final Duration BUCKET = Duration.ofMinutes(5);
    static final DateTimeFormatter BUCKET_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").withZone(ZoneOffset.UTC);

    private final ObjectMapper mapper;
    private final TopologyNetworkEdgeRepository repository;
    private final IntermediaryResolver resolver;
    private final IngestQueue<FlowHop> queue;

    public FlowHopIngest(
            ObjectMapper mapper,
            TopologyNetworkEdgeRepository repository,
            @Value("${fx.topology.intermediaries:}") List<String> intermediaries) {
        this(mapper, repository, Set.copyOf(intermediaries), 10_000);
    }

    FlowHopIngest(ObjectMapper mapper, TopologyNetworkEdgeRepository repository, Set<String> intermediaries, int capacity) {
        this.mapper = mapper;
        this.repository = repository;
        this.resolver = new IntermediaryResolver(intermediaries);
        this.queue = new IngestQueue<>(capacity);
    }

    /** Parses one message and queues it. Blocks while the queue is full. */
    public void accept(String json) throws InterruptedException {
        FlowHop hop = parse(json);
        while (!queue.offer(hop)) {
            Thread.sleep(10);
        }
    }

    FlowHop parse(String json) {
        try {
            JsonNode n = mapper.readTree(json);
            return new FlowHop(n.get("src").asText(), n.get("dst").asText(), n.get("flows").asLong());
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid flow-hop message: " + json, ex);
        }
    }

    public long refusedCount() {
        return queue.refusedCount();
    }

    /** Resolves and writes everything queued so far. Returns the number of edges written. */
    public int drainOnce(Instant now) {
        List<FlowHop> batch = new ArrayList<>();
        FlowHop hop;
        while ((hop = queue.poll()) != null) {
            batch.add(hop);
        }
        if (batch.isEmpty()) {
            return 0;
        }
        String bucket = bucketOf(now);
        List<DirectEdge> edges = resolver.resolve(batch);
        for (DirectEdge e : edges) {
            TopologyNetworkEdge row = new TopologyNetworkEdge();
            row.setId(UUID.randomUUID());
            row.setSrcService(e.src());
            row.setDstService(e.dst());
            row.setTimeBucket(bucket);
            row.setPodNamespace("fx-sor");
            row.setAzZone("unknown");
            row.setEgressLpGateway(null);
            row.setSocketRetransmits(0);
            row.setFlowCount(e.flows());
            row.setObservedAt(now);
            repository.save(row);
        }
        return edges.size();
    }

    /** Start of the 5-minute bucket that contains the instant, as yyyy-MM-ddTHH:mm (UTC). */
    static String bucketOf(Instant t) {
        long secs = t.getEpochSecond();
        long start = secs - (secs % BUCKET.getSeconds());
        return BUCKET_FMT.format(Instant.ofEpochSecond(start));
    }
}
