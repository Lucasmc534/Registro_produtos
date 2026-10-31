package com.lab.mvc.springbootlist.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void productPagesRequireAuthentication() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void validBasicCredentialsCanOpenProductPages() throws Exception {
        mockMvc.perform(get("/products")
                        .header("Authorization", basicAuthorization("test-admin", "test-only-password")))
                .andExpect(status().isOk());
    }

    @Test
    void thymeleafAddsCsrfTokenToPostForms() throws Exception {
        mockMvc.perform(get("/products/new")
                        .header("Authorization", basicAuthorization("test-admin", "test-only-password")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }

    @Test
    void stateChangingRequestsRequireCsrfToken() throws Exception {
        mockMvc.perform(post("/products/delete/1")
                        .header("Authorization", basicAuthorization("test-admin", "test-only-password")))
                .andExpect(status().isForbidden());
    }

    private static String basicAuthorization(String username, String password) {
        String credentials = username + ":" + password;
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encodedCredentials;
    }
}
