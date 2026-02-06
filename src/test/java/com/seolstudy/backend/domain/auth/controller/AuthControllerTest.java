package com.seolstudy.backend.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seolstudy.backend.domain.auth.dto.SignUpRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpResponse;
import com.seolstudy.backend.domain.auth.service.AuthService;
import com.seolstudy.backend.domain.user.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("회원가입 API")
    void 회원가입_API() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("password123")
                .name("홍길동")
                .role(UserRole.MENTOR)
                .build();

        SignUpResponse response = SignUpResponse.builder()
                .id(1L)
                .username("test")
                .name("홍길동")
                .role(UserRole.MENTOR)
                .build();

        //when
        given(authService.signUp(any(SignUpRequest.class))).willReturn(response);

        //then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.username").value("test"))
                .andExpect(jsonPath("$.result.name").value("홍길동"))
                .andExpect(jsonPath("$.result.role").value("MENTOR"));
    }

    @Test
    @DisplayName("회원가입 API 실패 - 검증 실패")
    void 회원가입_API_실패_검증() throws Exception {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("abc")
                .password("password123")
                .passwordConfirm("password123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        //when&then
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isSuccess").value(false));
    }
}
