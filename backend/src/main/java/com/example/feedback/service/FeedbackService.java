package com.example.feedback.service;

import com.example.feedback.dto.FeedbackRequest;
import com.example.feedback.dto.FeedbackResponse;
import com.example.feedback.entity.Feedback;
import com.example.feedback.entity.User;
import com.example.feedback.exception.DuplicateResourceException;
import com.example.feedback.exception.ResourceNotFoundException;
import com.example.feedback.repository.FeedbackRepository;
import com.example.feedback.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final UserRepository userRepository;

    public FeedbackService(FeedbackRepository feedbackRepository, UserRepository userRepository) {
        this.feedbackRepository = feedbackRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<FeedbackResponse> getMyFeedback(Long userId) {
        return feedbackRepository.findByUserId(userId).map(FeedbackResponse::from);
    }

    /**
     * Creates the single feedback entry allowed per user.
     * A second submission is rejected with 409 to enforce the one-per-user rule.
     */
    @Transactional
    public FeedbackResponse createFeedback(Long userId, FeedbackRequest request) {
        if (feedbackRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("You have already submitted feedback. Please edit your existing feedback instead.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User no longer exists"));
        Feedback feedback = new Feedback(user, request.message().trim());
        Feedback saved = feedbackRepository.save(feedback);
        return FeedbackResponse.from(saved);
    }

    /**
     * Updates feedback owned by the given user. Ownership is derived from the
     * authenticated principal, never from the request body.
     */
    @Transactional
    public FeedbackResponse updateOwnFeedback(Long userId, Long feedbackId, FeedbackRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback not found with id " + feedbackId));
        if (!feedback.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You can only modify your own feedback");
        }
        feedback.setMessage(request.message().trim());
        feedback.setUpdatedAt(Instant.now());
        return FeedbackResponse.from(feedbackRepository.save(feedback));
    }
}
