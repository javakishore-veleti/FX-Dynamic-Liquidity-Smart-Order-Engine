package com.jk.apps.banking.fx.sor.exec.repository;

import com.jk.apps.banking.fx.sor.exec.entity.ChildVenueSplit;
import com.jk.apps.banking.fx.sor.exec.entity.ChildVenueSplitId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChildVenueSplitRepository extends JpaRepository<ChildVenueSplit, ChildVenueSplitId> {

    List<ChildVenueSplit> findByOrderId(UUID orderId);
}
