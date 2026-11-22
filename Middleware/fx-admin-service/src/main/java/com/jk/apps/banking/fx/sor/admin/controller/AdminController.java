package com.jk.apps.banking.fx.sor.admin.controller;

import com.jk.apps.banking.fx.sor.admin.dto.BpmnProcessView;
import com.jk.apps.banking.fx.sor.admin.dto.CacheInvalidateRequest;
import com.jk.apps.banking.fx.sor.admin.dto.CreditOverrideRequest;
import com.jk.apps.banking.fx.sor.admin.dto.LoadSimRequest;
import com.jk.apps.banking.fx.sor.admin.dto.SyntheticGenerateRequest;
import com.jk.apps.banking.fx.sor.admin.dto.VenueHaltRequest;
import com.jk.apps.banking.fx.sor.admin.dto.WorkflowAcceptedResponse;
import com.jk.apps.banking.fx.sor.admin.dto.WorkflowStatusResponse;
import com.jk.apps.banking.fx.sor.admin.entity.WorkflowDefinition;
import com.jk.apps.banking.fx.sor.admin.service.AdminWorkflowService;
import com.jk.apps.banking.fx.sor.admin.service.BpmnWorkflowService;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final AdminWorkflowService adminWorkflowService;
    private final BpmnWorkflowService bpmnWorkflowService;

    public AdminController(AdminWorkflowService adminWorkflowService, BpmnWorkflowService bpmnWorkflowService) {
        this.adminWorkflowService = adminWorkflowService;
        this.bpmnWorkflowService = bpmnWorkflowService;
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

    /** Master BPMN workflow catalog (table: workflows). */
    @GetMapping("/workflow-definitions")
    public List<WorkflowDefinition> listWorkflowDefinitions(
            @RequestParam(name = "portalScope", required = false) String portalScope) {
        return bpmnWorkflowService.listMaster(portalScope);
    }

    @GetMapping("/workflow-definitions/{workflowKey}")
    public WorkflowDefinition getWorkflowDefinition(@PathVariable("workflowKey") String workflowKey) {
        return bpmnWorkflowService.getMaster(workflowKey);
    }

    /** Start a BPMN process — always creates a wf_exec_log row. */
    @PostMapping("/bpmn/{workflowKey}/start")
    public BpmnProcessView startBpmn(
            @PathVariable("workflowKey") String workflowKey,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> vars = body == null ? Map.of() : body;
        String triggeredBy = vars.get("triggeredBy") == null ? "ADMIN_PORTAL" : String.valueOf(vars.get("triggeredBy"));
        return bpmnWorkflowService.start(workflowKey, vars, triggeredBy);
    }

    @GetMapping("/bpmn/executions/{wfExecId}")
    public BpmnProcessView getBpmnExecution(@PathVariable("wfExecId") UUID wfExecId) {
        return bpmnWorkflowService.getProcess(wfExecId);
    }

    @PostMapping("/bpmn/executions/{wfExecId}/complete-task")
    public BpmnProcessView completeBpmnTask(
            @PathVariable("wfExecId") UUID wfExecId, @RequestBody(required = false) Map<String, Object> body) {
        return bpmnWorkflowService.completeCurrentTask(wfExecId, body == null ? Map.of() : body);
    }

    @GetMapping("/workflows/{wfExecId}")
    public WorkflowStatusResponse getWorkflow(@PathVariable("wfExecId") UUID wfExecId) {
        return adminWorkflowService.getWorkflow(wfExecId);
    }

    @GetMapping("/workflows")
    public Page<WorkflowStatusResponse> listWorkflows(
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "workflowType", required = false) String workflowType,
            @RequestParam(name = "workflowKey", required = false) String workflowKey,
            @RequestParam(name = "triggeredBy", required = false) String triggeredBy,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        if (workflowKey != null && !workflowKey.isBlank()) {
            return bpmnWorkflowService
                    .listExecutions(
                            status,
                            workflowType,
                            workflowKey,
                            triggeredBy,
                            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt")))
                    .map(adminWorkflowService::toStatusPublic);
        }
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

    @GetMapping("/spread-schedules")
    public List<Map<String, Object>> listSpreadSchedules() {
        return adminWorkflowService.listSpreadSchedules();
    }

    @PostMapping("/spread-schedules/draft")
    public Map<String, Object> draftSpreadSchedule(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.draftSpreadSchedule(body);
    }

    @PostMapping("/spread-schedules/submit")
    public Map<String, Object> submitSpreadSchedule(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.submitSpreadSchedule(body);
    }

    @PostMapping("/spread-schedules/approve")
    public Map<String, Object> approveSpreadSchedule(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.approveSpreadSchedule(body);
    }

    @PostMapping("/macro-flag")
    public Map<String, Object> setMacroFlag(@RequestBody(required = false) Map<String, Object> body) {
        return adminWorkflowService.setMacroFlag(body == null ? Map.of() : body);
    }

    @DeleteMapping("/macro-flag")
    public Map<String, Object> clearMacroFlag() {
        return adminWorkflowService.clearMacroFlag();
    }

    @GetMapping("/macro-flag")
    public Map<String, Object> getMacroFlag() {
        return adminWorkflowService.getMacroFlag();
    }

    @GetMapping("/economic-calendar")
    public List<Map<String, Object>> economicCalendar() {
        return adminWorkflowService.listEconomicCalendar();
    }

    @PutMapping("/economic-calendar")
    public Map<String, Object> upsertEconomicCalendar(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.upsertEconomicCalendar(body);
    }

    @DeleteMapping("/economic-calendar/{source}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEconomicCalendar(
            @PathVariable("source") String source, @RequestParam("start") String start) {
        adminWorkflowService.deleteEconomicCalendar(source, start);
    }

    @DeleteMapping("/spread-schedules/{tierId}/{ccyPair}/{sessionRegime}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSpreadSchedule(
            @PathVariable("tierId") String tierId,
            @PathVariable("ccyPair") String ccyPair,
            @PathVariable("sessionRegime") String sessionRegime) {
        adminWorkflowService.deleteSpreadSchedule(tierId, ccyPair, sessionRegime);
    }

    @GetMapping("/lp-book")
    public List<Map<String, Object>> listLpBook() {
        return adminWorkflowService.listLpBook();
    }

    @PutMapping("/lp-book")
    public Map<String, Object> upsertLpBook(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.upsertLpBook(body);
    }

    @DeleteMapping("/lp-book/{ccyPair}/{lpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLpBook(
            @PathVariable("ccyPair") String ccyPair, @PathVariable("lpId") String lpId) {
        adminWorkflowService.deleteLpBook(ccyPair, lpId);
    }

    @GetMapping("/lp-rankings")
    public List<Map<String, Object>> listLpRankings() {
        return adminWorkflowService.listLpRankings();
    }

    @PutMapping("/lp-rankings")
    public Map<String, Object> upsertLpRanking(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.upsertLpRanking(body);
    }

    @DeleteMapping("/lp-rankings/{lpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLpRanking(@PathVariable("lpId") String lpId) {
        adminWorkflowService.deleteLpRanking(lpId);
    }

    @GetMapping("/settlement-policies")
    public List<Map<String, Object>> listSettlementPolicies() {
        return adminWorkflowService.listSettlementPolicies();
    }

    @PutMapping("/settlement-policies")
    public Map<String, Object> upsertSettlementPolicy(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.upsertSettlementPolicy(body);
    }

    @DeleteMapping("/settlement-policies/{ccyPair}/{tenor}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSettlementPolicy(
            @PathVariable("ccyPair") String ccyPair, @PathVariable("tenor") String tenor) {
        adminWorkflowService.deleteSettlementPolicy(ccyPair, tenor);
    }

    @GetMapping("/credit-lines")
    public List<Map<String, Object>> listCreditLines() {
        return adminWorkflowService.listCreditLines();
    }

    @PutMapping("/credit-lines")
    public Map<String, Object> upsertCreditLine(@RequestBody Map<String, Object> body) {
        return adminWorkflowService.upsertCreditLine(body);
    }

    @DeleteMapping("/credit-lines/{clientId}/{lpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCreditLine(
            @PathVariable("clientId") String clientId, @PathVariable("lpId") String lpId) {
        adminWorkflowService.deleteCreditLine(clientId, lpId);
    }
}
