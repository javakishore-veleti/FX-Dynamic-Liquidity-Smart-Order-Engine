package com.jk.apps.banking.fx.sor.quote.service;

import com.jk.apps.banking.fx.sor.quote.business.facade.EconomicCalendarFacade;
import com.jk.apps.banking.fx.sor.quote.business.facade.LpBookAdminFacade;
import com.jk.apps.banking.fx.sor.quote.business.facade.MacroRegimeFacade;
import com.jk.apps.banking.fx.sor.quote.business.facade.QuotePricingFacade;
import com.jk.apps.banking.fx.sor.quote.business.facade.SpreadScheduleAdminFacade;
import com.jk.apps.banking.fx.sor.quote.cache.LpQuote;
import com.jk.apps.banking.fx.sor.quote.dto.EconomicCalendarEventDto;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteRequest;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteResponse;
import com.jk.apps.banking.fx.sor.quote.dto.MacroFlagRequest;
import com.jk.apps.banking.fx.sor.quote.dto.MacroFlagResponse;
import com.jk.apps.banking.fx.sor.quote.dto.QuoteTickEvent;
import com.jk.apps.banking.fx.sor.quote.dto.RateSnapshotResponse;
import com.jk.apps.banking.fx.sor.quote.dto.RegimeContextResponse;
import com.jk.apps.banking.fx.sor.quote.dto.SpreadScheduleDto;
import java.math.BigDecimal;
import java.time.Instant;
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
    private final EconomicCalendarFacade economicCalendarFacade;
    private final LpBookAdminFacade lpBookAdminFacade;

    public QuoteService(
            QuotePricingFacade pricingFacade,
            MacroRegimeFacade macroRegimeFacade,
            SpreadScheduleAdminFacade scheduleAdminFacade,
            EconomicCalendarFacade economicCalendarFacade,
            LpBookAdminFacade lpBookAdminFacade) {
        this.pricingFacade = pricingFacade;
        this.macroRegimeFacade = macroRegimeFacade;
        this.scheduleAdminFacade = scheduleAdminFacade;
        this.economicCalendarFacade = economicCalendarFacade;
        this.lpBookAdminFacade = lpBookAdminFacade;
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
        String armedBy = body.getArmedBy() == null || body.getArmedBy().isBlank()
                ? MacroRegimeFacade.ARMED_BY_MANUAL
                : body.getArmedBy();
        macroRegimeFacade.ingest(body.getSource(), body.getSeverity(), ttl, armedBy);
        return macroStatus();
    }

    public List<EconomicCalendarEventDto> economicCalendar() {
        return economicCalendarFacade.listEvents();
    }

    public EconomicCalendarEventDto upsertEconomicCalendar(EconomicCalendarEventDto request) {
        return economicCalendarFacade.upsert(request);
    }

    public void deleteEconomicCalendar(String source, Instant start) {
        economicCalendarFacade.delete(source, start);
    }

    public List<LpQuote> listLpBook() {
        return lpBookAdminFacade.listAll();
    }

    public LpQuote upsertLpBook(LpQuote request) {
        return lpBookAdminFacade.upsert(request);
    }

    public void deleteLpBook(String ccyPair, String lpId) {
        lpBookAdminFacade.delete(ccyPair, lpId);
    }

    public void deleteSchedule(String tierId, String ccyPair, String sessionRegime) {
        scheduleAdminFacade.delete(tierId, ccyPair, sessionRegime);
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
            response.setArmedBy(flag.get().armedBy());
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
