package com.example.feedback.controller;

import com.example.feedback.dto.AdminFeedbackRequest;
import com.example.feedback.dto.FeedbackRequest;
import com.example.feedback.dto.FeedbackResponse;
import com.example.feedback.dto.MessageResponse;
import com.example.feedback.service.AdminFeedbackService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/feedback")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFeedbackController {

    private final AdminFeedbackService adminFeedbackService;

    public AdminFeedbackController(AdminFeedbackService adminFeedbackService) {
        this.adminFeedbackService = adminFeedbackService;
    }

    @GetMapping
    public ResponseEntity<List<FeedbackResponse>> all() {
        return ResponseEntity.ok(adminFeedbackService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<FeedbackResponse> one(@PathVariable Long id) {
        return ResponseEntity.ok(adminFeedbackService.findById(id));
    }

    @PostMapping
    public ResponseEntity<FeedbackResponse> create(@Valid @RequestBody AdminFeedbackRequest request) {
        FeedbackResponse response = adminFeedbackService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FeedbackResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackRequest request
    ) {
        return ResponseEntity.ok(adminFeedbackService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        adminFeedbackService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Feedback deleted successfully"));
    }
}
