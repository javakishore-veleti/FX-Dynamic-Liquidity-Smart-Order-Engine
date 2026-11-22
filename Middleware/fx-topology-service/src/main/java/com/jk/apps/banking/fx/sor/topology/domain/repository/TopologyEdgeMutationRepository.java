package com.jk.apps.banking.fx.sor.topology.domain.repository;

import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyEdgeMutation;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopologyEdgeMutationRepository extends JpaRepository<TopologyEdgeMutation, UUID> {

    List<TopologyEdgeMutation> findBySrcServiceAndDstServiceOrderByChangedAtAsc(String src, String dst);
}
