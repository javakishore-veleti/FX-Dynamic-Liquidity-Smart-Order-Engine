package com.jk.apps.banking.fx.sor.admin.dto;

import java.util.List;

public class CacheInvalidateRequest {

    private List<String> cacheKeys;
    private String reason;

    public List<String> getCacheKeys() {
        return cacheKeys;
    }

    public void setCacheKeys(List<String> cacheKeys) {
        this.cacheKeys = cacheKeys;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
