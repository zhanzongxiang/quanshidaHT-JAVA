package com.qsd.admin.common.exception;

import com.qsd.admin.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new StubController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void shouldExposeStructuredBusinessErrorCode() throws Exception {
        mockMvc.perform(post("/test/business"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.RESOURCE_CONFLICT))
            .andExpect(jsonPath("$.message").value("duplicate resource"));
    }

    @Test
    void shouldExposeReadableValidationMessage() throws Exception {
        mockMvc.perform(post("/test/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "name": ""
                    }
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED))
            .andExpect(jsonPath("$.message").value("name: must not be blank"));
    }

    @RestController
    static class StubController {
        @PostMapping(value = "/test/business", produces = MediaType.APPLICATION_JSON_VALUE)
        ApiResponse<Void> business() {
            throw new BusinessException(ErrorCode.RESOURCE_CONFLICT, "duplicate resource");
        }

        @PostMapping(value = "/test/validation", produces = MediaType.APPLICATION_JSON_VALUE)
        ApiResponse<Void> validation(@Valid @RequestBody StubRequest request) {
            return ApiResponse.ok();
        }
    }

    record StubRequest(@NotBlank String name) {
    }
}
