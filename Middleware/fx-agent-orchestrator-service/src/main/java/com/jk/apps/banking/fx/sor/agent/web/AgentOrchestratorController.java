package com.jk.apps.banking.fx.sor.agent.web;

import com.jk.apps.banking.fx.sor.agent.dto.AgentAcceptedResponse;
import com.jk.apps.banking.fx.sor.agent.dto.BackpressureAssessRequest;
import com.jk.apps.banking.fx.sor.agent.dto.BestExecutionExplainRequest;
import com.jk.apps.banking.fx.sor.agent.dto.BlastRadiusAnalyzeRequest;
import com.jk.apps.banking.fx.sor.agent.dto.ChaosSimExecuteRequest;
import com.jk.apps.banking.fx.sor.agent.dto.IncidentRcaInvestigateRequest;
import com.jk.apps.banking.fx.sor.agent.dto.LpToxicityDetectRequest;
import com.jk.apps.banking.fx.sor.agent.dto.MacroGuardAssessRequest;
import com.jk.apps.banking.fx.sor.agent.dto.PolicyAuthoringRequest;
import com.jk.apps.banking.fx.sor.agent.dto.RegulatoryRagAskRequest;
import com.jk.apps.banking.fx.sor.agent.dto.TreasuryHedgeCopilotRequest;
import com.jk.apps.banking.fx.sor.agent.service.AgentOrchestratorService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentOrchestratorController {

    private final AgentOrchestratorService orchestratorService;

    public AgentOrchestratorController(AgentOrchestratorService orchestratorService) {
        this.orchestratorService = orchestratorService;
    }

    @PostMapping("/blast-radius/analyze")
    public ResponseEntity<AgentAcceptedResponse> analyzeBlastRadius(
            @RequestBody(required = false) BlastRadiusAnalyzeRequest request) {
        BlastRadiusAnalyzeRequest body = request == null ? new BlastRadiusAnalyzeRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.analyzeBlastRadius(body));
    }

    @PostMapping("/backpressure/assess")
    public ResponseEntity<AgentAcceptedResponse> assessBackpressure(
            @RequestBody(required = false) BackpressureAssessRequest request) {
        BackpressureAssessRequest body = request == null ? new BackpressureAssessRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.assessBackpressure(body));
    }

    @PostMapping("/incident-rca/investigate")
    public ResponseEntity<AgentAcceptedResponse> investigateIncidentRca(
            @RequestBody(required = false) IncidentRcaInvestigateRequest request) {
        IncidentRcaInvestigateRequest body =
                request == null ? new IncidentRcaInvestigateRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.investigateIncidentRca(body));
    }

    @PostMapping("/policy-authoring/generate")
    public ResponseEntity<AgentAcceptedResponse> generatePolicy(
            @RequestBody(required = false) PolicyAuthoringRequest request) {
        PolicyAuthoringRequest body = request == null ? new PolicyAuthoringRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.generatePolicy(body));
    }

    @PostMapping("/lp-toxicity/detect")
    public ResponseEntity<AgentAcceptedResponse> detectLpToxicity(
            @RequestBody(required = false) LpToxicityDetectRequest request) {
        LpToxicityDetectRequest body = request == null ? new LpToxicityDetectRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.detectLpToxicity(body));
    }

    @PostMapping("/best-execution/explain")
    public ResponseEntity<AgentAcceptedResponse> explainBestExecution(
            @RequestBody(required = false) BestExecutionExplainRequest request) {
        BestExecutionExplainRequest body = request == null ? new BestExecutionExplainRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.explainBestExecution(body));
    }

    @PostMapping("/treasury-hedge/copilot")
    public ResponseEntity<AgentAcceptedResponse> treasuryHedgeCopilot(
            @RequestBody(required = false) TreasuryHedgeCopilotRequest request) {
        TreasuryHedgeCopilotRequest body = request == null ? new TreasuryHedgeCopilotRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.treasuryHedgeCopilot(body));
    }

    @PostMapping("/regulatory-rag/ask")
    public ResponseEntity<AgentAcceptedResponse> askRegulatoryRag(
            @RequestBody(required = false) RegulatoryRagAskRequest request) {
        RegulatoryRagAskRequest body = request == null ? new RegulatoryRagAskRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.askRegulatoryRag(body));
    }

    @PostMapping("/chaos-sim/execute")
    public ResponseEntity<AgentAcceptedResponse> executeChaosSim(
            @RequestBody(required = false) ChaosSimExecuteRequest request) {
        ChaosSimExecuteRequest body = request == null ? new ChaosSimExecuteRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.executeChaosSim(body));
    }

    @PostMapping("/macro-guard/assess")
    public ResponseEntity<AgentAcceptedResponse> assessMacroGuard(
            @RequestBody(required = false) MacroGuardAssessRequest request) {
        MacroGuardAssessRequest body = request == null ? new MacroGuardAssessRequest() : request;
        return ResponseEntity.accepted().body(orchestratorService.assessMacroGuard(body));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}
