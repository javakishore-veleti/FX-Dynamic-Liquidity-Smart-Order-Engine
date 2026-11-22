package com.jk.apps.banking.fx.sor.quote.grpc;

import com.jk.apps.banking.fx.sor.quote.controller.QuoteController;
import com.jk.apps.banking.fx.sor.quote.dto.EconomicCalendarEventDto;
import com.jk.apps.banking.fx.sor.quote.dto.MacroFlagResponse;
import com.jk.apps.banking.fx.sor.quote.dto.RegimeContextResponse;
import com.jk.apps.banking.fx.sor.quote.dto.SpreadScheduleDto;
import com.jk.apps.banking.fx.sor.quote.cache.LpQuote;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.CalendarEvent;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.CalendarEventList;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.Empty;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.LpQuoteList;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.MacroFlag;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.QuoteReadGrpc;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.RegimeContextReply;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.RegimeContextRequest;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.SpreadSchedule;
import com.jk.apps.banking.fx.sor.quote.grpc.v1.SpreadScheduleList;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.grpc.server.service.GrpcService;

/**
 * Read-only gRPC API for quote (fx.quote.v1.QuoteRead). Enabled by fx.features.grpc-enabled.
 * Delegates to the same controller reads that REST exposes, so both return the same data.
 */
@GrpcService
@ConditionalOnProperty(name = "fx.features.grpc-enabled", havingValue = "true")
public class QuoteReadServer extends QuoteReadGrpc.QuoteReadImplBase {

    private final QuoteController quote;

    public QuoteReadServer(QuoteController quote) {
        this.quote = quote;
    }

    @Override
    public void listSpreadSchedules(Empty r, StreamObserver<SpreadScheduleList> o) {
        run(o, () -> {
            var b = SpreadScheduleList.newBuilder();
            for (SpreadScheduleDto s : quote.listSchedules()) {
                b.addSchedules(SpreadSchedule.newBuilder()
                        .setTierId(nz(s.getTierId())).setCcyPair(nz(s.getCcyPair()))
                        .setSessionRegime(nz(s.getSessionRegime()))
                        .setMarkupBps(String.valueOf(s.getMarkupBps()))
                        .setNewsMultiplier(String.valueOf(s.getNewsMultiplier()))
                        .setEffectiveFrom(String.valueOf(s.getEffectiveFrom()))
                        .setApprovedBy(nz(s.getApprovedBy())).setStatus(nz(s.getStatus()))
                        .setPurposeNote(nz(s.getPurposeNote())));
            }
            return b.build();
        });
    }

    @Override
    public void listLpBook(Empty r, StreamObserver<LpQuoteList> o) {
        run(o, () -> {
            var b = LpQuoteList.newBuilder();
            for (LpQuote q : quote.listLpBook()) {
                b.addQuotes(com.jk.apps.banking.fx.sor.quote.grpc.v1.LpQuote.newBuilder()
                        .setCcyPair(nz(q.getCcyPair())).setLpId(nz(q.getLpId()))
                        .setBid(String.valueOf(q.getBid())).setAsk(String.valueOf(q.getAsk()))
                        .setAvailableSize(String.valueOf(q.getAvailableSize()))
                        .setQuoteTs(q.getQuoteTs()).setTtlMs(q.getTtlMs()));
            }
            return b.build();
        });
    }

    @Override
    public void listEconomicCalendar(Empty r, StreamObserver<CalendarEventList> o) {
        run(o, () -> {
            var b = CalendarEventList.newBuilder();
            for (EconomicCalendarEventDto e : quote.economicCalendar()) {
                b.addEvents(CalendarEvent.newBuilder()
                        .setSource(nz(e.getSource())).setSeverity(String.valueOf(e.getSeverity()))
                        .setStart(String.valueOf(e.getStart())).setEnd(String.valueOf(e.getEnd()))
                        .setLabel(nz(e.getLabel())).setActiveNow(e.isActiveNow()));
            }
            return b.build();
        });
    }

    @Override
    public void getMacroFlag(Empty r, StreamObserver<MacroFlag> o) {
        run(o, () -> {
            MacroFlagResponse m = quote.getMacroFlag();
            return MacroFlag.newBuilder()
                    .setActive(m.isActive()).setRegime(nz(m.getRegime())).setSource(nz(m.getSource()))
                    .setSeverity(String.valueOf(m.getSeverity())).setExpiresAt(String.valueOf(m.getExpiresAt()))
                    .setLastLookPenaltyWeight(String.valueOf(m.getLastLookPenaltyWeight()))
                    .setArmedBy(nz(m.getArmedBy())).build();
        });
    }

    @Override
    public void regimeContext(RegimeContextRequest r, StreamObserver<RegimeContextReply> o) {
        run(o, () -> {
            RegimeContextResponse c = quote.regimeContext(
                    r.getClientTier().isBlank() ? "TIER_2" : r.getClientTier(),
                    r.getCcyPair().isBlank() ? "EURUSD" : r.getCcyPair());
            return RegimeContextReply.newBuilder()
                    .setSessionRegime(nz(c.getSessionRegime())).setMacroActive(c.isMacroActive())
                    .setLastLookPenaltyWeight(String.valueOf(c.getLastLookPenaltyWeight()))
                    .setSampleMarkupBps(String.valueOf(c.getSampleMarkupBps())).build();
        });
    }

    private static <T> void run(StreamObserver<T> o, java.util.function.Supplier<T> call) {
        try {
            T reply = call.get();
            o.onNext(reply);
            o.onCompleted();
        } catch (IllegalArgumentException ex) {
            o.onError(Status.INVALID_ARGUMENT.withDescription(ex.getMessage()).asRuntimeException());
        } catch (Exception ex) {
            o.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
