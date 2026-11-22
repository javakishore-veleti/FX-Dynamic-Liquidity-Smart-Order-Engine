package com.jk.apps.banking.fx.sor.quote.business.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.quote.dto.CurrencyPairDto;
import com.jk.apps.banking.fx.sor.quote.entity.CurrencyPair;
import com.jk.apps.banking.fx.sor.quote.repository.CurrencyPairRepository;
import java.io.IOException;
import java.io.InputStream;
import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Currency pair master: loads the committed master file and serves pairs to the API. */
@Component
public class CurrencyPairFacade {

    static final String MASTER_RESOURCE = "master/currency-pairs.json";
    private static final Pattern CODE = Pattern.compile("[A-Z]{6}");

    private record Row(String pairCode, String baseCcy, String quoteCcy) {
    }

    private final CurrencyPairRepository repository;
    private final ObjectMapper mapper;
    private final Clock clock;

    public CurrencyPairFacade(CurrencyPairRepository repository, ObjectMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = Clock.systemUTC();
    }

    /** Loads the committed master file. Idempotent: existing pairs are updated in place. */
    @Transactional
    public int loadCommitted(String actor) {
        List<Row> rows = readMaster();
        java.time.Instant now = clock.instant();
        for (Row r : rows) {
            validate(r);
            repository.save(new CurrencyPair(r.pairCode(), r.baseCcy(), r.quoteCcy(), actor, now));
        }
        return rows.size();
    }

    @Transactional(readOnly = true)
    public List<CurrencyPairDto> list() {
        return repository.findAllByOrderByPairCodeAsc().stream().map(CurrencyPairFacade::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<CurrencyPairDto> find(String pairCode) {
        return repository.findById(pairCode).map(CurrencyPairFacade::toDto);
    }

    private static CurrencyPairDto toDto(CurrencyPair p) {
        return new CurrencyPairDto(p.getPairCode(), p.getBaseCcy(), p.getQuoteCcy());
    }

    private List<Row> readMaster() {
        try (InputStream in = new ClassPathResource(MASTER_RESOURCE).getInputStream()) {
            return mapper.readValue(in, new TypeReference<List<Row>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read " + MASTER_RESOURCE, ex);
        }
    }

    private static void validate(Row r) {
        if (r.pairCode() == null || !CODE.matcher(r.pairCode()).matches()) {
            throw new IllegalStateException("invalid pair code: " + r.pairCode());
        }
        if (!r.pairCode().equals(r.baseCcy() + r.quoteCcy())) {
            throw new IllegalStateException("pair code must equal base + quote: " + r.pairCode());
        }
    }
}
