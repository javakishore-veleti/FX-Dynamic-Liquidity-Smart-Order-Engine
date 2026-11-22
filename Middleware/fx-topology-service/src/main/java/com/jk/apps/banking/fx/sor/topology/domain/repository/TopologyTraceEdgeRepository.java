package com.jk.apps.banking.fx.sor.topology.domain.repository;

import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyTraceEdge;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopologyTraceEdgeRepository extends JpaRepository<TopologyTraceEdge, UUID> {
}
