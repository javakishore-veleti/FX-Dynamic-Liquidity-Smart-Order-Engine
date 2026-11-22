package com.jk.apps.banking.fx.sor.topology.api;

import com.jk.apps.banking.fx.sor.topology.api.dto.BlastRadiusRequest;
import com.jk.apps.banking.fx.sor.topology.api.dto.BlastRadiusResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.IpcGraphResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.NetworkGraphResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.TopologyDiffResponse;
import com.jk.apps.banking.fx.sor.topology.api.dto.TraceOutlierResponse;
import com.jk.apps.banking.fx.sor.topology.service.TopologyQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping(path = "/api/v1/topology", produces = MediaType.APPLICATION_JSON_VALUE)
public class TopologyController {

    private final TopologyQueryService topologyQueryService;

    public TopologyController(TopologyQueryService topologyQueryService) {
        this.topologyQueryService = topologyQueryService;
    }

    @GetMapping("/graph/network")
    public NetworkGraphResponse networkGraph(
            @RequestParam(name = "service", required = false) String service,
            @RequestParam(name = "depth", defaultValue = "3") int depth,
            @RequestParam(name = "freshMinutes", defaultValue = "0") int freshMinutes) {
        return topologyQueryService.networkGraph(service, depth, freshMinutes);
    }

    @GetMapping("/graph/merged")
    public java.util.List<com.jk.apps.banking.fx.sor.topology.service.LayerMerge.Merged> mergedGraph(
            @RequestParam(name = "freshMinutes", defaultValue = "0") int freshMinutes) {
        return topologyQueryService.mergedGraph(freshMinutes);
    }

    @GetMapping("/graph/ipc")
    public IpcGraphResponse ipcGraph(
            @RequestParam(name = "ccyPair", required = false) String ccyPair,
            @RequestParam(name = "window", defaultValue = "5m") String window) {
        return topologyQueryService.ipcGraph(ccyPair, window);
    }

    @GetMapping("/traces/outliers")
    public TraceOutlierResponse outliers(
            @RequestParam(name = "minLatencyMs", defaultValue = "10") double minLatencyMs,
            @RequestParam(name = "serviceName", required = false) String serviceName) {
        return topologyQueryService.outliers(minLatencyMs, serviceName);
    }

    @PostMapping(path = "/blast-radius", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BlastRadiusResponse blastRadius(@RequestBody BlastRadiusRequest request) {
        return topologyQueryService.blastRadius(request);
    }

    @GetMapping("/diff")
    public TopologyDiffResponse diff(
            @RequestParam(name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return topologyQueryService.diff(from, to);
    }
}
