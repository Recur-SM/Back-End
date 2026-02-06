package com.seolstudy.backend.domain.auth.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.auth.dto.SignUpRequest;
import com.seolstudy.backend.domain.auth.dto.SignUpResponse;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("password123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        User savedUser = User.builder()
                .id(1L)
                .username("test")
                .password("password123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        given(userRepository.existsByUsername("test")).willReturn(false);
        given(userRepository.save(any(User.class))).willReturn(savedUser);

        //when
        SignUpResponse response = authService.signUp(request);

        //then
        assertThat(response.getUsername()).isEqualTo("test");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo(UserRole.MENTEE);
    }

    @Test
    @DisplayName("비밀번호 불일치")
    void 비밀번호_불일치() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("different123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        //when&then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");
    }

    @Test
    @DisplayName("아이디 중복")
    void 아이디_중복() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("password123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        given(userRepository.existsByUsername("test")).willReturn(true);

        //when&then
        assertThatThrownBy(() -> authService.signUp(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 존재하는 아이디입니다.");
    }
}
