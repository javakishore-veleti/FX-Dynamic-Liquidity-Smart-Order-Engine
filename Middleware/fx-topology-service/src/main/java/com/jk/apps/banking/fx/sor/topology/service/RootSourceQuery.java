package com.jk.apps.banking.fx.sor.topology.service;

import com.jk.apps.banking.fx.sor.topology.domain.repository.TopologyIpcEdgeRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/** Reads IPC edges and runs the root-source analysis (premise P11). */
@Component
public class RootSourceQuery {

    private final TopologyIpcEdgeRepository ipcEdges;

    public RootSourceQuery(TopologyIpcEdgeRepository ipcEdges) {
        this.ipcEdges = ipcEdges;
    }

    public List<RootSource.Candidate> rank(double thresholdMs) {
        List<RootSource.CallEdge> edges = ipcEdges.findAll().stream()
                .map(e -> new RootSource.CallEdge(e.getSrcService(), e.getDstService(), e.getP99Ms(), e.getCircuitState()))
                .toList();
        return RootSource.rank(edges, thresholdMs);
    }
}
