package com.seolstudy.backend.global.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
     void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey",
                "testSecretKey");
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpiration", 604800000L);
        jwtTokenProvider.init();
    }

    @Test
    @DisplayName("Access Token 생성")
    void Access_Token_생성() {
        //given
        Long userId = 1L;
        String username = "testuser";

        // when
        String token = jwtTokenProvider.createAccessToken(userId, username);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("Refresh Token 생성")
    void Refresh_Token_생성() {
        //given
        Long userId = 1L;
        String username = "testuser";

        //when
        String token = jwtTokenProvider.createRefreshToken(userId, username);

        //then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("토큰에서 사용자 ID 추출")
    void 토큰에서_사용자_ID_추출() {
        //given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtTokenProvider.createAccessToken(userId, username);

        //when
        Long extractedUserId = jwtTokenProvider.getUserId(token);

        //then
        assertThat(extractedUserId).isEqualTo(userId);
    }

    @Test
    @DisplayName("토큰에서 사용자명 추출")
    void 토큰에서_사용자명_추출() {
        //given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtTokenProvider.createAccessToken(userId, username);

        //when
        String extractedUsername = jwtTokenProvider.getUsername(token);

        //then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    @DisplayName("유효한 토큰 검증")
    void 유효한_토큰_검증() {
        //given
        Long userId = 1L;
        String username = "testuser";
        String token = jwtTokenProvider.createAccessToken(userId, username);

        //when
        boolean isValid = jwtTokenProvider.validateToken(token);

        //then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("유효하지 않은 토큰 검증")
    void 유효하지_않은_토큰_검증() {
        //given
        String invalidToken = "invalid.token.here";

        //when
        boolean isValid = jwtTokenProvider.validateToken(invalidToken);

        //then
        assertThat(isValid).isFalse();
    }
}
