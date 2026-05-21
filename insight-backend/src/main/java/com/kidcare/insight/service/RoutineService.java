package com.kidcare.insight.service;

import com.kidcare.insight.entity.Routine;
import com.kidcare.insight.entity.Child;
import com.kidcare.insight.entity.User;
import com.kidcare.insight.repository.RoutineRepository;
import com.kidcare.insight.repository.ChildRepository;
import com.kidcare.insight.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
public class RoutineService {

    @Autowired
    private RoutineRepository routineRepository;

    @Autowired
    private ChildRepository childRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Routine> getRoutinesForChild(Long childId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));
        return routineRepository.findByChildAndUser(child, user);
    }

    @Transactional
    public Routine createRoutine(Long childId, Map<String, Object> data, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Child child = childRepository.findById(childId)
                .orElseThrow(() -> new RuntimeException("Child not found"));

        Routine routine = new Routine();
        routine.setChild(child);
        routine.setUser(user);
        routine.setDayOfWeek((String) data.get("day_of_week"));
        routine.setTime(LocalTime.parse((String) data.get("time")));
        routine.setActivity((String) data.get("activity"));
        routine.setDuration(data.get("duration") != null ? (Integer) data.get("duration") : null);
        routine.setCompleted(false);
        routine.setOrderIndex(0);

        return routineRepository.save(routine);
    }

    @Transactional
    public Routine updateRoutine(Long id, Map<String, Object> data, String email) {
        Routine routine = getRoutineById(id, email);

        if (data.containsKey("time")) {
            routine.setTime(LocalTime.parse((String) data.get("time")));
        }
        if (data.containsKey("activity")) {
            routine.setActivity((String) data.get("activity"));
        }
        if (data.containsKey("duration")) {
            routine.setDuration((Integer) data.get("duration"));
        }
        if (data.containsKey("completed")) {
            routine.setCompleted((Boolean) data.get("completed"));
        }

        return routineRepository.save(routine);
    }

    @Transactional
    public Routine toggleComplete(Long id, String email) {
        Routine routine = getRoutineById(id, email);
        routine.setCompleted(!routine.getCompleted());
        return routineRepository.save(routine);
    }

    @Transactional
    public void deleteRoutine(Long id, String email) {
        Routine routine = getRoutineById(id, email);
        routineRepository.delete(routine);
    }

    private Routine getRoutineById(Long id, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Routine routine = routineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Routine not found"));

        if (!routine.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        return routine;
    }
}