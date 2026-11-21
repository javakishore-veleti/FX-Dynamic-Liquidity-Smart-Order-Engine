package com.jk.apps.banking.fx.sor.topology.domain.repository;

import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyIpcEdge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TopologyIpcEdgeRepository extends JpaRepository<TopologyIpcEdge, UUID> {

    @Query("""
            select e from TopologyIpcEdge e
            where (:ccyPair is null or e.ccyPair = :ccyPair)
              and (:window is null or e.windowLabel = :window)
            order by e.observedAt desc
            """)
    List<TopologyIpcEdge> findByCcyPairAndWindow(@Param("ccyPair") String ccyPair,
                                                 @Param("window") String window);
}
