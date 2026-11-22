package com.jk.apps.banking.fx.sor.topology.service;

import java.util.Map;

/**
 * Premise P9 (Netflix TechBlog): enrichment happens when an edge is written, not when it is queried.
 * Tier, business domain and owner come from a lookup of service metadata. A service with no metadata
 * is marked UNKNOWN rather than dropped, so the gap is visible.
 */
public final class EdgeEnrich {

    public record ServiceMeta(String tier, String domain, String owner) {
    }

    public record Enriched(String src, String dst, String srcTier, String dstTier, String domain, String owner) {
    }

    public static final String UNKNOWN = "UNKNOWN";

    private final Map<String, ServiceMeta> meta;

    public EdgeEnrich(Map<String, ServiceMeta> meta) {
        this.meta = Map.copyOf(meta);
    }

    public Enriched enrich(String src, String dst) {
        ServiceMeta s = meta.get(src);
        ServiceMeta d = meta.get(dst);
        String domain = d != null ? d.domain() : s != null ? s.domain() : UNKNOWN;
        return new Enriched(src, dst,
                s == null ? UNKNOWN : s.tier(),
                d == null ? UNKNOWN : d.tier(),
                domain,
                d == null ? UNKNOWN : d.owner());
    }
}
