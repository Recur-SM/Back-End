package com.seolstudy.backend.domain.auth.dto;

import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SignUpResponse {

    private Long id;
    private String username;
    private String name;
    private UserRole role;
    private LocalDateTime createdAt;

    public static SignUpResponse from(User user) {
        return SignUpResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
