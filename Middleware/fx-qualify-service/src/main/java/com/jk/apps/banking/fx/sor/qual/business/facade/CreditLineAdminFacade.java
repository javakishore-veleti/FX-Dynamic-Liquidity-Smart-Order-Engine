package com.jk.apps.banking.fx.sor.qual.business.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.core.io.ClassPathResource;

import com.jk.apps.banking.fx.sor.qual.cache.CreditLine;
import com.jk.apps.banking.fx.sor.qual.cache.CreditStore;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** CLOSED — CRUD for bilateral credit lines (FR-02 seed / config). */
@Component
public class CreditLineAdminFacade {

    private final CreditStore creditStore;
    private final ObjectMapper mapper;

    private record SeedRow(String clientId, String lpId, BigDecimal maxCreditUsd, BigDecimal nopLimitUsd) {
    }

    public CreditLineAdminFacade(CreditStore creditStore, ObjectMapper mapper) {
        this.creditStore = creditStore;
        this.mapper = mapper;
    }

    /** Loads the committed credit-line seed. Idempotent. Called only by the admin Initial Data workflow. */
    public int loadCommitted() {
        List<SeedRow> rows;
        try (InputStream in = new ClassPathResource("master/credit-lines.json").getInputStream()) {
            rows = mapper.readValue(in, new TypeReference<List<SeedRow>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read master/credit-lines.json", ex);
        }
        long reset = Instant.now().plusSeconds(86_400).getEpochSecond();
        for (SeedRow r : rows) {
            creditStore.put(new CreditLine(r.clientId(), r.lpId(), r.maxCreditUsd(), r.nopLimitUsd(), BigDecimal.ZERO, reset));
        }
        return rows.size();
    }

    public List<CreditLine> listAll() {
        return creditStore.listAll().stream()
                .sorted(Comparator.comparing(CreditLine::getClientId).thenComparing(CreditLine::getLpId))
                .toList();
    }

    public CreditLine upsert(CreditLine request) {
        if (request.getClientId() == null || request.getLpId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "clientId and lpId required");
        }
        CreditLine line = new CreditLine(
                request.getClientId().trim().toUpperCase(Locale.ROOT),
                request.getLpId().trim().toUpperCase(Locale.ROOT),
                request.getMaxCreditUsd() == null ? new BigDecimal("25000000") : request.getMaxCreditUsd(),
                request.getUtilizedUsd() == null ? BigDecimal.ZERO : request.getUtilizedUsd(),
                request.getReservedUsd() == null ? BigDecimal.ZERO : request.getReservedUsd(),
                request.getResetEpoch() <= 0 ? Instant.now().plusSeconds(86_400).getEpochSecond() : request.getResetEpoch());
        creditStore.put(line);
        return line;
    }

    public void delete(String clientId, String lpId) {
        creditStore.delete(clientId.trim().toUpperCase(Locale.ROOT), lpId.trim().toUpperCase(Locale.ROOT));
    }
}
