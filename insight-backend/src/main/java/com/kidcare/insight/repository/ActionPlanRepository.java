package com.kidcare.insight.repository;

import com.kidcare.insight.entity.ActionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ActionPlanRepository extends JpaRepository<ActionPlan, Long> {

    @Query("SELECT a FROM ActionPlan a WHERE a.child.id = :childId ORDER BY a.generatedDate DESC")
    ActionPlan findTopByChildIdOrderByGeneratedDateDesc(@Param("childId") Long childId);
}