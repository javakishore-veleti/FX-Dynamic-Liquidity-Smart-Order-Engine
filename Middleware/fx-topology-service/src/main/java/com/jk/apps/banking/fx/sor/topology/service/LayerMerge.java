package com.jk.apps.banking.fx.sor.topology.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * Premises P1 and P8 (Netflix TechBlog): edges from separate layers (network, ipc, trace) are merged
 * into one union. Each merged edge lists the layers that reported it. Agreement is the number of
 * layers, so a consumer can see which edges are confirmed by more than one source.
 */
public final class LayerMerge {

    public record Edge(String layer, String src, String dst) {
    }

    public record Merged(String src, String dst, List<String> layers, int agreement) {
    }

    private LayerMerge() {
    }

    public static List<Merged> merge(List<Edge> edges) {
        Map<String, TreeSet<String>> layersByPair = new LinkedHashMap<>();
        Map<String, String[]> endpoints = new LinkedHashMap<>();
        for (Edge e : edges) {
            String key = e.src() + "\u0000" + e.dst();
            layersByPair.computeIfAbsent(key, k -> new TreeSet<>()).add(e.layer());
            endpoints.putIfAbsent(key, new String[] {e.src(), e.dst()});
        }
        List<Merged> out = new ArrayList<>();
        for (Map.Entry<String, TreeSet<String>> en : layersByPair.entrySet()) {
            String[] ends = endpoints.get(en.getKey());
            List<String> layers = List.copyOf(en.getValue());
            out.add(new Merged(ends[0], ends[1], layers, layers.size()));
        }
        return out;
    }
}
