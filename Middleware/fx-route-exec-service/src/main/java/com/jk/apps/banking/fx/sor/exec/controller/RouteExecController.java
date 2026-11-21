package com.jk.apps.banking.fx.sor.exec.controller;

import com.jk.apps.banking.fx.sor.exec.dto.FailoverRequest;
import com.jk.apps.banking.fx.sor.exec.dto.FailoverResponse;
import com.jk.apps.banking.fx.sor.exec.dto.OrderTraceResponse;
import com.jk.apps.banking.fx.sor.exec.dto.RouteExecuteRequest;
import com.jk.apps.banking.fx.sor.exec.dto.RouteExecuteResponse;
import com.jk.apps.banking.fx.sor.exec.service.RouteExecService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fx")
public class RouteExecController {

    private final RouteExecService routeExecService;

    public RouteExecController(RouteExecService routeExecService) {
        this.routeExecService = routeExecService;
    }

    @PostMapping("/orders/route-execute")
    public RouteExecuteResponse routeExecute(@RequestBody RouteExecuteRequest request) {
        return routeExecService.routeExecute(request);
    }

    @PostMapping("/orders/{orderId}/failover")
    public FailoverResponse failover(@PathVariable UUID orderId, @RequestBody FailoverRequest request) {
        return routeExecService.failover(orderId, request);
    }

    @GetMapping("/orders/{clientOrderId}/trace")
    public OrderTraceResponse trace(@PathVariable String clientOrderId) {
        return routeExecService.trace(clientOrderId);
    }
}
