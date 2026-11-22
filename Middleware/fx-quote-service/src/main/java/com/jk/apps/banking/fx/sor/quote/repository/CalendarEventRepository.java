package com.jk.apps.banking.fx.sor.quote.repository;

import com.jk.apps.banking.fx.sor.quote.entity.CalendarEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarEventRepository extends JpaRepository<CalendarEvent, String> {
}
