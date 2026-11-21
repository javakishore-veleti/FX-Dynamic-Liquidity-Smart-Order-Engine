package com.jk.apps.banking.fx.sor.exec.repository;

import com.jk.apps.banking.fx.sor.exec.entity.FxExecutionOrder;
import com.jk.apps.banking.fx.sor.exec.entity.FxExecutionOrderId;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FxExecutionOrderRepository extends JpaRepository<FxExecutionOrder, FxExecutionOrderId> {

    Optional<FxExecutionOrder> findFirstByClientOrderIdOrderByCreatedAtDesc(String clientOrderId);

    Optional<FxExecutionOrder> findFirstByOrderId(UUID orderId);
}
