package com.kidcare.insight.service;

import com.kidcare.insight.dto.NoteRequest;
import com.kidcare.insight.entity.*;
import com.kidcare.insight.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PsychologistNoteService {

    @Autowired
    private PsychologistNoteRepository noteRepository;
    
    @Autowired
    private ChildRepository childRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<PsychologistNote> getNotesForChild(Long childId) {
        Child child = childRepository.findById(childId).orElseThrow();
        return noteRepository.findByChildOrderBySessionDateDesc(child);
    }

    public PsychologistNote addNote(Long childId, NoteRequest req, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        if (!currentUser.isPsychologist()) {
            throw new RuntimeException("Only psychologists can add notes");
        }
        Child child = childRepository.findById(childId).orElseThrow();
        
        PsychologistNote note = new PsychologistNote();
        note.setChild(child);
        note.setPsychologist(currentUser);
        note.setNote(req.getNote());
        note.setSessionDate(req.getSessionDate());
        
        return noteRepository.save(note);
    }
}