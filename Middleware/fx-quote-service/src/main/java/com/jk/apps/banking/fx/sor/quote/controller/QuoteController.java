package com.jk.apps.banking.fx.sor.quote.controller;

import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteRequest;
import com.jk.apps.banking.fx.sor.quote.dto.IndicativeQuoteResponse;
import com.jk.apps.banking.fx.sor.quote.dto.QuoteTickEvent;
import com.jk.apps.banking.fx.sor.quote.dto.RateSnapshotResponse;
import com.jk.apps.banking.fx.sor.quote.service.QuoteService;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
            @RequestParam(required = false) String ccyPairs,
            @RequestParam(required = false) String clientTier,
            @RequestParam(required = false, defaultValue = "SPOT") String tenor) {
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
    public RateSnapshotResponse rates(@PathVariable String ccyPair) {
        return quoteService.rates(ccyPair);
    }
}
