package com.kidcare.insight.repository;

import com.kidcare.insight.entity.Routine;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
    List<Routine> findByChildOrderByDayOfWeekAscTimeAsc(Child child);
    List<Routine> findByChildAndUser(Child child, User user);
}