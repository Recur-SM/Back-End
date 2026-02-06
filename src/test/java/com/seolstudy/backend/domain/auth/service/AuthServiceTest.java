package com.seolstudy.backend.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.seolstudy.backend.domain.auth.dto.LoginRequest;
import com.seolstudy.backend.domain.auth.dto.LoginResponse;
import com.seolstudy.backend.domain.auth.dto.SignUpRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpResponse;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.security.JwtTokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("testuser")
                .password("password123")
                .passwordConfirm("password123")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();

        User savedUser = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();

        ReflectionTestUtils.setField(savedUser, "id", 1L);

        given(userRepository.existsByUsername("testuser")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        //when
        SignUpResponse response = authService.signUp(request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getName()).isEqualTo("테스트");
        assertThat(response.getRole()).isEqualTo(UserRole.MENTEE);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("비밀번호 불일치로 회원가입 실패")
    void 비밀번호_불일치로_회원가입_실패() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("different")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();

        //when&then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("중복 아이디로 회원가입 실패")
    void 중복_아이디로_회원가입_실패() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("password123")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();

        given(userRepository.existsByUsername("test")).willReturn(true);

        //when&then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 사용중인 아이디입니다.");
    }

    @Test
    @DisplayName("로그인 성공")
    void 로그인_성공() {
        //given
        LoginRequest request = LoginRequest.builder()
                .username("test")
                .password("password123")
                .build();

        User user = User.builder()
                .username("test")
                .password("encodedPassword")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();

        given(userRepository.findByUsername("test")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("password123", "encodedPassword")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(any(), anyString())).willReturn("accessToken");
        given(jwtTokenProvider.createRefreshToken(any(), anyString())).willReturn("refreshToken");

        //when
        LoginResponse response = authService.login(request);

        //then
        assertThat(response.getAccessToken()).isEqualTo("accessToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
        assertThat(response.getTokenType()).isEqualTo("Bearer");
        assertThat(response.getUsername()).isEqualTo("test");
    }

    @Test
    @DisplayName("존재하지 않는 아이디로 로그인 실패")
    void 존재하지_않는_아이디로_로그인_실패() {
        //given
        LoginRequest request = LoginRequest.builder()
                .username("notexist")
                .password("password123")
                .build();

        given(userRepository.findByUsername("notexist")).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("비밀번호 불일치로 로그인 실패")
    void 비밀번호_불일치로_로그인_실패() {
        //given
        LoginRequest request = LoginRequest.builder()
                .username("testuser")
                .password("wrongpassword")
                .build();

        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("테스트")
                .role(UserRole.MENTEE)
                .build();

        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));
        given(passwordEncoder.matches("wrongpassword", "encodedPassword")).willReturn(false);

        //when&then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호가 일치하지 않습니다.");
    }
}