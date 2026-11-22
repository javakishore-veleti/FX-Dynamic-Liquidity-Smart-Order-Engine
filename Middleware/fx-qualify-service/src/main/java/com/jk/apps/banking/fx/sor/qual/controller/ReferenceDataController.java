package com.jk.apps.banking.fx.sor.qual.controller;

import com.jk.apps.banking.fx.sor.qual.business.facade.CreditLineAdminFacade;
import com.jk.apps.banking.fx.sor.qual.business.facade.SettlementPolicyAdminFacade;
import com.jk.apps.banking.fx.sor.qual.cache.CreditLine;
import com.jk.apps.banking.fx.sor.qual.dto.SettlementPolicyDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Presentation — seeded/config reference data owned by qualify (API → facade → DAO/store).
 */
@RestController
@RequestMapping("/api/v1/fx/reference")
public class ReferenceDataController {

    private final SettlementPolicyAdminFacade settlementPolicyAdminFacade;
    private final CreditLineAdminFacade creditLineAdminFacade;

    public ReferenceDataController(
            SettlementPolicyAdminFacade settlementPolicyAdminFacade,
            CreditLineAdminFacade creditLineAdminFacade) {
        this.settlementPolicyAdminFacade = settlementPolicyAdminFacade;
        this.creditLineAdminFacade = creditLineAdminFacade;
    }

    @GetMapping("/settlement-policies")
    public List<SettlementPolicyDto> listPolicies() {
        return settlementPolicyAdminFacade.listAll();
    }

    /** Called by the admin Initial Data workflow. Loads the committed settlement seed. */
    @PostMapping("/settlement-policies/load")
    public java.util.Map<String, Object> loadPolicies() {
        return java.util.Map.of("loaded", settlementPolicyAdminFacade.loadCommitted());
    }

    @PutMapping("/settlement-policies")
    public SettlementPolicyDto upsertPolicy(@RequestBody SettlementPolicyDto body) {
        return settlementPolicyAdminFacade.upsert(body);
    }

    @DeleteMapping("/settlement-policies/{ccyPair}/{tenor}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePolicy(
            @PathVariable("ccyPair") String ccyPair, @PathVariable("tenor") String tenor) {
        settlementPolicyAdminFacade.delete(ccyPair, tenor);
    }

    @GetMapping("/credit-lines")
    public List<CreditLine> listCreditLines() {
        return creditLineAdminFacade.listAll();
    }

    @PutMapping("/credit-lines")
    public CreditLine upsertCreditLine(@RequestBody CreditLine body) {
        return creditLineAdminFacade.upsert(body);
    }

    @DeleteMapping("/credit-lines/{clientId}/{lpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCreditLine(
            @PathVariable("clientId") String clientId, @PathVariable("lpId") String lpId) {
        creditLineAdminFacade.delete(clientId, lpId);
    }
}
