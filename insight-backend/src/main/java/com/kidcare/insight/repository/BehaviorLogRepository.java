package com.kidcare.insight.repository;

import com.kidcare.insight.entity.BehaviorLog;
import com.kidcare.insight.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BehaviorLogRepository extends JpaRepository<BehaviorLog, Long> {

    List<BehaviorLog> findByChildOrderByLogDateDesc(Child child);

    @Query("SELECT b FROM BehaviorLog b WHERE b.child = :child ORDER BY b.logDate DESC LIMIT 7")
    List<BehaviorLog> findTop7ByChildOrderByLogDateDesc(@Param("child") Child child);
}