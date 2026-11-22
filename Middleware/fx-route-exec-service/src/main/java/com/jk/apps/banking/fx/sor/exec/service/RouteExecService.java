package com.jk.apps.banking.fx.sor.exec.service;

import com.jk.apps.banking.fx.sor.exec.cache.IdempotencyRecord;
import com.jk.apps.banking.fx.sor.exec.cache.IdempotencyStore;
import com.jk.apps.banking.fx.sor.exec.dto.ChildSplitDto;
import com.jk.apps.banking.fx.sor.exec.dto.FailoverRequest;
import com.jk.apps.banking.fx.sor.exec.dto.FailoverResponse;
import com.jk.apps.banking.fx.sor.exec.dto.OrderTraceResponse;
import com.jk.apps.banking.fx.sor.exec.dto.RouteExecuteRequest;
import com.jk.apps.banking.fx.sor.exec.dto.RouteExecuteResponse;
import com.jk.apps.banking.fx.sor.exec.entity.ChildVenueSplit;
import com.jk.apps.banking.fx.sor.exec.entity.FxExecutionOrder;
import com.jk.apps.banking.fx.sor.exec.client.QuoteRegimeClient;
import com.jk.apps.banking.fx.sor.exec.eventing.DomainEventPublisher;
import com.jk.apps.banking.fx.sor.exec.metrics.ServiceMetrics;
import com.jk.apps.banking.fx.sor.exec.regime.LastLookPenaltyHintCache;
import com.jk.apps.banking.fx.sor.exec.repository.ChildVenueSplitRepository;
import com.jk.apps.banking.fx.sor.exec.repository.FxExecutionOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RouteExecService {

    private static final List<String> RANKED_LPS = List.of("LP_CITI", "LP_JPM", "LP_GS");
    private static final String RULE_VERSION = "LP_SCORE_v1.0";
    private static final BigDecimal INTERNALIZE_THRESHOLD = new BigDecimal("1000000");
    private static final String TOPIC_ORDER_ROUTED = "fx.exec.order-routed-provenance.v1";
    private static final String TOPIC_LAST_LOOK_FAILOVER = "fx.exec.last-look-failover.v1";

    private final FxExecutionOrderRepository orderRepository;
    private final ChildVenueSplitRepository splitRepository;
    private final IdempotencyStore idempotencyStore;
    private final ServiceMetrics metrics;
    private final DomainEventPublisher domainEvents;
    private final QuoteRegimeClient quoteRegimeClient;
    private final LastLookPenaltyHintCache lastLookPenaltyHintCache;

    public RouteExecService(
            FxExecutionOrderRepository orderRepository,
            ChildVenueSplitRepository splitRepository,
            IdempotencyStore idempotencyStore,
            ServiceMetrics metrics,
            DomainEventPublisher domainEvents,
            QuoteRegimeClient quoteRegimeClient,
            LastLookPenaltyHintCache lastLookPenaltyHintCache) {
        this.orderRepository = orderRepository;
        this.splitRepository = splitRepository;
        this.idempotencyStore = idempotencyStore;
        this.metrics = metrics;
        this.domainEvents = domainEvents;
        this.quoteRegimeClient = quoteRegimeClient;
        this.lastLookPenaltyHintCache = lastLookPenaltyHintCache;
    }

    @Transactional
    public RouteExecuteResponse routeExecute(RouteExecuteRequest request) {
        metrics.ordersRouteExecute().increment();
        String clientOrderId = request.getClientOrderId();
        if (clientOrderId == null || clientOrderId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientOrderId required");
        }

        UUID provisionalId = UUID.randomUUID();
        Optional<IdempotencyRecord> existing =
                idempotencyStore.tryAcquire(clientOrderId, new IdempotencyRecord("IN_FLIGHT", provisionalId, null));
        if (existing.isPresent()) {
            return replay(existing.get(), clientOrderId);
        }

        LocalDate tradeDate = LocalDate.now();
        String pair = normalizePair(request.getCcyPair());
        String side = request.getSide() == null ? "BUY" : request.getSide().toUpperCase(Locale.ROOT);
        BigDecimal notional = request.getNotional() == null ? BigDecimal.ZERO : request.getNotional();
        BigDecimal maxSlip = request.getMaxSlippageBps() == null
                ? new BigDecimal("2.0000")
                : request.getMaxSlippageBps();

        BigDecimal mid = new BigDecimal("1.08500");
        BigDecimal slippage = maxSlip.min(new BigDecimal("0.8500"));
        BigDecimal adj = mid.multiply(slippage).divide(new BigDecimal("10000"), 8, RoundingMode.HALF_UP);
        BigDecimal rate = "SELL".equals(side) ? mid.subtract(adj) : mid.add(adj);

        // FR-01: resolve regime — request fields > Kafka hint cache > quote HTTP
        BigDecimal penalty = request.getLastLookPenaltyWeight();
        String sessionRegime = request.getSessionRegime();
        if (penalty == null || sessionRegime == null || sessionRegime.isBlank()) {
            var hint = lastLookPenaltyHintCache.current();
            if (hint.isPresent() && (penalty == null || sessionRegime == null || sessionRegime.isBlank())) {
                if (sessionRegime == null || sessionRegime.isBlank()) {
                    sessionRegime = hint.get().sessionRegime();
                }
                if (penalty == null) {
                    penalty = hint.get().lastLookPenaltyWeight();
                }
            }
        }
        if (penalty == null || sessionRegime == null || sessionRegime.isBlank()) {
            QuoteRegimeClient.RegimeHint fromQuote = quoteRegimeClient.fetch("TIER_2", pair);
            if (sessionRegime == null || sessionRegime.isBlank()) {
                sessionRegime = fromQuote.sessionRegime();
            }
            if (penalty == null) {
                penalty = fromQuote.lastLookPenaltyWeight();
            }
        }
        if (penalty == null) {
            penalty = BigDecimal.ONE;
        }
        if (sessionRegime == null || sessionRegime.isBlank()) {
            sessionRegime = "LONDON_NY_OVERLAP";
        }
        boolean stressedRegime = penalty.compareTo(BigDecimal.ONE) > 0
                || "NY_ROLLOVER".equals(sessionRegime)
                || "MACRO_NEWS_HIGH".equals(sessionRegime);

        boolean internalize = notional.compareTo(INTERNALIZE_THRESHOLD) <= 0;
        String mode = internalize ? "INTERNALIZED" : (notional.compareTo(new BigDecimal("10000000")) > 0
                ? "MULTI_LP_SWEEP"
                : "SINGLE_LP");
        if (internalize) {
            metrics.ordersInternalized().increment();
        }

        FxExecutionOrder order = new FxExecutionOrder();
        order.setTradeDate(tradeDate);
        order.setOrderId(provisionalId);
        order.setClientOrderId(clientOrderId);
        order.setClientId(request.getClientId() == null ? "CORP_88" : request.getClientId());
        order.setCcyPair(pair);
        order.setSide(side);
        order.setRequestedNotional(notional);
        order.setExecutedRate(rate);
        order.setSlippageBps(slippage);
        order.setExecutionMode(mode);
        order.setStatus("FILLED");
        order.setCreatedAt(Instant.now());
        orderRepository.save(order);

        List<ChildVenueSplit> splits = buildSplits(tradeDate, provisionalId, notional, rate, mode, penalty, stressedRegime);
        splitRepository.saveAll(splits);

        idempotencyStore.complete(clientOrderId, new IdempotencyRecord("FILLED", provisionalId, rate));
        metrics.auditEventsPublished().increment();
        emitOrderRoutedProvenance(order, splits, internalize, sessionRegime, penalty);

        RouteExecuteResponse response = toExecuteResponse(order, splits, false);
        response.setSessionRegime(sessionRegime);
        response.setLastLookPenaltyWeight(penalty);
        return response;
    }

    @Transactional
    public FailoverResponse failover(UUID orderId, FailoverRequest request) {
        metrics.ordersFailover().increment();
        FxExecutionOrder order = orderRepository
                .findFirstByOrderId(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));

        List<ChildVenueSplit> existing = splitRepository.findByOrderId(orderId);
        for (ChildVenueSplit split : existing) {
            if (split.getLpId().equals(request.getRejectedLpId())) {
                split.setLpStatus("LAST_LOOK_REJECTED");
            }
        }
        splitRepository.saveAll(existing);

        String backupLp = RANKED_LPS.stream()
                .filter(lp -> !lp.equals(request.getRejectedLpId()))
                .findFirst()
                .orElse("LP_GS");

        ChildVenueSplit reroute = new ChildVenueSplit();
        reroute.setTradeDate(order.getTradeDate());
        reroute.setSplitId(UUID.randomUUID());
        reroute.setOrderId(orderId);
        reroute.setLpId(backupLp);
        reroute.setAllocatedNotional(
                request.getUnfilledNotional() == null ? BigDecimal.ZERO : request.getUnfilledNotional());
        reroute.setFillRate(order.getExecutedRate());
        reroute.setHoldLatencyMs(18);
        reroute.setLpStatus("ACCEPTED");
        splitRepository.save(reroute);

        order.setStatus("PARTIAL_FILLED".equals(order.getStatus()) ? "FILLED" : "FILLED");
        order.setExecutionMode("MULTI_LP_SWEEP");
        orderRepository.save(order);

        emitLastLookFailover(orderId, request, backupLp, reroute);

        FailoverResponse response = new FailoverResponse();
        response.setOrderId(orderId);
        response.setStatus(order.getStatus());
        response.getReroutedSplits().add(toDto(reroute));
        return response;
    }

    @Transactional(readOnly = true)
    public OrderTraceResponse trace(String clientOrderId) {
        FxExecutionOrder order = orderRepository
                .findFirstByClientOrderIdOrderByCreatedAtDesc(clientOrderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "order not found"));
        List<ChildVenueSplit> splits = splitRepository.findByOrderId(order.getOrderId());

        OrderTraceResponse trace = new OrderTraceResponse();
        trace.setClientOrderId(clientOrderId);
        trace.setOrderId(order.getOrderId());
        trace.setStatus(order.getStatus());
        trace.setExecutionMode(order.getExecutionMode());
        trace.setExecutedRate(order.getExecutedRate());
        trace.setSlippageBps(order.getSlippageBps());
        trace.setDroolsRuleVersion(RULE_VERSION);
        trace.setEvaluatedLps(new ArrayList<>(RANKED_LPS));
        trace.setKnockoutCodes(List.of("STALE_QUOTE_OK", "CREDIT_OK", "CLIP_OK"));
        for (ChildVenueSplit split : splits) {
            trace.getChildSplits().add(toDto(split));
        }
        return trace;
    }

    private RouteExecuteResponse replay(IdempotencyRecord record, String clientOrderId) {
        Optional<FxExecutionOrder> order = orderRepository.findFirstByOrderId(record.getOrderId());
        if (order.isEmpty()) {
            order = orderRepository.findFirstByClientOrderIdOrderByCreatedAtDesc(clientOrderId);
        }
        if (order.isEmpty()) {
            RouteExecuteResponse pending = new RouteExecuteResponse();
            pending.setOrderId(record.getOrderId());
            pending.setClientOrderId(clientOrderId);
            pending.setStatus(record.getState());
            pending.setIdempotentReplay(true);
            return pending;
        }
        List<ChildVenueSplit> splits = splitRepository.findByOrderId(order.get().getOrderId());
        return toExecuteResponse(order.get(), splits, true);
    }

    private List<ChildVenueSplit> buildSplits(
            LocalDate tradeDate,
            UUID orderId,
            BigDecimal notional,
            BigDecimal rate,
            String mode,
            BigDecimal lastLookPenaltyWeight,
            boolean stressedRegime) {
        List<ChildVenueSplit> splits = new ArrayList<>();
        // Under stressed regimes, penalize slower last-look holds (scale hold latency for scoring evidence)
        int holdScale = stressedRegime
                ? Math.max(1, lastLookPenaltyWeight.multiply(new BigDecimal("10")).intValue())
                : 0;
        if ("INTERNALIZED".equals(mode)) {
            ChildVenueSplit split = baseSplit(tradeDate, orderId, "INTERNAL_BOOK", notional, rate, 2);
            splits.add(split);
            return splits;
        }
        if ("MULTI_LP_SWEEP".equals(mode)) {
            BigDecimal half = notional.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            // Prefer CITI then JPM; inflate hold on secondary under stress to encode last-look penalty
            splits.add(baseSplit(tradeDate, orderId, RANKED_LPS.get(0), half, rate, 12));
            splits.add(baseSplit(
                    tradeDate, orderId, RANKED_LPS.get(1), notional.subtract(half), rate, 15 + holdScale));
            return splits;
        }
        splits.add(baseSplit(tradeDate, orderId, RANKED_LPS.getFirst(), notional, rate, 10 + holdScale / 2));
        return splits;
    }

    private static ChildVenueSplit baseSplit(
            LocalDate tradeDate,
            UUID orderId,
            String lpId,
            BigDecimal notional,
            BigDecimal rate,
            int holdMs) {
        ChildVenueSplit split = new ChildVenueSplit();
        split.setTradeDate(tradeDate);
        split.setSplitId(UUID.randomUUID());
        split.setOrderId(orderId);
        split.setLpId(lpId);
        split.setAllocatedNotional(notional);
        split.setFillRate(rate);
        split.setHoldLatencyMs(holdMs);
        split.setLpStatus("ACCEPTED");
        return split;
    }

    private static RouteExecuteResponse toExecuteResponse(
            FxExecutionOrder order, List<ChildVenueSplit> splits, boolean replay) {
        RouteExecuteResponse response = new RouteExecuteResponse();
        response.setOrderId(order.getOrderId());
        response.setClientOrderId(order.getClientOrderId());
        response.setStatus(order.getStatus());
        response.setExecutionMode(order.getExecutionMode());
        response.setExecutedRate(order.getExecutedRate());
        response.setSlippageBps(order.getSlippageBps());
        response.setIdempotentReplay(replay);
        for (ChildVenueSplit split : splits) {
            response.getChildSplits().add(toDto(split));
        }
        return response;
    }

    private static ChildSplitDto toDto(ChildVenueSplit split) {
        return new ChildSplitDto(
                split.getSplitId(),
                split.getLpId(),
                split.getAllocatedNotional(),
                split.getFillRate(),
                split.getHoldLatencyMs(),
                split.getLpStatus());
    }

    private static String normalizePair(String ccyPair) {
        if (ccyPair == null || ccyPair.isBlank()) {
            return "EURUSD";
        }
        return ccyPair.replace("/", "").toUpperCase(Locale.ROOT);
    }

    private void emitOrderRoutedProvenance(
            FxExecutionOrder order,
            List<ChildVenueSplit> splits,
            boolean internalized,
            String sessionRegime,
            BigDecimal lastLookPenaltyWeight) {
        String winningLpId = splits.isEmpty() ? "INTERNAL_BOOK" : splits.getFirst().getLpId();
        List<Map<String, Object>> childSplits = new ArrayList<>();
        for (ChildVenueSplit split : splits) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("splitId", split.getSplitId().toString());
            row.put("lpId", split.getLpId());
            row.put("allocatedNotional", split.getAllocatedNotional());
            row.put("fillRate", split.getFillRate());
            row.put("holdLatencyMs", split.getHoldLatencyMs());
            row.put("lpStatus", split.getLpStatus());
            childSplits.add(row);
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("clientOrderId", order.getClientOrderId());
        payload.put("winningLpId", winningLpId);
        payload.put("internalized", internalized);
        payload.put("childSplits", childSplits);
        payload.put("slippageBps", order.getSlippageBps());
        payload.put("droolsRuleVersion", RULE_VERSION);
        payload.put("sessionRegime", sessionRegime);
        payload.put("lastLookPenaltyWeight", lastLookPenaltyWeight);
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_ORDER_ROUTED, payload);
    }

    private void emitLastLookFailover(
            UUID orderId, FailoverRequest request, String backupLpId, ChildVenueSplit reroute) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("orderId", orderId.toString());
        payload.put("rejectedLpId", request.getRejectedLpId());
        payload.put("holdDurationMs", reroute.getHoldLatencyMs());
        payload.put("backupLpId", backupLpId);
        payload.put("incrementalSlippageBps", new BigDecimal("0.2500"));
        payload.put("timestamp", Instant.now().toString());
        domainEvents.publish(TOPIC_LAST_LOOK_FAILOVER, payload);
    }
}
