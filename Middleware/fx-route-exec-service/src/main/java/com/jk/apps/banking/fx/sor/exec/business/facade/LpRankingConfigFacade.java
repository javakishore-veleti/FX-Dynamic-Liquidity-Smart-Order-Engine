package com.jk.apps.banking.fx.sor.exec.business.facade;

import com.jk.apps.banking.fx.sor.exec.dto.LpRankingEntryDto;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** CLOSED — seeded LP ranking config visible/editable via Admin Reference Data. */
@Component
public class LpRankingConfigFacade {

    public static final String DEFAULT_RULE_VERSION = "LP_SCORE_v1.0";

    private final CopyOnWriteArrayList<LpRankingEntryDto> rankings = new CopyOnWriteArrayList<>();

    private final com.fasterxml.jackson.databind.ObjectMapper mapper;

    public LpRankingConfigFacade(com.fasterxml.jackson.databind.ObjectMapper mapper) {
        this.mapper = mapper;
        rankings.add(new LpRankingEntryDto("LP_CITI", 1, DEFAULT_RULE_VERSION, true));
        rankings.add(new LpRankingEntryDto("LP_JPM", 2, DEFAULT_RULE_VERSION, true));
        rankings.add(new LpRankingEntryDto("LP_GS", 3, DEFAULT_RULE_VERSION, true));
    }

    /** Loads the committed ranking starter (master/lp-rankings.json). Idempotent. Admin workflow only. */
    public int loadCommitted() {
        List<LpRankingEntryDto> rows;
        try (java.io.InputStream in = new org.springframework.core.io.ClassPathResource("master/lp-rankings.json").getInputStream()) {
            rows = mapper.readValue(in, new com.fasterxml.jackson.core.type.TypeReference<List<LpRankingEntryDto>>() {
            });
        } catch (java.io.IOException ex) {
            throw new IllegalStateException("cannot read master/lp-rankings.json", ex);
        }
        replaceAll(rows);
        return rows.size();
    }

    public List<LpRankingEntryDto> listAll() {
        return rankings.stream()
                .sorted(Comparator.comparingInt(LpRankingEntryDto::getRank).thenComparing(LpRankingEntryDto::getLpId))
                .map(this::copy)
                .toList();
    }

    public List<String> rankedEnabledLpIds() {
        return rankings.stream()
                .filter(LpRankingEntryDto::isEnabled)
                .sorted(Comparator.comparingInt(LpRankingEntryDto::getRank))
                .map(LpRankingEntryDto::getLpId)
                .toList();
    }

    public synchronized LpRankingEntryDto upsert(LpRankingEntryDto request) {
        if (request.getLpId() == null || request.getLpId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "lpId required");
        }
        String lp = request.getLpId().trim().toUpperCase(Locale.ROOT);
        rankings.removeIf(r -> r.getLpId().equals(lp));
        LpRankingEntryDto next = new LpRankingEntryDto(
                lp,
                request.getRank() <= 0 ? rankings.size() + 1 : request.getRank(),
                request.getRuleVersion() == null || request.getRuleVersion().isBlank()
                        ? DEFAULT_RULE_VERSION
                        : request.getRuleVersion(),
                request.isEnabled());
        rankings.add(next);
        return copy(next);
    }

    public synchronized void delete(String lpId) {
        String lp = lpId.trim().toUpperCase(Locale.ROOT);
        boolean removed = rankings.removeIf(r -> r.getLpId().equals(lp));
        if (!removed) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "lp ranking not found");
        }
    }

    public synchronized void replaceAll(List<LpRankingEntryDto> body) {
        if (body == null || body.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one LP required");
        }
        List<LpRankingEntryDto> next = new ArrayList<>();
        int i = 1;
        for (LpRankingEntryDto row : body) {
            if (row.getLpId() == null || row.getLpId().isBlank()) {
                continue;
            }
            next.add(new LpRankingEntryDto(
                    row.getLpId().trim().toUpperCase(Locale.ROOT),
                    row.getRank() <= 0 ? i : row.getRank(),
                    row.getRuleVersion() == null || row.getRuleVersion().isBlank()
                            ? DEFAULT_RULE_VERSION
                            : row.getRuleVersion(),
                    row.isEnabled()));
            i++;
        }
        if (next.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "at least one LP required");
        }
        rankings.clear();
        rankings.addAll(next);
    }

    private LpRankingEntryDto copy(LpRankingEntryDto src) {
        return new LpRankingEntryDto(src.getLpId(), src.getRank(), src.getRuleVersion(), src.isEnabled());
    }
}
