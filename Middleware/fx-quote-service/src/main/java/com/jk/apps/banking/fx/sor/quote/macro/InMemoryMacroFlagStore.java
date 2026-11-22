package com.jk.apps.banking.fx.sor.quote.macro;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class InMemoryMacroFlagStore implements MacroFlagStore {

    private final AtomicReference<MacroFlag> active = new AtomicReference<>();

    @Override
    public void save(MacroFlag flag) {
        active.set(flag);
    }

    @Override
    public void clear() {
        active.set(null);
    }

    @Override
    public Optional<MacroFlag> load() {
        return Optional.ofNullable(active.get());
    }
}
