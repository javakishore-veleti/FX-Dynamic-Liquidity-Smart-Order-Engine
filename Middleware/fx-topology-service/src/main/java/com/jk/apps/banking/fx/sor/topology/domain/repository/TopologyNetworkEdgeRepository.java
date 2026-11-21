package com.jk.apps.banking.fx.sor.topology.domain.repository;

import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyNetworkEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TopologyNetworkEdgeRepository extends JpaRepository<TopologyNetworkEdge, UUID> {

    @Query("""
            select e from TopologyNetworkEdge e
            where (:service is null or e.srcService = :service or e.dstService = :service)
            order by e.observedAt desc
            """)
    List<TopologyNetworkEdge> findByService(@Param("service") String service);

    List<TopologyNetworkEdge> findBySrcService(String srcService);
}
