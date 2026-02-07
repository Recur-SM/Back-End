package com.seolstudy.backend.domain.mentoring.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.seolstudy.backend.domain.mentoring.dto.MenteeListResponse;
import com.seolstudy.backend.domain.mentoring.dto.MentorResponse;
import com.seolstudy.backend.domain.mentoring.dto.MentoringCreateRequest;
import com.seolstudy.backend.domain.mentoring.dto.MentoringResponse;
import com.seolstudy.backend.domain.mentoring.entity.Mentoring;
import com.seolstudy.backend.domain.mentoring.repository.MentoringRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import java.util.Arrays;
import java.util.Collections;
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
                .isInstanceOf(GeneralException.class);
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
                .isInstanceOf(GeneralException.class);
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
                .isInstanceOf(GeneralException.class);
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
                .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("멘토의 담당 멘티 목록 조회 성공")
    void 멘토의_담당_멘티_목록_조회_성공() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        User mentee1 = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee1, "id", 2L);

        User mentee2 = User.builder()
                .username("mentee2")
                .password("password")
                .name("김멘티")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee2, "id", 3L);

        Mentoring mentoring1 = Mentoring.builder()
                .mentor(mentor)
                .mentee(mentee1)
                .build();
        ReflectionTestUtils.setField(mentoring1, "id", 1L);

        Mentoring mentoring2 = Mentoring.builder()
                .mentor(mentor)
                .mentee(mentee2)
                .build();
        ReflectionTestUtils.setField(mentoring2, "id", 2L);

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(mentoringRepository.findActiveByMentorId(1L))
                .willReturn(Arrays.asList(mentoring1, mentoring2));

        //when
        MenteeListResponse response = mentoringService.getMentees("mentor1");

        //then
        assertThat(response).isNotNull();
        assertThat(response.getMentorId()).isEqualTo(1L);
        assertThat(response.getMentorName()).isEqualTo("정용태");
        assertThat(response.getMentees()).hasSize(2);
        assertThat(response.getMentees().get(0).getMenteeId()).isEqualTo(2L);
        assertThat(response.getMentees().get(0).getMenteeName()).isEqualTo("임수미");
        assertThat(response.getMentees().get(0).getUsername()).isEqualTo("mentee1");
        assertThat(response.getMentees().get(1).getMenteeId()).isEqualTo(3L);
        assertThat(response.getMentees().get(1).getMenteeName()).isEqualTo("김멘티");
    }

    @Test
    @DisplayName("멘토 권한이 없는 사용자가 멘티 목록 조회 시도 시 실패")
    void 멘토_권한_없음_멘티_목록_조회_실패() {
        //given
        User notMentor = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();

        given(userRepository.findByUsername("mentee1")).willReturn(Optional.of(notMentor));

        //when&then
        assertThatThrownBy(() -> mentoringService.getMentees("mentee1"))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("담당 멘티가 없는 멘토의 빈 목록 조회")
    void 담당_멘티_없음_빈_목록_조회() {
        //given
        User mentor = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();
        ReflectionTestUtils.setField(mentor, "id", 1L);

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(mentor));
        given(mentoringRepository.findActiveByMentorId(1L))
                .willReturn(Collections.emptyList());

        //when
        MenteeListResponse response = mentoringService.getMentees("mentor1");

        //then
        assertThat(response).isNotNull();
        assertThat(response.getMentorId()).isEqualTo(1L);
        assertThat(response.getMentees()).isEmpty();
    }

    @Test
    @DisplayName("멘티의 담당 멘토 조회 성공")
    void 멘티의_담당_멘토_조회_성공() {
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

        given(userRepository.findByUsername("mentee1")).willReturn(Optional.of(mentee));
        given(mentoringRepository.findActiveByMenteeId(2L))
                .willReturn(Optional.of(mentoring));

        //when
        MentorResponse response = mentoringService.getMentor("mentee1");

        //then
        assertThat(response).isNotNull();
        assertThat(response.getMenteeId()).isEqualTo(2L);
        assertThat(response.getMenteeName()).isEqualTo("임수미");
        assertThat(response.getMentorId()).isEqualTo(1L);
        assertThat(response.getMentorName()).isEqualTo("정용태");
        assertThat(response.getMentorUsername()).isEqualTo("mentor1");
    }

    @Test
    @DisplayName("멘티 권한이 없는 사용자가 멘토 조회 시도 시 실패")
    void 멘티_권한_없음_멘토_조회_실패() {
        //given
        User notMentee = User.builder()
                .username("mentor1")
                .password("password")
                .name("정용태")
                .role(UserRole.MENTOR)
                .build();

        given(userRepository.findByUsername("mentor1")).willReturn(Optional.of(notMentee));

        //when&then
        assertThatThrownBy(() -> mentoringService.getMentor("mentor1"))
                .isInstanceOf(GeneralException.class);
    }

    @Test
    @DisplayName("담당 멘토가 없는 멘티의 조회 실패")
    void 담당_멘토_없음_조회_실패() {
        //given
        User mentee = User.builder()
                .username("mentee1")
                .password("password")
                .name("임수미")
                .role(UserRole.MENTEE)
                .build();
        ReflectionTestUtils.setField(mentee, "id", 2L);

        given(userRepository.findByUsername("mentee1")).willReturn(Optional.of(mentee));
        given(mentoringRepository.findActiveByMenteeId(2L))
                .willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> mentoringService.getMentor("mentee1"))
                .isInstanceOf(GeneralException.class);
    }
}