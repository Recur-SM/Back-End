package com.seolstudy.backend.domain.user.dto;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.seolstudy.backend.domain.user.entity.UserRole;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class SignUpRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("유효한 회원가입 요청을 생성할 수 있다")
    void 유효한_회원가입_요청() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("12345678")
                .passwordConfirm("12345678")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();

        //when
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        //then
        assertThat(violations.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 일치하면 true 반환")
    void 비밀번호_일치() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("password123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        //when&then
        assertThat(request.isPasswordMatch()).isTrue();
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 false 반환")
    void 비밀번호_불일치() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("password123")
                .passwordConfirm("different123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        //when & then
        assertThat(request.isPasswordMatch()).isFalse();
    }

    @Test
    @DisplayName("아이디가 4자 미만이면 검증 실패")
    void 아이디_길이_부족() {
        // given
        SignUpRequest request = SignUpRequest.builder()
                .username("abc")
                .password("password123")
                .passwordConfirm("password123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        //when
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        //then
        assertThat(violations.size()).isEqualTo(1);
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 검증 실패")
    void 비밀번호_길이_부족() {
        //given
        SignUpRequest request = SignUpRequest.builder()
                .username("test")
                .password("pass123")
                .passwordConfirm("pass123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();

        //when
        Set<ConstraintViolation<SignUpRequest>> violations = validator.validate(request);

        //then
        assertThat(violations.size()).isEqualTo(1);
    }
}
