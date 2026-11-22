package com.jk.apps.banking.fx.sor.quote.business.facade;

import com.jk.apps.banking.fx.sor.quote.cache.SpreadScheduleCache;
import com.jk.apps.banking.fx.sor.quote.dto.SpreadScheduleDto;
import com.jk.apps.banking.fx.sor.quote.entity.ClientTierSpreadSchedule;
import com.jk.apps.banking.fx.sor.quote.repository.ClientTierSpreadScheduleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * CLOSED business — maker-checker style spread schedule lifecycle (FR-01 / FR-05-aligned).
 */
@Component
public class SpreadScheduleAdminFacade {

    private final ClientTierSpreadScheduleRepository repository;
    private final SpreadScheduleCache scheduleCache;

    public SpreadScheduleAdminFacade(
            ClientTierSpreadScheduleRepository repository,
            SpreadScheduleCache scheduleCache) {
        this.repository = repository;
        this.scheduleCache = scheduleCache;
    }

    public List<SpreadScheduleDto> listAll() {
        return scheduleCache.listAll().stream()
                .sorted(Comparator
                        .comparing(ClientTierSpreadSchedule::getTierId)
                        .thenComparing(ClientTierSpreadSchedule::getCcyPair)
                        .thenComparing(ClientTierSpreadSchedule::getSessionRegime))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SpreadScheduleDto draft(SpreadScheduleDto request) {
        ClientTierSpreadSchedule row = upsertEntity(request, "DRAFT",
                request.getApprovedBy() == null ? "MAKER" : request.getApprovedBy());
        return toDto(row);
    }

    @Transactional
    public SpreadScheduleDto submitForChecker(SpreadScheduleDto request) {
        ClientTierSpreadSchedule row = upsertEntity(request, "PENDING_APPROVAL",
                request.getApprovedBy() == null ? "MAKER" : request.getApprovedBy());
        return toDto(row);
    }

    @Transactional
    public SpreadScheduleDto approve(SpreadScheduleDto request) {
        String checker = request.getApprovedBy() == null ? "CHECKER" : request.getApprovedBy();
        ClientTierSpreadSchedule row = upsertEntity(request, "ACTIVE", checker);
        scheduleCache.invalidate();
        return toDto(row);
    }

    public void invalidateCache() {
        scheduleCache.invalidate();
    }

    private ClientTierSpreadSchedule upsertEntity(SpreadScheduleDto request, String status, String approvedBy) {
        if (request.getTierId() == null || request.getCcyPair() == null || request.getSessionRegime() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tierId, ccyPair, sessionRegime required");
        }
        String tier = request.getTierId().trim().toUpperCase(Locale.ROOT);
        String pair = request.getCcyPair().replace("/", "").toUpperCase(Locale.ROOT);
        String regime = request.getSessionRegime().trim().toUpperCase(Locale.ROOT);

        ClientTierSpreadSchedule row = repository
                .findByTierIdAndCcyPairAndSessionRegime(tier, pair, regime)
                .orElseGet(ClientTierSpreadSchedule::new);
        row.setTierId(tier);
        row.setCcyPair(pair);
        row.setSessionRegime(regime);
        row.setMarkupBps(request.getMarkupBps() == null ? new BigDecimal("1.5000") : request.getMarkupBps());
        row.setNewsMultiplier(request.getNewsMultiplier() == null ? BigDecimal.ONE : request.getNewsMultiplier());
        row.setEffectiveFrom(request.getEffectiveFrom() == null ? Instant.now() : request.getEffectiveFrom());
        row.setApprovedBy(approvedBy);
        row.setStatus(status);
        ClientTierSpreadSchedule saved = repository.save(row);
        scheduleCache.put(saved);
        return saved;
    }

    private SpreadScheduleDto toDto(ClientTierSpreadSchedule row) {
        SpreadScheduleDto dto = new SpreadScheduleDto();
        dto.setTierId(row.getTierId());
        dto.setCcyPair(row.getCcyPair());
        dto.setSessionRegime(row.getSessionRegime());
        dto.setMarkupBps(row.getMarkupBps());
        dto.setNewsMultiplier(row.getNewsMultiplier());
        dto.setEffectiveFrom(row.getEffectiveFrom());
        dto.setApprovedBy(row.getApprovedBy());
        dto.setStatus(row.getStatus() == null ? "ACTIVE" : row.getStatus());
        return dto;
    }
}
