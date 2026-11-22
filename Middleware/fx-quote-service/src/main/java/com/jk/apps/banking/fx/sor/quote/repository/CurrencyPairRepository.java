package com.jk.apps.banking.fx.sor.quote.repository;

import com.jk.apps.banking.fx.sor.quote.entity.CurrencyPair;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CurrencyPairRepository extends JpaRepository<CurrencyPair, String> {

    List<CurrencyPair> findAllByOrderByPairCodeAsc();
}
