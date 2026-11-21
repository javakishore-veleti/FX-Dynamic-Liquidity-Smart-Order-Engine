package com.jk.apps.banking.fx.sor.topology.mcp.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.mcp.persistence.entity.McpToolInvocationLog;
import com.jk.apps.banking.fx.sor.topology.mcp.persistence.repo.McpToolInvocationLogRepository;

@Service
public class McpInvocationAuditService {

    private static final Logger log = LoggerFactory.getLogger(McpInvocationAuditService.class);

    private final McpToolInvocationLogRepository repository;
    private final ObjectMapper objectMapper;

    public McpInvocationAuditService(McpToolInvocationLogRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public UUID begin(String toolName, Object request, String safetyTier, boolean dryRun) {
        McpToolInvocationLog row = new McpToolInvocationLog();
        row.setInvocationId(UUID.randomUUID());
        row.setToolName(toolName);
        row.setRequestPayload(toJson(request));
        row.setStatus("RUNNING");
        row.setSafetyTier(safetyTier);
        row.setDryRun(dryRun);
        row.setTriggeredBy("MCP_CLIENT");
        row.setStartedAt(Instant.now());
        repository.save(row);
        return row.getInvocationId();
    }

    @Transactional
    public void complete(UUID invocationId, Object response, long durationMs) {
        repository.findById(invocationId).ifPresent(row -> {
            row.setStatus("SUCCESS");
            row.setResponsePayload(toJson(response));
            row.setDurationMs(durationMs);
            row.setCompletedAt(Instant.now());
            repository.save(row);
        });
    }

    @Transactional
    public void fail(UUID invocationId, String error, long durationMs) {
        repository.findById(invocationId).ifPresent(row -> {
            row.setStatus("FAILED");
            row.setErrorDetails(error);
            row.setDurationMs(durationMs);
            row.setCompletedAt(Instant.now());
            repository.save(row);
        });
    }

    private String toJson(Object value) {
        if (value == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            log.debug("Failed to serialize audit payload: {}", ex.getMessage());
            return "{\"serializationError\":true}";
        }
    }
}
