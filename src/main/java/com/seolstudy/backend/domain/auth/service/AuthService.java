package com.seolstudy.backend.domain.auth.service;

import com.seolstudy.backend.domain.auth.dto.LoginRequest;
import com.seolstudy.backend.domain.auth.dto.LoginResponse;
import com.seolstudy.backend.domain.auth.dto.RefreshTokenRequest;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.auth.dto.SignUpRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpResponse;
import com.seolstudy.backend.domain.auth.entity.RefreshToken;
import com.seolstudy.backend.domain.auth.repository.RefreshTokenRepository;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public SignUpResponse signUp(SignUpRequest request) {
        if (!request.isPasswordMatch()) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole())
                .build();

        User savedUser = userRepository.save(user);

        return SignUpResponse.from(savedUser);
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new IllegalArgumentException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String role = user.getRole().name();
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), role);
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getUsername(), role);

        saveRefreshToken(user, refreshToken);

        return LoginResponse.of(accessToken, refreshToken, user.getId(), user.getUsername());
    }

    @Transactional
    public LoginResponse refresh(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다."));

        User user = storedToken.getUser();
        String role = user.getRole().name();
        String newAccessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getUsername(), role);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getUsername(), role);

        storedToken.updateToken(newRefreshToken);

        return LoginResponse.of(newAccessToken, newRefreshToken, user.getId(), user.getUsername());
    }

    @Transactional
    public void logout(String username) {
        userRepository.findByUsername(username)
                .ifPresent(user -> refreshTokenRepository.deleteByUserId(user.getId()));
    }

    private void saveRefreshToken(User user, String refreshToken) {
        refreshTokenRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        token -> token.updateToken(refreshToken),
                        () -> refreshTokenRepository.save(RefreshToken.builder()
                                .user(user)
                                .token(refreshToken)
                                .build())
                );
    }
}
