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
import org.springframework.beans.factory.annotation.Autowired;
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
    private final EdgeEnrich enrich;
    private final Freshness freshness;
    private final IngestPartition partition;
    private final java.util.concurrent.atomic.AtomicLong notOwned = new java.util.concurrent.atomic.AtomicLong();
    private final java.util.concurrent.atomic.AtomicLong stale = new java.util.concurrent.atomic.AtomicLong();

    @Autowired
    public FlowHopIngest(
            ObjectMapper mapper,
            TopologyNetworkEdgeRepository repository,
            EdgeEnrich enrich,
            @Value("${fx.topology.intermediaries:}") List<String> intermediaries,
            @Value("${fx.topology.freshness-minutes:10}") long freshnessMinutes,
            @Value("${fx.topology.instance-id:local-1}") String instanceId,
            @Value("${fx.topology.members:}") List<String> members) {
        this(mapper, repository, enrich, Set.copyOf(intermediaries), 10_000, freshnessMinutes,
                new IngestPartition(instanceId, members));
    }

    FlowHopIngest(ObjectMapper mapper, TopologyNetworkEdgeRepository repository, EdgeEnrich enrich,
                  Set<String> intermediaries, int capacity) {
        this(mapper, repository, enrich, intermediaries, capacity, 10);
    }

    FlowHopIngest(ObjectMapper mapper, TopologyNetworkEdgeRepository repository, EdgeEnrich enrich,
                  Set<String> intermediaries, int capacity, long freshnessMinutes) {
        this(mapper, repository, enrich, intermediaries, capacity, freshnessMinutes,
                new IngestPartition("local-1", List.of()));
    }

    FlowHopIngest(ObjectMapper mapper, TopologyNetworkEdgeRepository repository, EdgeEnrich enrich,
                  Set<String> intermediaries, int capacity, long freshnessMinutes, IngestPartition partition) {
        this.partition = partition;
        this.freshness = new Freshness(Duration.ofMinutes(freshnessMinutes));
        this.mapper = mapper;
        this.repository = repository;
        this.enrich = enrich;
        this.resolver = new IntermediaryResolver(intermediaries);
        this.queue = new IngestQueue<>(capacity);
    }

    /** Parses one message and queues it. Blocks while the queue is full. */
    public void accept(String json) throws InterruptedException {
        Instant observed = observedAt(json);
        if (observed != null && !freshness.isFresh(observed, Instant.now())) {
            stale.incrementAndGet();
            return;
        }
        FlowHop hop = parse(json);
        if (!partition.owns(hop.src())) {
            notOwned.incrementAndGet();
            return;
        }
        while (!queue.offer(hop)) {
            Thread.sleep(10);
        }
    }

    /** Hops older than the freshness window are dropped at ingest and counted, never silently. */
    /** Hops whose key belongs to another instance on the hash ring (P6). */
    public long notOwnedCount() {
        return notOwned.get();
    }

    public long staleCount() {
        return stale.get();
    }

    private Instant observedAt(String json) {
        try {
            JsonNode n = mapper.readTree(json).get("observedAt");
            return n == null ? null : Instant.parse(n.asText());
        } catch (Exception ex) {
            return null;
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
            EdgeEnrich.Enriched meta = enrich.enrich(e.src(), e.dst());
            row.setSrcTier(meta.srcTier());
            row.setDstTier(meta.dstTier());
            row.setDomain(meta.domain());
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
