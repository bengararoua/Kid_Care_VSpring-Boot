package com.kidcare.insight.controller;

import com.kidcare.insight.dto.NoteRequest;
import com.kidcare.insight.entity.PsychologistNote;
import com.kidcare.insight.service.PsychologistNoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/children/{childId}/notes")
public class PsychologistNoteController {

    @Autowired
    private PsychologistNoteService noteService;

    @GetMapping
    public ResponseEntity<List<PsychologistNote>> getNotes(@PathVariable Long childId) {
        return ResponseEntity.ok(noteService.getNotesForChild(childId));
    }

    @PostMapping
    public ResponseEntity<PsychologistNote> addNote(@PathVariable Long childId, 
                                                     @RequestBody NoteRequest req,
                                                     @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(noteService.addNote(childId, req, user.getUsername()));
    }
}