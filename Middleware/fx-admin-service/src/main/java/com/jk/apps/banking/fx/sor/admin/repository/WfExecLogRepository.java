package com.jk.apps.banking.fx.sor.admin.repository;

import com.jk.apps.banking.fx.sor.admin.entity.WfExecLog;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WfExecLogRepository extends JpaRepository<WfExecLog, UUID> {

    @Query("""
            SELECT w FROM WfExecLog w
            WHERE (:status IS NULL OR w.status = :status)
              AND (:workflowType IS NULL OR w.workflowType = :workflowType)
              AND (:triggeredBy IS NULL OR w.triggeredBy = :triggeredBy)
            """)
    Page<WfExecLog> findFiltered(
            @Param("status") String status,
            @Param("workflowType") String workflowType,
            @Param("triggeredBy") String triggeredBy,
            Pageable pageable);
}
