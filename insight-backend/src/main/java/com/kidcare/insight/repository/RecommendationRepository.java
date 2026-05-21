package com.kidcare.insight.repository;

import com.kidcare.insight.entity.Recommendation;
import com.kidcare.insight.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    List<Recommendation> findByChildOrderByCreatedAtDesc(Child child);
    List<Recommendation> findByChildAndIsCompletedFalse(Child child);
}