package com.example.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Payload used by administrators when creating a feedback record.
 * The associated user must be supplied because feedback is tied to a user.
 */
public record AdminFeedbackRequest(
        @NotNull(message = "userId is required")
        Long userId,

        @NotBlank(message = "Feedback message is required")
        @Size(max = 2000, message = "Feedback must be at most 2000 characters")
        String message
) {
}
