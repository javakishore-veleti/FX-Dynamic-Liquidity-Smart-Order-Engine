package com.jk.apps.banking.fx.sor.qual.service;

import com.jk.apps.banking.fx.sor.qual.cache.CreditLine;
import com.jk.apps.banking.fx.sor.qual.cache.CreditStore;
import com.jk.apps.banking.fx.sor.qual.dto.CreditUtilizationResponse;
import com.jk.apps.banking.fx.sor.qual.dto.EligibleLp;
import com.jk.apps.banking.fx.sor.qual.dto.KnockoutCheckRequest;
import com.jk.apps.banking.fx.sor.qual.dto.KnockoutCheckResponse;
import com.jk.apps.banking.fx.sor.qual.dto.KnockoutReason;
import com.jk.apps.banking.fx.sor.qual.dto.QualifyOrderRequest;
import com.jk.apps.banking.fx.sor.qual.dto.QualifyOrderResponse;
import com.jk.apps.banking.fx.sor.qual.dto.VenueEligibility;
import com.jk.apps.banking.fx.sor.qual.entity.SettlementCutoffPolicy;
import com.jk.apps.banking.fx.sor.qual.eventing.DomainEventPublisher;
import com.jk.apps.banking.fx.sor.qual.metrics.ServiceMetrics;
import com.jk.apps.banking.fx.sor.qual.repository.SettlementCutoffPolicyRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class QualifyService {

    private static final List<String> LPS = List.of("LP_CITI", "LP_JPM", "LP_GS");
    private static final String DEFAULT_CLIENT = "CORP_88";
    private static final String TOPIC_VENUE_KNOCKOUT = "fx.qualify.venue-knockout.v1";
    private static final String TOPIC_CREDIT_BREACH = "fx.qualify.credit-threshold-breached.v1";
    private static final BigDecimal WARN_PCT = new BigDecimal("85");
    private static final BigDecimal HARD_PCT = new BigDecimal("95");

    private final SettlementCutoffPolicyRepository policyRepository;
    private final CreditStore creditStore;
    private final ServiceMetrics metrics;
    private final DomainEventPublisher domainEvents;

    public QualifyService(
            SettlementCutoffPolicyRepository policyRepository,
            CreditStore creditStore,
            ServiceMetrics metrics,
            DomainEventPublisher domainEvents) {
        this.policyRepository = policyRepository;
        this.creditStore = creditStore;
        this.metrics = metrics;
        this.domainEvents = domainEvents;
    }

    @PostConstruct
    void seedCreditLines() {
        long reset = Instant.now().plusSeconds(86_400).getEpochSecond();
        for (String lp : LPS) {
            creditStore.put(new CreditLine(
                    DEFAULT_CLIENT,
                    lp,
                    new BigDecimal("25000000"),
                    new BigDecimal("2500000"),
                    BigDecimal.ZERO,
                    reset));
        }
    }

    public QualifyOrderResponse qualify(QualifyOrderRequest request) {
        metrics.ordersQualifyRequests().increment();
        String pair = normalizePair(request.getCcyPair());
        String tenor = request.getTenor() == null ? "SPOT" : request.getTenor().toUpperCase(Locale.ROOT);
        String clientId = request.getClientId() == null ? DEFAULT_CLIENT : request.getClientId();
        BigDecimal notional = request.getNotionalUsd() == null ? BigDecimal.ZERO : request.getNotionalUsd();

        QualifyOrderResponse response = new QualifyOrderResponse();
        response.setClientOrderId(request.getClientOrderId());

        Optional<SettlementCutoffPolicy> policyOpt = policyRepository.findByCcyPairAndTenor(pair, tenor);
        if (policyOpt.isEmpty()) {
            for (String lp : LPS) {
                response.getKnockouts().add(new KnockoutReason(lp, "PAIR_TENOR_UNSUPPORTED",
                        "No settlement policy for " + pair + "/" + tenor));
                metrics.venuesKnockedOut().increment();
                emitVenueKnockout(request.getClientOrderId(), clientId, pair, "PAIR_TENOR_UNSUPPORTED", true);
            }
            response.setQualified(false);
            return response;
        }

        SettlementCutoffPolicy policy = policyOpt.get();
        String policyFail = policyFailCode(policy, notional);
        if (policyFail != null) {
            boolean escalate = "RESTRICTED_CCY".equals(policyFail);
            for (String lp : LPS) {
                response.getKnockouts().add(new KnockoutReason(lp, policyFail, "Policy knock-out: " + policyFail));
                metrics.venuesKnockedOut().increment();
                emitVenueKnockout(request.getClientOrderId(), clientId, pair, policyFail, escalate);
            }
            response.setQualified(false);
            return response;
        }

        String reservationToken = "CR-" + UUID.randomUUID();
        int rank = 1;
        for (String lp : LPS) {
            ensureCreditLine(clientId, lp);
            if (!creditStore.tryReserve(clientId, lp, notional)) {
                response.getKnockouts().add(new KnockoutReason(lp, "INSUFFICIENT_CREDIT",
                        "Bilateral credit / NOP headroom exhausted"));
                metrics.venuesKnockedOut().increment();
                emitVenueKnockout(request.getClientOrderId(), clientId, pair, "INSUFFICIENT_CREDIT", false);
                maybeEmitCreditBreach(clientId, lp);
                continue;
            }
            metrics.creditReservations().increment();
            CreditLine line = creditStore.get(clientId, lp).orElseThrow();
            maybeEmitCreditBreach(clientId, lp);
            response.getEligibleLps().add(new EligibleLp(lp, rank++, line.headroom(), reservationToken));
        }

        response.setQualified(!response.getEligibleLps().isEmpty());
        if (response.isQualified()) {
            response.setCreditReservationToken(reservationToken);
        }
        return response;
    }

    public CreditUtilizationResponse creditUtilization(String clientId, String counterpartyLpId) {
        String lpId = counterpartyLpId == null ? "LP_CITI" : counterpartyLpId;
        ensureCreditLine(clientId, lpId);
        CreditLine line = creditStore.get(clientId, lpId).orElseThrow();
        maybeEmitCreditBreach(clientId, lpId);
        return new CreditUtilizationResponse(
                clientId,
                lpId,
                line.getMaxCreditUsd(),
                line.getUtilizedUsd(),
                line.getReservedUsd(),
                line.headroom(),
                line.getResetEpoch());
    }

    public KnockoutCheckResponse knockoutCheck(KnockoutCheckRequest request) {
        String pair = normalizePair(request.getCcyPair());
        String tenor = request.getTenor() == null ? "SPOT" : request.getTenor().toUpperCase(Locale.ROOT);
        BigDecimal notional = request.getNotionalUsd() == null ? BigDecimal.ZERO : request.getNotionalUsd();

        KnockoutCheckResponse response = new KnockoutCheckResponse();
        response.setCcyPair(pair);
        response.setTenor(tenor);

        Optional<SettlementCutoffPolicy> policyOpt = policyRepository.findByCcyPairAndTenor(pair, tenor);
        for (String lp : LPS) {
            if (policyOpt.isEmpty()) {
                response.getVenues().add(new VenueEligibility(lp, false, "PAIR_TENOR_UNSUPPORTED"));
                metrics.venuesKnockedOut().increment();
                continue;
            }
            String fail = policyFailCode(policyOpt.get(), notional);
            if (fail != null) {
                response.getVenues().add(new VenueEligibility(lp, false, fail));
                metrics.venuesKnockedOut().increment();
            } else {
                response.getVenues().add(new VenueEligibility(lp, true, "PASS"));
            }
        }
        return response;
    }

    private void ensureCreditLine(String clientId, String lpId) {
        if (creditStore.get(clientId, lpId).isEmpty()) {
            creditStore.put(new CreditLine(
                    clientId,
                    lpId,
                    new BigDecimal("10000000"),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    Instant.now().plusSeconds(86_400).getEpochSecond()));
        }
    }

    private static String policyFailCode(SettlementCutoffPolicy policy, BigDecimal notional) {
        if (policy.isRestrictedFlag()) {
            return "RESTRICTED_CCY";
        }
        if (notional.compareTo(policy.getMinClipUsd()) < 0) {
            return "BELOW_MIN_CLIP";
        }
        if (notional.compareTo(policy.getMaxClipUsd()) > 0) {
            return "ABOVE_MAX_CLIP";
        }
        if (policy.isClsEligible()) {
            LocalTime nowUtc = LocalTime.now(ZoneOffset.UTC);
            if (nowUtc.isAfter(policy.getCutoffTimeUtc())) {
                return "PAST_CLS_CUTOFF";
            }
        }
        return null;
    }

    private static String normalizePair(String ccyPair) {
        if (ccyPair == null || ccyPair.isBlank()) {
            return "EURUSD";
        }
        return ccyPair.replace("/", "").toUpperCase(Locale.ROOT);
    }

    private void emitVenueKnockout(
            String clientOrderId,
            String clientId,
            String ccyPair,
            String knockoutRuleCode,
            boolean escalatedToDesk) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("clientOrderId", clientOrderId);
        payload.put("clientId", clientId);
        payload.put("ccyPair", ccyPair);
        payload.put("knockoutRuleCode", knockoutRuleCode);
        payload.put("escalatedToDesk", escalatedToDesk);
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_VENUE_KNOCKOUT, payload);
    }

    private void maybeEmitCreditBreach(String clientId, String lpId) {
        CreditLine line = creditStore.get(clientId, lpId).orElse(null);
        if (line == null || line.getMaxCreditUsd().signum() <= 0) {
            return;
        }
        BigDecimal used = line.getUtilizedUsd().add(line.getReservedUsd());
        BigDecimal pct = used
                .multiply(new BigDecimal("100"))
                .divide(line.getMaxCreditUsd(), 2, RoundingMode.HALF_UP);
        if (pct.compareTo(WARN_PCT) < 0) {
            return;
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("clientId", clientId);
        payload.put("lpId", lpId);
        payload.put("utilizedUsd", used);
        payload.put("maxLimitUsd", line.getMaxCreditUsd());
        payload.put("utilizationPct", pct);
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_CREDIT_BREACH, payload);
    }
}
