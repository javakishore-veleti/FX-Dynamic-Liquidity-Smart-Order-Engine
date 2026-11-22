package com.jk.apps.banking.fx.sor.qual.business.facade;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * Currency pair master as known to qualify. Read from qualify's committed copy of the master file
 * (master data rule, DESIGN §9): no call to quote on the request path.
 */
@Component
public class PairMasterFacade {

    private final Set<String> codes;

    public PairMasterFacade(ObjectMapper mapper) {
        this.codes = load(mapper);
    }

    public boolean isKnown(String pairCode) {
        return pairCode != null && codes.contains(pairCode);
    }

    private static Set<String> load(ObjectMapper mapper) {
        try (InputStream in = new ClassPathResource("master/currency-pairs.json").getInputStream()) {
            List<Map<String, String>> rows = mapper.readValue(in, new TypeReference<>() {
            });
            return rows.stream().map(r -> r.get("pairCode")).collect(Collectors.toUnmodifiableSet());
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read master/currency-pairs.json", ex);
        }
    }
}
