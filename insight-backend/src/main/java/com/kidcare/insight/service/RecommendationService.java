package com.kidcare.insight.service;

import com.kidcare.insight.dto.RecommendationRequest;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.entity.Recommendation;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.ChildRepository;
import com.kidcare.insight.repository.RecommendationRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecommendationService {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Recommendation> getRecommendationsForChild(Long childId, String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        Child child = childRepository.findById(childId).orElseThrow();
        return recommendationRepository.findByChildOrderByCreatedAtDesc(child);
    }

    public Recommendation addRecommendation(Long childId, RecommendationRequest req, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        if (!currentUser.isPsychologist()) {
            throw new RuntimeException("Only psychologists can add recommendations");
        }
        Child child = childRepository.findById(childId).orElseThrow();
        Recommendation rec = new Recommendation();
        rec.setChild(child);
        rec.setTitle(req.getTitle());
        rec.setDescription(req.getDescription());
        rec.setCategory(req.getCategory());
        rec.setIsCompleted(false);
        return recommendationRepository.save(rec);
    }

    public Recommendation toggleComplete(Long recId, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        Recommendation rec = recommendationRepository.findById(recId).orElseThrow();
        if (currentUser.isParent() && rec.getChild().getParent().getId().equals(currentUser.getId())) {
            rec.setIsCompleted(!rec.getIsCompleted());
            return recommendationRepository.save(rec);
        } else if (currentUser.isPsychologist()) {
            rec.setIsCompleted(!rec.getIsCompleted());
            return recommendationRepository.save(rec);
        } else if (currentUser.isTeacher() && rec.getChild().getTeacher() != null && rec.getChild().getTeacher().getId().equals(currentUser.getId())) {
            rec.setIsCompleted(!rec.getIsCompleted());
            return recommendationRepository.save(rec);
        }
        throw new RuntimeException("Unauthorized");
    }

    public void deleteRecommendation(Long recId, String email) {
        User currentUser = userRepository.findByEmail(email).orElseThrow();
        if (!currentUser.isPsychologist()) {
            throw new RuntimeException("Only psychologists can delete recommendations");
        }
        recommendationRepository.deleteById(recId);
    }
}