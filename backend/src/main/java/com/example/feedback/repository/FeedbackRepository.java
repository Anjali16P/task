package com.example.feedback.repository;

import com.example.feedback.entity.Feedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    Optional<Feedback> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Feedback> findAllByOrderByCreatedAtDesc();
}
