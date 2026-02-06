package com.seolstudy.backend.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.seolstudy.backend.domain.user.dto.UserResponse;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("내 정보 조회 성공")
    void 내_정보_조회_성공() {
        //given
        User user = User.builder()
                .username("testuser")
                .password("encodedPassword")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        given(userRepository.findByUsername("testuser")).willReturn(Optional.of(user));

        //when
        UserResponse response = userService.getMyInfo("testuser");

        //then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getName()).isEqualTo("홍길동");
        assertThat(response.getRole()).isEqualTo(UserRole.MENTEE);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 조회 실패")
    void 존재하지_않는_사용자_조회_실패() {
        // given
        given(userRepository.findByUsername("notexist")).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getMyInfo("notexist"))
                .isInstanceOf(GeneralException.class);
    }
}