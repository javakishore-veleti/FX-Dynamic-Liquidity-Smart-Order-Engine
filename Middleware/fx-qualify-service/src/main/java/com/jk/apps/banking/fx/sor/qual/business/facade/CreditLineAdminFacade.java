package com.jk.apps.banking.fx.sor.qual.business.facade;

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

    public CreditLineAdminFacade(CreditStore creditStore) {
        this.creditStore = creditStore;
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
