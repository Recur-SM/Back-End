package com.seolstudy.backend.domain.user.repository;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("사용자 저장")
    void 사용자_저장() {
        //given
        User user = User.builder()
                .username("test")
                .password("password")
                .name("홍길동")
                .role(UserRole.MENTOR)
                .build();

        //when
        User savedUser = userRepository.save(user);

        //then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("test");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("username으로 사용자 조회")
    void username으로_사용자_조회() {
        //given
        User user = User.builder()
                .username("test")
                .password("password")
                .name("홍길동")
                .role(UserRole.MENTOR)
                .build();
        userRepository.save(user);

        //when
        User foundUser = userRepository.findByUserrname("test").orElseThrow();

        //then
        assertThat(foundUser.getUsername()).isEqualTo("test");
        assertThat(foundUser.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("존재하지 않는 username 조회 시 빈 Optional 반환")
    void 존재하지_않는_username_조회() {
        // when
        Optional<User> foundUser = userRepository.findByUsername("notexist");

        // then
        assertThat(foundUser.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("username 중복하는 경우")
    void username_중복_존재() {
        // given
        User user = User.builder()
                .username("test")
                .password("password123")
                .name("홍길동")
                .role(UserRole.MENTEE)
                .build();
        userRepository.save(user);

        // when
        boolean exists = userRepository.existsByUsername("test");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("username 중복하지 않는 경우")
    void username_중복_없음() {
        // when
        boolean exists = userRepository.existsByUsername("notexist");

        // then
        assertThat(exists).isFalse();
    }
}
