package com.jk.apps.banking.fx.sor.topology.mcp.persistence.repo;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jk.apps.banking.fx.sor.topology.mcp.persistence.entity.McpToolInvocationLog;

public interface McpToolInvocationLogRepository extends JpaRepository<McpToolInvocationLog, UUID> {
}
