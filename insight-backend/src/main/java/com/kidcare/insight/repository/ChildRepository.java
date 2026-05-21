package com.kidcare.insight.repository;

import com.kidcare.insight.entity.Child;
import com.kidcare.insight.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChildRepository extends JpaRepository<Child, Long> {
    List<Child> findByParent(User parent);
    List<Child> findByPsychologist(User psychologist);
    List<Child> findByTeacher(User teacher);
}