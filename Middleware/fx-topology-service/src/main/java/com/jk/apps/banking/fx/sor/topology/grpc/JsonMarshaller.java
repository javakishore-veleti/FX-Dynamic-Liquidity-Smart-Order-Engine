package com.jk.apps.banking.fx.sor.topology.grpc;

import io.grpc.MethodDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Carries a JSON document as the gRPC message body (UTF-8). Keeps the read API free of codegen. */
final class JsonMarshaller implements MethodDescriptor.Marshaller<String> {

    static final JsonMarshaller INSTANCE = new JsonMarshaller();

    @Override
    public InputStream stream(String value) {
        return new java.io.ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String parse(InputStream stream) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            stream.transferTo(out);
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read gRPC message", ex);
        }
    }
}
