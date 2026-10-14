package com.example.feedback;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeedbackFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String register(String username, String email, int expectedStatus) throws Exception {
        String body = """
                {"username":"%s","email":"%s","password":"Password@123","confirmPassword":"Password@123"}
                """.formatted(username, email);
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(expectedStatus));
        return body;
    }

    private String login(String username, String password, int expectedStatus) throws Exception {
        String body = """
                {"username":"%s","password":"%s"}
                """.formatted(username, password);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().is(expectedStatus))
                .andReturn();
        if (expectedStatus == 200) {
            JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
            return node.get("token").asText();
        }
        return null;
    }

    @Test
    void registrationValidationAndDuplicates() throws Exception {
        register("alice", "alice@example.com", 201);
        // duplicate username
        register("alice", "other@example.com", 409);
        // duplicate email
        register("alice2", "alice@example.com", 409);
        // invalid email + mismatch password
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"bob","email":"not-an-email","password":"Password@123","confirmPassword":"Different@123"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginAndAccessControl() throws Exception {
        register("carol", "carol@example.com", 201);
        login("carol", "wrong-password", 401);
        String token = login("carol", "Password@123", 200);

        // authenticated /me
        mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("carol"))
                .andExpect(jsonPath("$.role").value("USER"));

        // unauthenticated protected endpoint
        mockMvc.perform(get("/api/feedback/my")).andExpect(status().isUnauthorized());

        // regular user blocked from admin API
        mockMvc.perform(get("/api/admin/feedback").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void feedbackLifecycleAndOwnership() throws Exception {
        register("dave", "dave@example.com", 201);
        register("erin", "erin@example.com", 201);
        String daveToken = login("dave", "Password@123", 200);
        String erinToken = login("erin", "Password@123", 200);

        // no feedback yet -> 204
        mockMvc.perform(get("/api/feedback/my").header("Authorization", "Bearer " + daveToken))
                .andExpect(status().isNoContent());

        // empty feedback rejected
        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", "Bearer " + daveToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest());

        // create feedback
        MvcResult created = mockMvc.perform(post("/api/feedback")
                        .header("Authorization", "Bearer " + daveToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Great service!\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Great service!"))
                .andReturn();
        long feedbackId = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();

        // duplicate feedback rejected
        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", "Bearer " + daveToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Second entry\"}"))
                .andExpect(status().isConflict());

        // update own feedback
        mockMvc.perform(put("/api/feedback/" + feedbackId)
                        .header("Authorization", "Bearer " + daveToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Updated service feedback\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated service feedback"));

        // another user cannot modify dave's feedback
        mockMvc.perform(put("/api/feedback/" + feedbackId)
                        .header("Authorization", "Bearer " + erinToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Hacked\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageAllFeedback() throws Exception {
        register("frank", "frank@example.com", 201);
        String frankToken = login("frank", "Password@123", 200);
        mockMvc.perform(post("/api/feedback")
                        .header("Authorization", "Bearer " + frankToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Admin should see this\"}"))
                .andExpect(status().isCreated());

        String adminToken = login("admin", "TestAdmin@123", 200);

        MvcResult list = mockMvc.perform(get("/api/admin/feedback").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode array = objectMapper.readTree(list.getResponse().getContentAsString());
        long id = array.get(0).get("id").asLong();

        // admin edit
        mockMvc.perform(put("/api/admin/feedback/" + id)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Edited by admin\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Edited by admin"));

        // admin delete
        mockMvc.perform(delete("/api/admin/feedback/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());

        // deletion confirmed
        mockMvc.perform(get("/api/admin/feedback/" + id).header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
