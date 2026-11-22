package com.jk.apps.banking.fx.sor.quote.service;

import com.jk.apps.banking.fx.sor.quote.business.facade.MacroRegimeFacade;
import com.jk.apps.banking.fx.sor.quote.business.facade.QuotePricingFacade;
import com.jk.apps.banking.fx.sor.quote.business.facade.SpreadScheduleAdminFacade;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteRequest;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteResponse;
import com.jk.apps.banking.fx.sor.quote.dto.MacroFlagRequest;
import com.jk.apps.banking.fx.sor.quote.dto.MacroFlagResponse;
import com.jk.apps.banking.fx.sor.quote.dto.QuoteTickEvent;
import com.jk.apps.banking.fx.sor.quote.dto.RateSnapshotResponse;
import com.jk.apps.banking.fx.sor.quote.dto.RegimeContextResponse;
import com.jk.apps.banking.fx.sor.quote.dto.SpreadScheduleDto;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Thin application service — delegates to CLOSED business facades (Neal Ford).
 */
@Service
public class QuoteService {

    private final QuotePricingFacade pricingFacade;
    private final MacroRegimeFacade macroRegimeFacade;
    private final SpreadScheduleAdminFacade scheduleAdminFacade;

    public QuoteService(
            QuotePricingFacade pricingFacade,
            MacroRegimeFacade macroRegimeFacade,
            SpreadScheduleAdminFacade scheduleAdminFacade) {
        this.pricingFacade = pricingFacade;
        this.macroRegimeFacade = macroRegimeFacade;
        this.scheduleAdminFacade = scheduleAdminFacade;
    }

    public IndicativeQuoteResponse indicative(IndicativeQuoteRequest request) {
        return pricingFacade.priceIndicative(request);
    }

    public RateSnapshotResponse rates(String ccyPair) {
        return pricingFacade.rates(ccyPair);
    }

    public List<QuoteTickEvent> streamTicks(String ccyPairs, String clientTier, String tenor) {
        return pricingFacade.streamMarkedTicks(ccyPairs, clientTier, tenor);
    }

    public RegimeContextResponse regimeContext(String clientTier, String ccyPair) {
        return pricingFacade.regimeContext(clientTier, ccyPair);
    }

    public MacroFlagResponse ingestMacro(MacroFlagRequest request) {
        MacroFlagRequest body = request == null ? new MacroFlagRequest() : request;
        long ttl = body.getTtlSeconds() == null ? 3600L : body.getTtlSeconds();
        macroRegimeFacade.ingest(body.getSource(), body.getSeverity(), ttl);
        return macroStatus();
    }

    public MacroFlagResponse clearMacro() {
        macroRegimeFacade.clear();
        return macroStatus();
    }

    public MacroFlagResponse macroStatus() {
        MacroFlagResponse response = new MacroFlagResponse();
        var flag = macroRegimeFacade.current();
        response.setActive(flag.isPresent());
        if (flag.isPresent()) {
            response.setRegime(flag.get().regime());
            response.setSource(flag.get().source());
            response.setSeverity(flag.get().severity());
            response.setExpiresAt(flag.get().expiresAt());
        } else {
            response.setRegime(pricingFacade.resolveActiveRegime());
        }
        response.setLastLookPenaltyWeight(
                pricingFacade.lastLookPenaltyWeight(pricingFacade.resolveActiveRegime()));
        return response;
    }

    public List<SpreadScheduleDto> listSchedules() {
        return scheduleAdminFacade.listAll();
    }

    public SpreadScheduleDto draftSchedule(SpreadScheduleDto request) {
        return scheduleAdminFacade.draft(request);
    }

    public SpreadScheduleDto submitSchedule(SpreadScheduleDto request) {
        return scheduleAdminFacade.submitForChecker(request);
    }

    public SpreadScheduleDto approveSchedule(SpreadScheduleDto request) {
        return scheduleAdminFacade.approve(request);
    }

    public void invalidateScheduleCache() {
        scheduleAdminFacade.invalidateCache();
    }

    public BigDecimal lastLookPenaltyWeight() {
        return pricingFacade.lastLookPenaltyWeight(pricingFacade.resolveActiveRegime());
    }
}
