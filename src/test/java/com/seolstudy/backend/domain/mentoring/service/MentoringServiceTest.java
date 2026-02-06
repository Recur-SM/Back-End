package com.seolstudy.backend.domain.mentoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.seolstudy.backend.domain.mentoring.dto.MentoringCreateRequest;
import com.seolstudy.backend.domain.mentoring.dto.MentoringResponse;
import com.seolstudy.backend.domain.mentoring.entity.Mentoring;
import com.seolstudy.backend.domain.mentoring.repository.MentoringRepository;
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
public class MentoringServiceTest {

    @Mock
    private MentoringRepository mentoringRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MentoringService mentoringService;

    @Test
    @DisplayName("멘토-멘티 관계 등록 성공")
    void 멘토_멘티_관계_등록_성공() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        User mentee = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee, "id", 2L);

        MentoringCreateRequest request = MentoringCreateRequest.builder()
                .menteeId(2L)
                .build();

        Mentoring mentoring = Mentoring.builder()
                .mentor(mentor)
                .mentee(mentee)
                .build();
        ReflectionTestUtils.setField(mentoring, "id", 1L);

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(mentoringRepository.existsActiveRelationship(1L, 2L)).willReturn(false);
        given(mentoringRepository.findByMentorIdAndMenteeId(1L, 2L)).willReturn(Optional.empty());
        given(mentoringRepository.save(any(Mentoring.class))).willReturn(mentoring);

        //when
        MentoringResponse response = mentoringService.createMentoring("mentor1", request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getMentorId()).isEqualTo(1L);
        assertThat(response.getMenteeId()).isEqualTo(2L);
        assertThat(response.getMentorName()).isEqualTo("정용태");
        assertThat(response.getMenteeName()).isEqualTo("임수미");
        verify(mentoringRepository).save(any(Mentoring.class));
    }

    @Test
    @DisplayName("멘토 권한이 없는 사용자가 등록 시도 시 실패")
    void 멘토_권한_없음_실패() {
        //given
        User notMentor = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();

        MentoringCreateRequest request = MentoringCreateRequest.builder()
                .menteeId(2L)
                .build();

        given(userRepository.findByUsername("mentee1")).willReturn(Optional.of(notMentor));

        //when&then
        assertThatThrownBy(() -> mentoringService.createMentoring("mentee1", request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("멘티 권한이 없는 사용자를 등록 시도 시 실패")
    void 멘티_권한_없음_실패() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        User notMentee = User.builder()
                .username("mentor2")
                .password("password")
                .name("박멘토")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(notMentee, "id", 2L);

        MentoringCreateRequest request = MentoringCreateRequest.builder()
                .menteeId(2L)
                .build();

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(userRepository.findById(2L)).willReturn(Optional.of(notMentee));

        //when&then
        assertThatThrownBy(() -> mentoringService.createMentoring("mentor1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("멘티 역할을 가진 사용자만 등록할 수 있습니다.");
    }

    @Test
    @DisplayName("이미 등록된 멘티 등록 시도 시 실패")
    void 중복_등록_실패() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        User mentee = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee, "id", 2L);

        MentoringCreateRequest request = MentoringCreateRequest.builder()
                .menteeId(2L)
                .build();

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(mentoringRepository.existsActiveRelationship(1L, 2L)).willReturn(true);

        //when&then
        assertThatThrownBy(() -> mentoringService.createMentoring("mentor1", request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 등록된 멘티입니다.");
    }

    @Test
    @DisplayName("기존 비활성화된 관계가 있으면 재활성화")
    void 비활성화된_관계_재활성화() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        User mentee = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee, "id", 2L);

        Mentoring existingMentoring = Mentoring.builder()
                .mentor(mentor)
                .mentee(mentee)
                .build();
        ReflectionTestUtils.setField(existingMentoring, "id", 1L);
        existingMentoring.deactivate();

        MentoringCreateRequest request = MentoringCreateRequest.builder()
                .menteeId(2L)
                .build();

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(mentoringRepository.existsActiveRelationship(1L, 2L)).willReturn(false);
        given(mentoringRepository.findByMentorIdAndMenteeId(1L, 2L))
                .willReturn(Optional.of(existingMentoring));
        given(mentoringRepository.save(any(Mentoring.class))).willReturn(existingMentoring);

        //when
        MentoringResponse response = mentoringService.createMentoring("mentor1", request);

        //then
        assertThat(response).isNotNull();
        assertThat(existingMentoring.getIsActive()).isTrue();
        verify(mentoringRepository).save(existingMentoring);
    }

    @Test
    @DisplayName("멘토-멘티 관계 비활성화 성공")
    void 멘토_멘티_관계_비활성화_성공() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        User mentee = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee, "id", 2L);

        Mentoring mentoring = Mentoring.builder()
                .mentor(mentor)
                .mentee(mentee)
                .build();
        ReflectionTestUtils.setField(mentoring, "id", 1L);
        ReflectionTestUtils.setField(mentoring, "isActive", true);

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(mentoringRepository.findByMentorIdAndMenteeId(1L, 2L))
                .willReturn(Optional.of(mentoring));

        //when
        mentoringService.deactivateMentoring("mentor1", 2L);

        //then
        assertThat(mentoring.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("이미 비활성화된 관계 비활성화 시도 시 실패")
    void 이미_비활성화된_관계_비활성화_실패() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        User mentee = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee, "id", 2L);

        Mentoring mentoring = Mentoring.builder()
                .mentor(mentor)
                .mentee(mentee)
                .build();
        ReflectionTestUtils.setField(mentoring, "id", 1L);
        mentoring.deactivate();

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(mentoringRepository.findByMentorIdAndMenteeId(1L, 2L))
                .willReturn(Optional.of(mentoring));

        //when&then
        assertThatThrownBy(() -> mentoringService.deactivateMentoring("mentor1", 2L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 비활성화된 관계입니다.");
    }
}