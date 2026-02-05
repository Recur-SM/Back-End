package com.seolstudy.backend.domain.user;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UserTest {

    @Test
    @DisplayName("User 객체 생성")
    void 유저_객체_생성() {
        //given&then
        User user = User.builder()
                .username("test")
                .password("1234")
                .name("정용태")
                .role(UserRole.MENTEE)
                .build();

        //then
        assertThat(user).isNotNull();
        assertThat(user.getUsername()).isEqualTo("test");
        assertThat(user.getPassword()).isEqualTo("1234");
        assertThat(user.getName()).isEqualTo("정용태");
        assertThat(user.getRole()).isEqualTo(UserRole.MENTEE);
    }

    @Test
    @DisplayName("MENTEE 역할을 가진 사용자는 멘티이다")
    void isMentee() {
        //given
        User user = User.builder()
                .username("test")
                .password("1234")
                .name("정용태")
                .role(UserRole.MENTEE)
                .build();

        //when&then
        assertThat(user.getRole()).isEqualTo(UserRole.MENTEE);
    }

    @Test
    @DisplayName("MENTOR 역할을 가진 사용자는 멘토이다")
    void isMentor() {
        //given
        User user = User.builder()
                .username("test")
                .password("1234")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();

        //when&then
        assertThat(user.getRole()).isEqualTo(UserRole.MENTOR);
    }
}
