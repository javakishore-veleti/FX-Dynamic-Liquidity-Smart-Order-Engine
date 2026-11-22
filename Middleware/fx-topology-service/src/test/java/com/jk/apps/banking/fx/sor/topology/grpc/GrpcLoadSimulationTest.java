package com.jk.apps.banking.fx.sor.topology.grpc;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.NetworkGraphRequest;
import com.jk.apps.banking.fx.sor.topology.grpc.v1.TopologyReadGrpc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/**
 * Parallel load simulation for the read-only gRPC API. Opt-in: runs only with -Dgrpc.load=true against
 * a running topology service that has fx.features.grpc-enabled=true.
 *   -Dgrpc.load=true -Dgrpc.host=localhost -Dgrpc.port=9085 -Dgrpc.clients=20 -Dgrpc.calls=200
 */
class GrpcLoadSimulationTest {

    @Test
    void parallelReadLoad() throws Exception {
        assumeTrue(Boolean.getBoolean("grpc.load"), "set -Dgrpc.load=true to run the load simulation");
        String host = System.getProperty("grpc.host", "localhost");
        int port = Integer.getInteger("grpc.port", 9085);
        int clients = Integer.getInteger("grpc.clients", 20);
        int callsPerClient = Integer.getInteger("grpc.calls", 200) / Math.max(1, clients);

        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        ExecutorService pool = Executors.newFixedThreadPool(clients);
        List<Future<List<Long>>> futures = new ArrayList<>();
        TopologyReadGrpc.TopologyReadBlockingStub stub = TopologyReadGrpc.newBlockingStub(channel);
        try {
            for (int c = 0; c < clients; c++) {
                futures.add(pool.submit(() -> {
                    List<Long> latencies = new ArrayList<>();
                    for (int i = 0; i < callsPerClient; i++) {
                        long t0 = System.nanoTime();
                        stub.networkGraph(NetworkGraphRequest.newBuilder().setDepth(3).build());
                        latencies.add((System.nanoTime() - t0) / 1_000_000);
                    }
                    return latencies;
                }));
            }
            List<Long> all = new ArrayList<>();
            for (Future<List<Long>> f : futures) {
                all.addAll(f.get(120, TimeUnit.SECONDS));
            }
            Collections.sort(all);
            System.out.printf("GRPC LOAD clients=%d calls=%d p50=%dms p99=%dms max=%dms%n",
                    clients, all.size(), all.get(all.size() / 2),
                    all.get(Math.max(0, (int) (all.size() * 0.99) - 1)), all.get(all.size() - 1));
        } finally {
            pool.shutdownNow();
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}
