package com.jk.apps.banking.fx.sor.topology.domain.repository;

import com.jk.apps.banking.fx.sor.topology.domain.entity.TopologySnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TopologySnapshotRepository extends JpaRepository<TopologySnapshot, UUID> {

    @Query("""
            select s from TopologySnapshot s
            where s.snapshotTs >= :fromTs and s.snapshotTs <= :toTs
            order by s.snapshotTs asc
            """)
    List<TopologySnapshot> findBetween(@Param("fromTs") Instant fromTs, @Param("toTs") Instant toTs);

    List<TopologySnapshot> findBySnapshotTsLessThanEqualOrderBySnapshotTsDesc(Instant ts);
}
