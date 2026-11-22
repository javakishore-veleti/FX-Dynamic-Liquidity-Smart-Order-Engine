package com.jk.apps.banking.fx.sor.topology.service;

import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyEdgeMutation;
import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyEdgeMutationRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Premise P10: records a mutation when an edge property changes between writes. Only changes are
 * recorded, so the log stays small. The last known value per edge is kept in memory and rebuilt from
 * the log on restart is not needed for correctness: a restart may record one extra mutation.
 */
@Component
public class EdgeMutationLog {

    private final TopologyEdgeMutationRepository repository;
    private final Map<String, String> last = new ConcurrentHashMap<>();

    public EdgeMutationLog(TopologyEdgeMutationRepository repository) {
        this.repository = repository;
    }

    /** Records flow_count changes for one edge. Returns true when a mutation was written. */
    public boolean observeFlowCount(String src, String dst, long flowCount, Instant now) {
        String key = src + "\u0000" + dst;
        String value = Long.toString(flowCount);
        String previous = last.put(key, value);
        if (value.equals(previous)) {
            return false;
        }
        TopologyEdgeMutation m = new TopologyEdgeMutation();
        m.setId(UUID.randomUUID());
        m.setSrcService(src);
        m.setDstService(dst);
        m.setProperty("flow_count");
        m.setOldValue(previous);
        m.setNewValue(value);
        m.setChangedAt(now);
        repository.save(m);
        return true;
    }
}
