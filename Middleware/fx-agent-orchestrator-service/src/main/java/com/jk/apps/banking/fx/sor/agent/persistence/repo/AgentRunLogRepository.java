package com.jk.apps.banking.fx.sor.agent.persistence.repo;

import com.jk.apps.banking.fx.sor.agent.persistence.entity.AgentRunLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgentRunLogRepository extends JpaRepository<AgentRunLog, UUID> {
}
