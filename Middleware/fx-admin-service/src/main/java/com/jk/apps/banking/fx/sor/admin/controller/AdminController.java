package com.jk.apps.banking.fx.sor.admin.controller;

import com.jk.apps.banking.fx.sor.admin.dto.CacheInvalidateRequest;
import com.jk.apps.banking.fx.sor.admin.dto.CreditOverrideRequest;
import com.jk.apps.banking.fx.sor.admin.dto.LoadSimRequest;
import com.jk.apps.banking.fx.sor.admin.dto.SyntheticGenerateRequest;
import com.jk.apps.banking.fx.sor.admin.dto.VenueHaltRequest;
import com.jk.apps.banking.fx.sor.admin.dto.WorkflowAcceptedResponse;
import com.jk.apps.banking.fx.sor.admin.dto.WorkflowStatusResponse;
import com.jk.apps.banking.fx.sor.admin.service.AdminWorkflowService;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminWorkflowService adminWorkflowService;

    public AdminController(AdminWorkflowService adminWorkflowService) {
        this.adminWorkflowService = adminWorkflowService;
    }

    @PostMapping("/synthetic/datasets/generate")
    public ResponseEntity<WorkflowAcceptedResponse> generate(
            @RequestBody(required = false) SyntheticGenerateRequest request) {
        WorkflowAcceptedResponse body =
                adminWorkflowService.generateSynthetic(request == null ? new SyntheticGenerateRequest() : request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    @PostMapping("/synthetic/load-sim/trigger")
    public ResponseEntity<WorkflowAcceptedResponse> loadSim(
            @RequestBody(required = false) LoadSimRequest request) {
        WorkflowAcceptedResponse body =
                adminWorkflowService.triggerLoadSim(request == null ? new LoadSimRequest() : request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(body);
    }

    @GetMapping("/workflows/{wfExecId}")
    public WorkflowStatusResponse getWorkflow(@PathVariable("wfExecId") UUID wfExecId) {
        return adminWorkflowService.getWorkflow(wfExecId);
    }

    @GetMapping("/workflows")
    public Page<WorkflowStatusResponse> listWorkflows(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "workflowType", required = false) String workflowType,
            @RequestParam(name = "triggeredBy", required = false) String triggeredBy,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return adminWorkflowService.listWorkflows(
                status,
                workflowType,
                triggeredBy,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt")));
    }

    @PatchMapping("/workflows/{wfExecId}/cancel")
    public WorkflowStatusResponse cancel(@PathVariable("wfExecId") UUID wfExecId) {
        return adminWorkflowService.cancel(wfExecId);
    }

    @DeleteMapping("/workflows/{wfExecId}")
    public ResponseEntity<Void> delete(
            @PathVariable("wfExecId") UUID wfExecId,
            @RequestParam(name = "purgeSyntheticData", defaultValue = "false") boolean purgeSyntheticData) {
        adminWorkflowService.delete(wfExecId, purgeSyntheticData);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/cache/invalidate")
    public WorkflowAcceptedResponse invalidateCache(@RequestBody(required = false) CacheInvalidateRequest request) {
        return adminWorkflowService.invalidateCache(request == null ? new CacheInvalidateRequest() : request);
    }

    @PostMapping("/credit/override")
    public WorkflowAcceptedResponse creditOverride(@RequestBody CreditOverrideRequest request) {
        return adminWorkflowService.creditOverride(request);
    }

    @PostMapping("/venues/{lpId}/halt")
    public WorkflowAcceptedResponse haltVenue(
            @PathVariable("lpId") String lpId, @RequestBody(required = false) VenueHaltRequest request) {
        return adminWorkflowService.haltVenue(lpId, request == null ? new VenueHaltRequest() : request);
    }

    /** FR-01 — Policy Studio proxies to quote (zero-sharing via HTTP). */
    @GetMapping("/spread-schedules")
    public java.util.List<java.util.Map<String, Object>> listSpreadSchedules() {
        return adminWorkflowService.listSpreadSchedules();
    }

    @PostMapping("/spread-schedules/draft")
    public java.util.Map<String, Object> draftSpreadSchedule(@RequestBody java.util.Map<String, Object> body) {
        return adminWorkflowService.draftSpreadSchedule(body);
    }

    @PostMapping("/spread-schedules/submit")
    public java.util.Map<String, Object> submitSpreadSchedule(@RequestBody java.util.Map<String, Object> body) {
        return adminWorkflowService.submitSpreadSchedule(body);
    }

    @PostMapping("/spread-schedules/approve")
    public java.util.Map<String, Object> approveSpreadSchedule(@RequestBody java.util.Map<String, Object> body) {
        return adminWorkflowService.approveSpreadSchedule(body);
    }

    @PostMapping("/macro-flag")
    public java.util.Map<String, Object> setMacroFlag(@RequestBody(required = false) java.util.Map<String, Object> body) {
        return adminWorkflowService.setMacroFlag(body == null ? java.util.Map.of() : body);
    }

    @DeleteMapping("/macro-flag")
    public java.util.Map<String, Object> clearMacroFlag() {
        return adminWorkflowService.clearMacroFlag();
    }
}
