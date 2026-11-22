package com.jk.apps.banking.fx.sor.topology.grpc;

import com.jk.apps.banking.fx.sor.topology.api.dto.BlastRadiusRequest;
import com.jk.apps.banking.fx.sor.topology.service.RootSourceQuery;
import com.jk.apps.banking.fx.sor.topology.service.TopologyQueryService;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyEdgeMutationRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.ServerCalls;
import java.io.IOException;
import java.time.Instant;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Component;

/**
 * gRPC server for the read-only topology API. Enabled by fx.features.grpc-enabled. Each read
 * operation takes a JSON request and returns the same JSON the REST endpoint returns. Writes are
 * not exposed here: ingest stays on Kafka and REST.
 */
@GrpcService
@ConditionalOnProperty(name = "fx.features.grpc-enabled", havingValue = "true")
public class GrpcReadServer implements BindableService {

    private final ObjectMapper mapper;
    private final TopologyQueryService query;
    private final RootSourceQuery rootSource;
    private final TopologyEdgeMutationRepository mutations;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GrpcReadServer.class);

    public GrpcReadServer(ObjectMapper mapper,
                          TopologyQueryService query, RootSourceQuery rootSource,
                          TopologyEdgeMutationRepository mutations) {
        this.mapper = mapper;
        LOG.info("GrpcReadServer bean created: read-only gRPC service {}", ReadMethods.SERVICE);
        this.query = query;
        this.rootSource = rootSource;
        this.mutations = mutations;
    }

    @Override
    public ServerServiceDefinition bindService() {
        ServerServiceDefinition.Builder def = ServerServiceDefinition.builder(ReadMethods.SERVICE);
        bind(def, ReadMethods.NETWORK, this::network);
        bind(def, ReadMethods.NETWORK_AT, this::networkAt);
        bind(def, ReadMethods.NETWORK_PAGE, this::networkPage);
        bind(def, ReadMethods.MERGED, this::merged);
        bind(def, ReadMethods.IPC, this::ipc);
        bind(def, ReadMethods.OUTLIERS, this::outliers);
        bind(def, ReadMethods.ROOT_SOURCE, this::rootSourceRank);
        bind(def, ReadMethods.MUTATIONS, this::mutationHistory);
        bind(def, ReadMethods.DIFF, this::diff);
        bind(def, ReadMethods.BLAST_RADIUS, this::blastRadius);
        return def.build();
    }

    private void bind(ServerServiceDefinition.Builder def, String name, Function<String, String> handler) {
        def.addMethod(ReadMethods.method(name),
                ServerCalls.asyncUnaryCall((request, observer) -> {
                    try {
                        observer.onNext(handler.apply(request));
                        observer.onCompleted();
                    } catch (Exception ex) {
                        observer.onError(io.grpc.Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
                    }
                }));
    }

    private String json(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private JsonNode args(String request) {
        try {
            return request == null || request.isBlank() ? mapper.createObjectNode() : mapper.readTree(request);
        } catch (IOException ex) {
            throw new IllegalArgumentException("request is not JSON", ex);
        }
    }

    private static String text(JsonNode n, String field, String fallback) {
        return n.hasNonNull(field) ? n.get(field).asText() : fallback;
    }

    private String network(String request) {
        JsonNode a = args(request);
        return json(query.networkGraph(text(a, "service", null), a.path("depth").asInt(3),
                a.path("freshMinutes").asInt(0)));
    }

    private String networkAt(String request) {
        return json(query.networkAt(text(args(request), "bucket", "")));
    }

    private String networkPage(String request) {
        JsonNode a = args(request);
        var all = query.networkGraph(text(a, "service", null), a.path("depth").asInt(3), 0).edges();
        int size = Math.max(1, Math.min(a.path("size").asInt(50), 500));
        int page = Math.max(0, a.path("page").asInt(0));
        int from = Math.min(all.size(), page * size);
        int to = Math.min(all.size(), from + size);
        return json(java.util.Map.of("page", page, "size", size, "total", all.size(), "edges", all.subList(from, to)));
    }

    private String merged(String request) {
        return json(query.mergedGraph(args(request).path("freshMinutes").asInt(0)));
    }

    private String ipc(String request) {
        JsonNode a = args(request);
        return json(query.ipcGraph(text(a, "ccyPair", null), text(a, "window", null)));
    }

    private String outliers(String request) {
        JsonNode a = args(request);
        return json(query.outliers(a.path("minLatencyMs").asDouble(0), text(a, "service", null)));
    }

    private String rootSourceRank(String request) {
        return json(rootSource.rank(args(request).path("thresholdMs").asDouble(200)));
    }

    private String mutationHistory(String request) {
        JsonNode a = args(request);
        return json(mutations.findBySrcServiceAndDstServiceOrderByChangedAtAsc(text(a, "src", ""), text(a, "dst", "")));
    }

    private String diff(String request) {
        JsonNode a = args(request);
        return json(query.diff(Instant.parse(text(a, "from", "")), Instant.parse(text(a, "to", ""))));
    }

    private String blastRadius(String request) {
        try {
            BlastRadiusRequest body = mapper.readValue(request, BlastRadiusRequest.class);
            return json(query.blastRadius(body));
        } catch (IOException ex) {
            throw new IllegalArgumentException("blast-radius request is not valid", ex);
        }
    }

}
