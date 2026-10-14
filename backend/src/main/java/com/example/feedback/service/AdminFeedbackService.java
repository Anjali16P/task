package com.example.feedback.service;

import com.example.feedback.dto.AdminFeedbackRequest;
import com.example.feedback.dto.FeedbackRequest;
import com.example.feedback.dto.FeedbackResponse;
import com.example.feedback.entity.Feedback;
import com.example.feedback.entity.User;
import com.example.feedback.exception.DuplicateResourceException;
import com.example.feedback.exception.ResourceNotFoundException;
import com.example.feedback.repository.FeedbackRepository;
import com.example.feedback.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminFeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public AdminFeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> findAll() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(FeedbackResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedbackResponse findById(Long id) {
        return feedbackRepository.findById(id)
                .map(FeedbackResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id " + id));
    }

    @Transactional
    public FeedbackResponse create(AdminFeedbackRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.userId()));
        if (feedbackRepository.existsByUserId(user.getId())) {
            throw new DuplicateResourceException("That user already has a feedback entry");
        }
        Feedback feedback = new Feedback(user, request.message().trim());
        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    @Transactional
    public FeedbackResponse update(Long id, FeedbackRequest request) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id " + id));
        feedback.setMessage(request.message().trim());
        feedback.setUpdatedAt(Instant.now());
        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }

    @Transactional
    public void delete(Long id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id " + id));
        feedbackRepository.delete(feedback);
    }
}
