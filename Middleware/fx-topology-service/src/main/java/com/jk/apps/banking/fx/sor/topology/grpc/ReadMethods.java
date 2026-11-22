package com.jk.apps.banking.fx.sor.topology.grpc;

import io.grpc.MethodDescriptor;

/** Names of the read-only operations exposed over gRPC (service fx.topology.TopologyRead). */
public final class ReadMethods {

    public static final String SERVICE = "fx.topology.TopologyRead";

    public static final String NETWORK = "NetworkGraph";
    public static final String NETWORK_AT = "NetworkAt";
    public static final String NETWORK_PAGE = "NetworkPage";
    public static final String MERGED = "MergedGraph";
    public static final String IPC = "IpcGraph";
    public static final String OUTLIERS = "TraceOutliers";
    public static final String ROOT_SOURCE = "RootSource";
    public static final String MUTATIONS = "Mutations";
    public static final String DIFF = "Diff";
    public static final String BLAST_RADIUS = "BlastRadius";

    public static MethodDescriptor<String, String> method(String name) {
        return MethodDescriptor.<String, String>newBuilder()
                .setType(MethodDescriptor.MethodType.UNARY)
                .setFullMethodName(MethodDescriptor.generateFullMethodName(SERVICE, name))
                .setRequestMarshaller(JsonMarshaller.INSTANCE)
                .setResponseMarshaller(JsonMarshaller.INSTANCE)
                .build();
    }

    private ReadMethods() {
    }
}
