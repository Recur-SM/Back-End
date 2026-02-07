package com.seolstudy.backend.domain.feedback.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.seolstudy.backend.domain.feedback.dto.*;
import com.seolstudy.backend.domain.feedback.entity.Feedback;
import com.seolstudy.backend.domain.feedback.repository.FeedbackRepository;
import com.seolstudy.backend.domain.subject.entity.Subject;
import com.seolstudy.backend.domain.subject.repository.SubjectRepository;
import com.seolstudy.backend.domain.task.entity.LearningMaterialType;
import com.seolstudy.backend.domain.task.entity.Task;
import com.seolstudy.backend.domain.task.entity.TaskType;
import com.seolstudy.backend.domain.task.repository.TaskRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    @DisplayName("피드백 생성 성공")
    void 피드백_생성_성공() {
        //given
        User mentor = createUser(1L, "mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createUser(2L, "mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createSubject(1L, "수학", "MATH");
        Task task = createTask(1L, mentor, mentee, subject, LocalDate.now());

        FeedbackCreateRequest request = FeedbackCreateRequest.builder()
                .taskId(1L)
                .menteeId(2L)
                .mentorId(1L)
                .subjectId(1L)
                .feedbackDate("2026-02-07")
                .detailContent("잘 했습니다.")
                .build();

        Feedback feedback = createFeedback(1L, task, mentee, mentor, subject,
                LocalDate.parse("2026-02-07"), "잘 했습니다.");

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(feedbackRepository.existsByTaskId(1L)).willReturn(false);
        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(userRepository.findById(1L)).willReturn(Optional.of(mentor));
        given(subjectRepository.findById(1L)).willReturn(Optional.of(subject));
        given(feedbackRepository.save(any(Feedback.class))).willReturn(feedback);

        //when
        FeedbackResponse response = feedbackService.createFeedback(request);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getFeedbackId()).isEqualTo(1L);
        assertThat(response.getDetailContent()).isEqualTo("잘 했습니다.");
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    @DisplayName("피드백 생성 실패 - 과제 없음")
    void 피드백_생성_실패_과제_없음() {
        //given
        FeedbackCreateRequest request = FeedbackCreateRequest.builder()
                .taskId(999L)
                .menteeId(2L)
                .mentorId(1L)
                .subjectId(1L)
                .feedbackDate("2026-02-07")
                .detailContent("잘 했습니다.")
                .build();

        given(taskRepository.findById(999L)).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> feedbackService.createFeedback(request))
                .isInstanceOf(GeneralException.class)
                .extracting("status")
                .isEqualTo(ErrorStatus.TASK_NOT_FOUND);
    }

    @Test
    @DisplayName("피드백 생성 실패 - 이미 존재")
    void 피드백_생성_실패_이미_존재() {
        //given
        User mentor = createUser(1L, "mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createUser(2L, "mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createSubject(1L, "수학", "MATH");
        Task task = createTask(1L, mentor, mentee, subject, LocalDate.now());

        FeedbackCreateRequest request = FeedbackCreateRequest.builder()
                .taskId(1L)
                .menteeId(2L)
                .mentorId(1L)
                .subjectId(1L)
                .feedbackDate("2026-02-07")
                .detailContent("잘 했습니다.")
                .build();

        given(taskRepository.findById(1L)).willReturn(Optional.of(task));
        given(feedbackRepository.existsByTaskId(1L)).willReturn(true);

        //when&then
        assertThatThrownBy(() -> feedbackService.createFeedback(request))
                .isInstanceOf(GeneralException.class)
                .extracting("status")
                .isEqualTo(ErrorStatus.FEEDBACK_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("피드백 상세 조회 성공")
    void 피드백_상세_조회_성공() {
        //given
        User mentor = createUser(1L, "mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createUser(2L, "mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createSubject(1L, "수학", "MATH");
        Task task = createTask(1L, mentor, mentee, subject, LocalDate.now());
        Feedback feedback = createFeedback(1L, task, mentee, mentor, subject,
                LocalDate.now(), "잘 했습니다.");

        given(feedbackRepository.findById(1L)).willReturn(Optional.of(feedback));
        given(subjectRepository.findById(1L)).willReturn(Optional.of(subject));

        //when
        FeedbackResponse response = feedbackService.getFeedback(1L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getFeedbackId()).isEqualTo(1L);
        assertThat(response.getDetailContent()).isEqualTo("잘 했습니다.");
    }

    @Test
    @DisplayName("피드백 상세 조회 실패 - 피드백 없음")
    void 피드백_상세_조회_실패_피드백_없음() {
        //given
        given(feedbackRepository.findById(999L)).willReturn(Optional.empty());

        //when&then
        assertThatThrownBy(() -> feedbackService.getFeedback(999L))
                .isInstanceOf(GeneralException.class)
                .extracting("status")
                .isEqualTo(ErrorStatus.FEEDBACK_NOT_FOUND);
    }

    @Test
    @DisplayName("멘티의 피드백 목록 조회 성공")
    void 멘티의_피드백_목록_조회_성공() {
        //given
        User mentor = createUser(1L, "mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createUser(2L, "mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createSubject(1L, "수학", "MATH");
        Task task1 = createTask(1L, mentor, mentee, subject, LocalDate.now());
        Task task2 = createTask(2L, mentor, mentee, subject, LocalDate.now().minusDays(1));

        Feedback feedback1 = createFeedback(1L, task1, mentee, mentor, subject,
                LocalDate.now(), "피드백1");
        Feedback feedback2 = createFeedback(2L, task2, mentee, mentor, subject,
                LocalDate.now().minusDays(1), "피드백2");

        given(userRepository.findById(2L)).willReturn(Optional.of(mentee));
        given(feedbackRepository.findByMenteeId(2L)).willReturn(List.of(feedback1, feedback2));
        given(subjectRepository.findById(1L)).willReturn(Optional.of(subject));

        //when
        FeedbackListResponse response = feedbackService.getFeedbacksByMentee(2L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getMenteeId()).isEqualTo(2L);
        assertThat(response.getFeedbacks()).hasSize(2);
    }

    @Test
    @DisplayName("피드백 작성 여부 확인 - 작성됨")
    void 피드백_작성_여부_확인_작성됨() {
        //given
        given(taskRepository.existsById(1L)).willReturn(true);
        given(feedbackRepository.existsByTaskId(1L)).willReturn(true);

        //when
        FeedbackCheckResponse response = feedbackService.checkFeedback(1L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getTaskId()).isEqualTo(1L);
        assertThat(response.getHasFeedback()).isTrue();
    }

    @Test
    @DisplayName("피드백 작성 여부 확인 - 작성 안됨")
    void 피드백_작성_여부_확인_작성_안됨() {
        //given
        given(taskRepository.existsById(1L)).willReturn(true);
        given(feedbackRepository.existsByTaskId(1L)).willReturn(false);

        //when
        FeedbackCheckResponse response = feedbackService.checkFeedback(1L);

        //then
        assertThat(response).isNotNull();
        assertThat(response.getTaskId()).isEqualTo(1L);
        assertThat(response.getHasFeedback()).isFalse();
    }

    @Test
    @DisplayName("피드백 수정 성공")
    void 피드백_수정_성공() {
        //given
        User mentor = createUser(1L, "mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createUser(2L, "mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createSubject(1L, "수학", "MATH");
        Task task = createTask(1L, mentor, mentee, subject, LocalDate.now());
        Feedback feedback = createFeedback(1L, task, mentee, mentor, subject,
                LocalDate.now(), "원본 피드백");

        FeedbackUpdateRequest request = FeedbackUpdateRequest.builder()
                .detailContent("수정된 피드백")
                .build();

        given(feedbackRepository.findById(1L)).willReturn(Optional.of(feedback));
        given(subjectRepository.findById(1L)).willReturn(Optional.of(subject));

        //when
        FeedbackResponse response = feedbackService.updateFeedback(1L, request);

        //then
        assertThat(response).isNotNull();
        assertThat(feedback.getDetailContent()).isEqualTo("수정된 피드백");
    }

    @Test
    @DisplayName("피드백 삭제 성공")
    void 피드백_삭제_성공() {
        //given
        User mentor = createUser(1L, "mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createUser(2L, "mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createSubject(1L, "수학", "MATH");
        Task task = createTask(1L, mentor, mentee, subject, LocalDate.now());
        Feedback feedback = createFeedback(1L, task, mentee, mentor, subject,
                LocalDate.now(), "피드백");

        given(feedbackRepository.findById(1L)).willReturn(Optional.of(feedback));

        //when
        feedbackService.deleteFeedback(1L);

        //then
        verify(feedbackRepository).delete(feedback);
    }

    // 헬퍼 메서드
    private User createUser(Long id, String username, String name, UserRole role) {
        User user = User.builder()
                .username(username)
                .password("password")
                .name(name)
                .role(role)
                .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Subject createSubject(Long id, String name, String code) {
        Subject subject = Subject.builder()
                .subjectName(name)
                .subjectCode(code)
                .build();
        ReflectionTestUtils.setField(subject, "id", id);
        return subject;
    }

    private Task createTask(Long id, User mentor, User mentee, Subject subject, LocalDate taskDate) {
        Task task = Task.builder()
                .mentor(mentor)
                .mentee(mentee)
                .subject(subject)
                .taskDate(taskDate)
                .taskName("과제")
                .taskType(TaskType.FIXED)
                .learningMaterialType(LearningMaterialType.PDF)
                .isFixed(false)
                .build();
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }

    private Feedback createFeedback(Long id, Task task, User mentee, User mentor,
                                    Subject subject, LocalDate date, String content) {
        Feedback feedback = Feedback.builder()
                .task(task)
                .mentee(mentee)
                .mentor(mentor)
                .subjectId(subject.getId())
                .feedbackDate(date)
                .detailContent(content)
                .build();
        ReflectionTestUtils.setField(feedback, "id", id);
        return feedback;
    }
}