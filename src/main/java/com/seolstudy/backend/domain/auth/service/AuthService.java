package com.seolstudy.backend.domain.auth.service;

import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.auth.dto.SignUpRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpResponse;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (!request.isPasswordMatch()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 존재하는 아이디입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .name(request.getName())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }

    public boolean checkUsernameDuplicate(String username) {
        return userRepository.existsByUsername(username);
    }
}
