package com.jk.apps.banking.fx.sor.admin.repository;

import com.jk.apps.banking.fx.sor.admin.entity.WorkflowDefinition;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition, String> {

    List<WorkflowDefinition> findByStatusOrderByWorkflowKeyAsc(String status);

    List<WorkflowDefinition> findByPortalScopeAndStatusOrderByWorkflowKeyAsc(String portalScope, String status);
}
