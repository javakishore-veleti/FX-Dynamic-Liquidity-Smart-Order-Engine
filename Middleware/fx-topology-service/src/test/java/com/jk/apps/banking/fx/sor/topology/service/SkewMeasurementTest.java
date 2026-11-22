package com.jk.apps.banking.fx.sor.topology.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

/**
 * Premise P5 measurement: how unevenly does hash partitioning spread a skewed (power-law) traffic
 * mix across instances? Traffic: 200 services, service i sends volume proportional to 1/(i+1).
 * Each hop is keyed by its source service, so the load of one hot source lands on one instance.
 * The test records the measured imbalance; it does not assume it.
 */
class SkewMeasurementTest {

    @Test
    void measuredImbalanceUnderPowerLawTraffic() {
        List<String> instances = List.of("i1", "i2", "i3");
        HashRing ring = new HashRing(instances);
        Map<String, Double> load = new TreeMap<>();
        instances.forEach(i -> load.put(i, 0.0));
        List<String> sources = new ArrayList<>();
        for (int s = 0; s < 200; s++) {
            sources.add("svc-" + s);
        }
        double total = 0;
        for (int s = 0; s < sources.size(); s++) {
            double volume = 1.0 / (s + 1); // power law: a few services carry most traffic
            total += volume;
            load.merge(ring.owner(sources.get(s)), volume, Double::sum);
        }
        final double totalFinal = total;
        double max = load.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double min = load.values().stream().mapToDouble(Double::doubleValue).min().orElse(0);
        System.out.printf("SKEW power-law: shares=%s max/mean=%.2f max/min=%.2f%n",
                load.entrySet().stream().map(e -> e.getKey() + ":" + String.format("%.2f", e.getValue() / totalFinal)).toList(),
                max / (total / instances.size()), max / min);
        // Hashing by source keeps every instance within a bounded factor of the mean for this mix.
        assertTrue(max / (total / instances.size()) < 1.6, "imbalance above bound");
    }
}
