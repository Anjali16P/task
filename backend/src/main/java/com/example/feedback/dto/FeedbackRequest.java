package com.example.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackRequest(
        @NotBlank(message = "Feedback message is required")
        @Size(max = 2000, message = "Feedback must be at most 2000 characters")
        String message
) {
}
