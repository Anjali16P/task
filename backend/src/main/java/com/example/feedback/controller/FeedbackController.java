package com.example.feedback.controller;

import com.example.feedback.dto.FeedbackRequest;
import com.example.feedback.dto.FeedbackResponse;
import com.example.feedback.security.AppUserDetails;
import com.example.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    /** Returns the authenticated user's feedback, or 204 if none exists yet. */
    @GetMapping("/my")
    public ResponseEntity<FeedbackResponse> myFeedback(@AuthenticationPrincipal AppUserDetails principal) {
        return feedbackService.getMyFeedback(principal.getId())
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /** Creates the single feedback entry for the authenticated user. */
    @PostMapping
    public ResponseEntity<FeedbackResponse> create(
            @AuthenticationPrincipal AppUserDetails principal,
            @Valid @RequestBody FeedbackRequest request
    ) {
        FeedbackResponse response = feedbackService.createFeedback(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** Updates the authenticated user's own feedback. */
    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> update(
            @AuthenticationPrincipal AppUserDetails principal,
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request
    ) {
        return ResponseEntity.ok(feedbackService.updateOwnFeedback(principal.getId(), id, request));
    }
}
