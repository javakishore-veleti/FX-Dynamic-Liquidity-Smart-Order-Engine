package com.jk.apps.banking.fx.sor.admin.repository;

import com.jk.apps.banking.fx.sor.admin.entity.WfTaskExecLog;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WfTaskExecLogRepository extends JpaRepository<WfTaskExecLog, UUID> {

    List<WfTaskExecLog> findByWfExecIdOrderByTaskSeqAsc(UUID wfExecId);
}
