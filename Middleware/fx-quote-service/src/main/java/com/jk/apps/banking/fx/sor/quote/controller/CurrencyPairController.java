package com.jk.apps.banking.fx.sor.quote.controller;

import com.jk.apps.banking.fx.sor.quote.business.facade.CurrencyPairFacade;
import com.jk.apps.banking.fx.sor.quote.dto.CurrencyPairDto;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/fx/pairs")
public class CurrencyPairController {

    private final CurrencyPairFacade pairs;

    public CurrencyPairController(CurrencyPairFacade pairs) {
        this.pairs = pairs;
    }

    @GetMapping
    public List<CurrencyPairDto> list() {
        return pairs.list();
    }

    @GetMapping("/{pairCode}")
    public CurrencyPairDto get(@PathVariable("pairCode") String pairCode) {
        return pairs.find(pairCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "unknown pair " + pairCode));
    }

    /** Called by the admin Initial Data workflow. Body may carry {"actor": "..."}. */
    @PostMapping("/load")
    public Map<String, Object> load(@RequestBody(required = false) Map<String, Object> body) {
        String actor = body == null || body.get("actor") == null ? "ADMIN_WORKFLOW" : String.valueOf(body.get("actor"));
        int loaded = pairs.loadCommitted(actor);
        return Map.of("loaded", loaded);
    }
}
