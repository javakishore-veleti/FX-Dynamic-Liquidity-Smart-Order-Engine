package com.jk.apps.banking.fx.sor.exec.controller;

import com.jk.apps.banking.fx.sor.exec.business.facade.LpRankingConfigFacade;
import com.jk.apps.banking.fx.sor.exec.dto.LpRankingEntryDto;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Presentation — LP ranking seed/config (API → facade). */
@RestController
@RequestMapping("/api/v1/fx/reference")
public class ReferenceDataController {

    private final LpRankingConfigFacade lpRankingConfigFacade;

    public ReferenceDataController(LpRankingConfigFacade lpRankingConfigFacade) {
        this.lpRankingConfigFacade = lpRankingConfigFacade;
    }

    @GetMapping("/lp-rankings")
    public List<LpRankingEntryDto> listLpRankings() {
        return lpRankingConfigFacade.listAll();
    }

    @PutMapping("/lp-rankings")
    public LpRankingEntryDto upsert(@RequestBody LpRankingEntryDto body) {
        return lpRankingConfigFacade.upsert(body);
    }

    @PutMapping("/lp-rankings/bulk")
    public List<LpRankingEntryDto> replaceAll(@RequestBody List<LpRankingEntryDto> body) {
        lpRankingConfigFacade.replaceAll(body);
        return lpRankingConfigFacade.listAll();
    }

    @DeleteMapping("/lp-rankings/{lpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("lpId") String lpId) {
        lpRankingConfigFacade.delete(lpId);
    }
}
