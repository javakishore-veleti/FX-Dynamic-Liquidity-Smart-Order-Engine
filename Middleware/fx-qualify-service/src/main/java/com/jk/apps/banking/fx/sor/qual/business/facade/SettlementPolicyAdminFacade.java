package com.jk.apps.banking.fx.sor.qual.business.facade;

import com.jk.apps.banking.fx.sor.qual.dto.SettlementPolicyDto;
import com.jk.apps.banking.fx.sor.qual.entity.SettlementCutoffPolicy;
import com.jk.apps.banking.fx.sor.qual.entity.SettlementCutoffPolicyId;
import com.jk.apps.banking.fx.sor.qual.repository.SettlementCutoffPolicyRepository;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** CLOSED — CRUD for settlement cutoffs / clip / restricted config (FR-02/04 seed data). */
@Component
public class SettlementPolicyAdminFacade {

    private final SettlementCutoffPolicyRepository repository;

    public SettlementPolicyAdminFacade(SettlementCutoffPolicyRepository repository) {
        this.repository = repository;
    }

    public List<SettlementPolicyDto> listAll() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(SettlementCutoffPolicy::getCcyPair)
                        .thenComparing(SettlementCutoffPolicy::getTenor))
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public SettlementPolicyDto upsert(SettlementPolicyDto request) {
        if (request.getCcyPair() == null || request.getTenor() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ccyPair and tenor required");
        }
        String pair = request.getCcyPair().replace("/", "").toUpperCase(Locale.ROOT);
        String tenor = request.getTenor().trim().toUpperCase(Locale.ROOT);
        SettlementCutoffPolicy row = repository
                .findByCcyPairAndTenor(pair, tenor)
                .orElseGet(SettlementCutoffPolicy::new);
        row.setCcyPair(pair);
        row.setTenor(tenor);
        row.setClsEligible(request.isClsEligible());
        row.setCutoffTimeUtc(request.getCutoffTimeUtc() == null ? LocalTime.of(23, 55) : request.getCutoffTimeUtc());
        row.setMinClipUsd(request.getMinClipUsd() == null ? new BigDecimal("100000") : request.getMinClipUsd());
        row.setMaxClipUsd(request.getMaxClipUsd() == null ? new BigDecimal("50000000") : request.getMaxClipUsd());
        row.setRestrictedFlag(request.isRestrictedFlag());
        return toDto(repository.save(row));
    }

    @Transactional
    public void delete(String ccyPair, String tenor) {
        String pair = ccyPair.replace("/", "").toUpperCase(Locale.ROOT);
        String t = tenor.trim().toUpperCase(Locale.ROOT);
        SettlementCutoffPolicyId id = new SettlementCutoffPolicyId(pair, t);
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "policy not found");
        }
        repository.deleteById(id);
    }

    private SettlementPolicyDto toDto(SettlementCutoffPolicy row) {
        SettlementPolicyDto dto = new SettlementPolicyDto();
        dto.setCcyPair(row.getCcyPair());
        dto.setTenor(row.getTenor());
        dto.setClsEligible(row.isClsEligible());
        dto.setCutoffTimeUtc(row.getCutoffTimeUtc());
        dto.setMinClipUsd(row.getMinClipUsd());
        dto.setMaxClipUsd(row.getMaxClipUsd());
        dto.setRestrictedFlag(row.isRestrictedFlag());
        return dto;
    }
}
