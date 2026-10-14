package com.example.feedback.dto;

import com.example.feedback.entity.Feedback;
import java.time.Instant;

public record FeedbackResponse(
        Long id,
        Long userId,
        String username,
        String message,
        Instant createdAt,
        Instant updatedAt
) {
    public static FeedbackResponse from(Feedback feedback) {
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getUser().getId(),
                feedback.getUser().getUsername(),
                feedback.getMessage(),
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }
}
