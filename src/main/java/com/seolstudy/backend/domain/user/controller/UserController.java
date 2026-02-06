package com.seolstudy.backend.domain.user.controller;

import com.seolstudy.backend.domain.user.dto.UserResponse;
import com.seolstudy.backend.domain.user.service.UserService;
import com.seolstudy.backend.global.payload.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "사용자", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인한 사용자 정보 조회")
    @GetMapping("/me")
    public CommonResponse<UserResponse> getMyInfo(@AuthenticationPrincipal UserDetails userDetails){
            UserResponse response = userService.getMyInfo(userDetails.getUsername());
            return CommonResponse.onSuccess(response);
    }
}
