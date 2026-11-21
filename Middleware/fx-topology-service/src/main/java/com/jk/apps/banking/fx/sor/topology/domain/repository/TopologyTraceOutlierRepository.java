package com.jk.apps.banking.fx.sor.topology.domain.repository;

import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologyTraceOutlier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TopologyTraceOutlierRepository extends JpaRepository<TopologyTraceOutlier, UUID> {

    @Query("""
            select t from TopologyTraceOutlier t
            where t.latencyMs >= :minLatencyMs
              and (:serviceName is null or t.serviceName = :serviceName)
            order by t.latencyMs desc
            """)
    List<TopologyTraceOutlier> findOutliers(@Param("minLatencyMs") double minLatencyMs,
                                            @Param("serviceName") String serviceName);
}
