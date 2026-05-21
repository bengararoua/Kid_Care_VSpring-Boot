package com.kidcare.insight.repository;

import com.kidcare.insight.entity.Appointment;
import com.kidcare.insight.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Query("SELECT a FROM Appointment a WHERE a.sender = :user OR a.receiver = :user ORDER BY a.scheduledAt DESC")
    List<Appointment> findByUser(@Param("user") User user);

    List<Appointment> findBySenderAndStatus(User sender, String status);
    List<Appointment> findByReceiverAndStatus(User receiver, String status);
}