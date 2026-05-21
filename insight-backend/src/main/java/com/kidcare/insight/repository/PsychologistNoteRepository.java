package com.kidcare.insight.repository;

import com.kidcare.insight.entity.PsychologistNote;
import com.kidcare.insight.entity.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PsychologistNoteRepository extends JpaRepository<PsychologistNote, Long> {
    List<PsychologistNote> findByChildOrderBySessionDateDesc(Child child);
}