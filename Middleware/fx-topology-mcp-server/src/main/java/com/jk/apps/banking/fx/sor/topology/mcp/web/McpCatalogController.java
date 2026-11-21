package com.jk.apps.banking.fx.sor.topology.mcp.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jk.apps.banking.fx.sor.topology.mcp.dto.McpToolDtos.McpCatalogDto;
import com.jk.apps.banking.fx.sor.topology.mcp.service.McpToolCatalog;

@RestController
@RequestMapping("/mcp")
public class McpCatalogController {

    @GetMapping
    public McpCatalogDto catalog() {
        return McpToolCatalog.catalog();
    }
}
