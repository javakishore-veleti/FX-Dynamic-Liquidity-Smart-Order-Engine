package com.jk.apps.banking.fx.sor.topology.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jk.apps.banking.fx.sor.topology.service.EdgeEnrich;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/** Loads the service catalog (committed master data) used to enrich topology edges on write (P9). */
@Configuration
public class CatalogConfig {

    static final String CATALOG = "master/service-catalog.json";

    private record Row(String service, String tier, String domain, String owner) {
    }

    @Bean
    public EdgeEnrich edgeEnrich(ObjectMapper mapper) {
        Map<String, EdgeEnrich.ServiceMeta> meta = new HashMap<>();
        for (Row r : read(mapper)) {
            meta.put(r.service(), new EdgeEnrich.ServiceMeta(r.tier(), r.domain(), r.owner()));
        }
        return new EdgeEnrich(meta);
    }

    private static List<Row> read(ObjectMapper mapper) {
        try (InputStream in = new ClassPathResource(CATALOG).getInputStream()) {
            return mapper.readValue(in, new TypeReference<List<Row>>() {
            });
        } catch (IOException ex) {
            throw new IllegalStateException("cannot read " + CATALOG, ex);
        }
    }
}
