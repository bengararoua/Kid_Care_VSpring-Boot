package com.kidcare.insight.service;

import com.kidcare.insight.dto.ChildRequest;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.ChildRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ChildService {

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Child> getChildrenForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Child> children;

        if (user.isParent()) {
            children = childRepository.findByParent(user);
        } else if (user.isTeacher()) {
            children = childRepository.findByTeacher(user);
        } else if (user.isPsychologist()) {
            children = childRepository.findByPsychologist(user);
        } else {
            children = List.of();
        }

        // FORCER LE CHARGEMENT DES RELATIONS
        for (Child child : children) {
            // Initialiser les relations lazy
            if (child.getParent() != null) {
                child.getParent().getId();
                child.getParent().getName();
                child.getParent().getEmail();
            }
            if (child.getPsychologist() != null) {
                child.getPsychologist().getId();
                child.getPsychologist().getName();
                child.getPsychologist().getEmail();
            }
            if (child.getTeacher() != null) {
                child.getTeacher().getId();
                child.getTeacher().getName();
                child.getTeacher().getEmail();
            }
        }

        return children;
    }

    public Child getChildById(Long id, String email) {
        Child child = childRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        // FORCER LE CHARGEMENT DES RELATIONS
        if (child.getParent() != null) {
            child.getParent().getId();
            child.getParent().getName();
        }
        if (child.getPsychologist() != null) {
            child.getPsychologist().getId();
            child.getPsychologist().getName();
        }
        if (child.getTeacher() != null) {
            child.getTeacher().getId();
            child.getTeacher().getName();
        }

        if (email != null) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.isParent() && (child.getParent() == null || !child.getParent().getId().equals(user.getId()))) {
                throw new RuntimeException("Unauthorized");
            }
            if (user.isTeacher() && (child.getTeacher() == null || !child.getTeacher().getId().equals(user.getId()))) {
                throw new RuntimeException("Unauthorized");
            }
            if (user.isPsychologist() && (child.getPsychologist() == null || !child.getPsychologist().getId().equals(user.getId()))) {
                throw new RuntimeException("Unauthorized");
            }
        }

        return child;
    }

    @Transactional
    public Child createChild(ChildRequest req, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Child child = new Child();
        child.setName(req.getName());
        child.setAge(req.getAge());
        child.setNotes(req.getNotes());

        if (user.isParent()) {
            child.setParent(user);

            if (req.getPsychologistId() != null && req.getPsychologistId() > 0) {
                User psychologist = userRepository.findById(req.getPsychologistId())
                        .orElseThrow(() -> new RuntimeException("Psychologist not found"));
                child.setPsychologist(psychologist);
            }

            if (req.getTeacherId() != null && req.getTeacherId() > 0) {
                User teacher = userRepository.findById(req.getTeacherId())
                        .orElseThrow(() -> new RuntimeException("Teacher not found"));
                child.setTeacher(teacher);
            }
        }
        else if (user.isTeacher()) {
            child.setTeacher(user);

            if (req.getParentId() != null && req.getParentId() > 0) {
                User parent = userRepository.findById(req.getParentId())
                        .orElseThrow(() -> new RuntimeException("Parent not found"));
                child.setParent(parent);
            }

            if (req.getPsychologistId() != null && req.getPsychologistId() > 0) {
                User psychologist = userRepository.findById(req.getPsychologistId())
                        .orElseThrow(() -> new RuntimeException("Psychologist not found"));
                child.setPsychologist(psychologist);
            }
        }

        Child saved = childRepository.save(child);

        // FORCER LE CHARGEMENT APRÈS SAUVEGARDE
        if (saved.getParent() != null) {
            saved.getParent().getName();
        }
        if (saved.getPsychologist() != null) {
            saved.getPsychologist().getName();
        }
        if (saved.getTeacher() != null) {
            saved.getTeacher().getName();
        }

        return saved;
    }

    @Transactional
    public Child updateChild(Long id, ChildRequest req, String email) {
        Child child = getChildById(id, email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (req.getName() != null) child.setName(req.getName());
        if (req.getAge() != null) child.setAge(req.getAge());
        if (req.getNotes() != null) child.setNotes(req.getNotes());

        if ((user.isParent() || user.isTeacher()) && req.getPsychologistId() != null) {
            Long psychId = req.getPsychologistId();
            if (psychId != null && psychId > 0) {
                User psychologist = userRepository.findById(psychId)
                        .orElseThrow(() -> new RuntimeException("Psychologist not found"));
                child.setPsychologist(psychologist);
            } else {
                child.setPsychologist(null);
            }
        }

        if (user.isParent() && req.getTeacherId() != null) {
            Long teacherId = req.getTeacherId();
            if (teacherId != null && teacherId > 0) {
                User teacher = userRepository.findById(teacherId)
                        .orElseThrow(() -> new RuntimeException("Teacher not found"));
                child.setTeacher(teacher);
            } else {
                child.setTeacher(null);
            }
        }

        if (user.isTeacher() && req.getParentId() != null) {
            Long parentId = req.getParentId();
            if (parentId != null && parentId > 0) {
                User parent = userRepository.findById(parentId)
                        .orElseThrow(() -> new RuntimeException("Parent not found"));
                child.setParent(parent);
            } else {
                child.setParent(null);
            }
        }

        Child updated = childRepository.save(child);
        if (updated.getParent() != null) updated.getParent().getName();
        if (updated.getPsychologist() != null) updated.getPsychologist().getName();
        if (updated.getTeacher() != null) updated.getTeacher().getName();

        return updated;
    }

    @Transactional
    public void deleteChild(Long id, String email) {
        Child child = getChildById(id, email);
        childRepository.delete(child);
    }
}