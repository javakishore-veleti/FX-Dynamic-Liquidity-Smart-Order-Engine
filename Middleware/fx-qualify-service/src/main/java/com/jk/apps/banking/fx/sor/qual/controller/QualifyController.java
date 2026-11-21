package com.jk.apps.banking.fx.sor.qual.controller;

import com.jk.apps.banking.fx.sor.qual.dto.CreditUtilizationResponse;
import com.jk.apps.banking.fx.sor.qual.dto.KnockoutCheckRequest;
import com.jk.apps.banking.fx.sor.qual.dto.KnockoutCheckResponse;
import com.jk.apps.banking.fx.sor.qual.dto.QualifyOrderRequest;
import com.jk.apps.banking.fx.sor.qual.dto.QualifyOrderResponse;
import com.jk.apps.banking.fx.sor.qual.service.QualifyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fx")
public class QualifyController {

    private final QualifyService qualifyService;

    public QualifyController(QualifyService qualifyService) {
        this.qualifyService = qualifyService;
    }

    @PostMapping("/orders/qualify")
    public QualifyOrderResponse qualify(@RequestBody QualifyOrderRequest request) {
        return qualifyService.qualify(request);
    }

    @GetMapping("/credit/utilization/{clientId}")
    public CreditUtilizationResponse creditUtilization(
            @PathVariable String clientId,
            @RequestParam(required = false) String counterpartyLpId) {
        return qualifyService.creditUtilization(clientId, counterpartyLpId);
    }

    @PostMapping("/venues/knockout-check")
    public KnockoutCheckResponse knockoutCheck(@RequestBody KnockoutCheckRequest request) {
        return qualifyService.knockoutCheck(request);
    }
}
