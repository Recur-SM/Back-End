package com.seolstudy.backend.domain.auth.controller;

import com.seolstudy.backend.domain.auth.dto.LoginRequest;
import com.seolstudy.backend.domain.auth.dto.LoginResponse;
import com.seolstudy.backend.domain.auth.dto.SignUpRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpResponse;
import com.seolstudy.backend.domain.auth.service.AuthService;
import com.seolstudy.backend.global.payload.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request){
        SignUpResponse response = authService.signUp(request);
        return CommonResponse.onSuccess(response);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return CommonResponse.onSuccess(response);
    }
}
