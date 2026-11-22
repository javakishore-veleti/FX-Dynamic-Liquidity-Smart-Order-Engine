package com.jk.apps.banking.fx.sor.quote.controller;

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
import com.jk.apps.banking.fx.sor.quote.service.QuoteService;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/fx")
public class QuoteController {

    private final QuoteService quoteService;

    public QuoteController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @GetMapping(value = "/quotes/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamQuotes(
            @RequestParam(name = "ccyPairs", required = false) String ccyPairs,
            @RequestParam(name = "clientTier", required = false) String clientTier,
            @RequestParam(name = "tenor", required = false, defaultValue = "SPOT") String tenor) {
        SseEmitter emitter = new SseEmitter(30_000L);
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        AtomicInteger ticks = new AtomicInteger();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (ticks.incrementAndGet() > 10) {
                    emitter.complete();
                    scheduler.shutdown();
                    return;
                }
                List<QuoteTickEvent> events = quoteService.streamTicks(ccyPairs, clientTier, tenor);
                for (QuoteTickEvent event : events) {
                    emitter.send(SseEmitter.event().name("quote").data(event));
                }
            } catch (IOException ex) {
                emitter.completeWithError(ex);
                scheduler.shutdown();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
        emitter.onCompletion(scheduler::shutdown);
        emitter.onTimeout(() -> {
            scheduler.shutdown();
            emitter.complete();
        });
        return emitter;
    }

    @PostMapping("/quotes/indicative")
    public IndicativeQuoteResponse indicative(@RequestBody IndicativeQuoteRequest request) {
        return quoteService.indicative(request);
    }

    @GetMapping("/rates/{ccyPair}")
    public RateSnapshotResponse rates(@PathVariable("ccyPair") String ccyPair) {
        return quoteService.rates(ccyPair);
    }

    @GetMapping("/regime/context")
    public RegimeContextResponse regimeContext(
            @RequestParam(name = "clientTier", required = false, defaultValue = "TIER_2") String clientTier,
            @RequestParam(name = "ccyPair", required = false, defaultValue = "EURUSD") String ccyPair) {
        return quoteService.regimeContext(clientTier, ccyPair);
    }

    @GetMapping("/spread-schedules")
    public List<SpreadScheduleDto> listSchedules() {
        return quoteService.listSchedules();
    }

    @PostMapping("/spread-schedules/draft")
    public SpreadScheduleDto draftSchedule(@RequestBody SpreadScheduleDto request) {
        return quoteService.draftSchedule(request);
    }

    @PostMapping("/spread-schedules/submit")
    public SpreadScheduleDto submitSchedule(@RequestBody SpreadScheduleDto request) {
        return quoteService.submitSchedule(request);
    }

    @PostMapping("/spread-schedules/approve")
    public SpreadScheduleDto approveSchedule(@RequestBody SpreadScheduleDto request) {
        return quoteService.approveSchedule(request);
    }

    @PostMapping("/spread-schedules/cache/invalidate")
    public Map<String, String> invalidateScheduleCache() {
        quoteService.invalidateScheduleCache();
        return Map.of("status", "INVALIDATED");
    }

    @DeleteMapping("/spread-schedules/{tierId}/{ccyPair}/{sessionRegime}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchedule(
            @PathVariable("tierId") String tierId,
            @PathVariable("ccyPair") String ccyPair,
            @PathVariable("sessionRegime") String sessionRegime) {
        quoteService.deleteSchedule(tierId, ccyPair, sessionRegime);
    }

    @GetMapping("/lp-book")
    public List<LpQuote> listLpBook() {
        return quoteService.listLpBook();
    }

    /** Called by the admin Initial Data workflow. Loads the committed LP book starter. */
    @PostMapping("/lp-book/load")
    public Map<String, Object> loadLpBook() {
        return Map.of("loaded", quoteService.loadLpBook());
    }

    @PutMapping("/lp-book")
    public LpQuote upsertLpBook(@RequestBody LpQuote body) {
        return quoteService.upsertLpBook(body);
    }

    @DeleteMapping("/lp-book/{ccyPair}/{lpId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLpBook(
            @PathVariable("ccyPair") String ccyPair, @PathVariable("lpId") String lpId) {
        quoteService.deleteLpBook(ccyPair, lpId);
    }

    @PutMapping("/macro-flag")
    public MacroFlagResponse putMacroFlag(@RequestBody(required = false) MacroFlagRequest request) {
        return quoteService.ingestMacro(request);
    }

    @DeleteMapping("/macro-flag")
    public MacroFlagResponse clearMacroFlag() {
        return quoteService.clearMacro();
    }

    @GetMapping("/macro-flag")
    public MacroFlagResponse getMacroFlag() {
        return quoteService.macroStatus();
    }

    /** Published FOMC/NFP/ECB windows — visible in Admin Policy Studio / BPMN scan task. */
    @GetMapping("/economic-calendar")
    public List<EconomicCalendarEventDto> economicCalendar() {
        return quoteService.economicCalendar();
    }

    @PutMapping("/economic-calendar")
    public EconomicCalendarEventDto upsertEconomicCalendar(@RequestBody EconomicCalendarEventDto body) {
        return quoteService.upsertEconomicCalendar(body);
    }

    @DeleteMapping("/economic-calendar/{source}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEconomicCalendar(
            @PathVariable("source") String source, @RequestParam("start") Instant start) {
        quoteService.deleteEconomicCalendar(source, start);
    }
}
