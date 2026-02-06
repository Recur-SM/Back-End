package com.seolstudy.backend.domain.auth.controller;

import com.seolstudy.backend.domain.auth.dto.LoginRequest;
import com.seolstudy.backend.domain.auth.dto.LoginResponse;
import com.seolstudy.backend.domain.auth.dto.RefreshTokenRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpResponse;
import com.seolstudy.backend.domain.auth.service.AuthService;
import com.seolstudy.backend.global.payload.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "인증", description = "인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새 사용자를 등록합니다")
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public CommonResponse<SignUpResponse> signUp(@Valid @RequestBody SignUpRequest request){
        SignUpResponse response = authService.signUp(request);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰으로 액세스 토큰을 재발급합니다")
    @PostMapping("/refresh")
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return CommonResponse.onSuccess(response);
    }

    @Operation(summary = "로그아웃", description = "리프레시 토큰을 삭제해 로그아웃 처리합니다")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public CommonResponse<String> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return CommonResponse.onSuccess("로그아웃 완료");
    }
}
