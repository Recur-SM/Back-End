package com.seolstudy.backend.domain.feedback.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.seolstudy.backend.domain.feedback.entity.Feedback;
import com.seolstudy.backend.domain.subject.entity.Subject;
import com.seolstudy.backend.domain.subject.repository.SubjectRepository;
import com.seolstudy.backend.domain.task.entity.LearningMaterialType;
import com.seolstudy.backend.domain.task.entity.Task;
import com.seolstudy.backend.domain.task.entity.TaskType;
import com.seolstudy.backend.domain.task.repository.TaskRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class FeedbackRepositoryTest {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @Test
    @DisplayName("피드백 저장")
    void 피드백_저장() {
        //given
        User mentor = createAndSaveUser("mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createAndSaveUser("mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createAndSaveSubject("수학", "MATH");
        Task task = createAndSaveTask(mentor, mentee, subject, LocalDate.now());

        Feedback feedback = Feedback.builder()
                .task(task)
                .mentee(mentee)
                .mentor(mentor)
                .subjectId(subject.getId())
                .feedbackDate(LocalDate.now())
                .detailContent("잘 했습니다.")
                .build();

        //when
        Feedback savedFeedback = feedbackRepository.save(feedback);

        //then
        assertThat(savedFeedback.getId()).isNotNull();
        assertThat(savedFeedback.getDetailContent()).isEqualTo("잘 했습니다.");
        assertThat(savedFeedback.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("특정 멘티의 피드백 목록 조회")
    void 특정_멘티의_피드백_목록_조회() {
        //given
        User mentor = createAndSaveUser("mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createAndSaveUser("mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createAndSaveSubject("수학", "MATH");
        Task task1 = createAndSaveTask(mentor, mentee, subject, LocalDate.now());
        Task task2 = createAndSaveTask(mentor, mentee, subject, LocalDate.now().minusDays(1));

        createAndSaveFeedback(task1, mentee, mentor, subject, LocalDate.now(), "피드백1");
        createAndSaveFeedback(task2, mentee, mentor, subject, LocalDate.now().minusDays(1), "피드백2");

        //when
        List<Feedback> feedbacks = feedbackRepository.findByMenteeId(mentee.getId());

        //then
        assertThat(feedbacks).hasSize(2);
        assertThat(feedbacks.get(0).getDetailContent()).isEqualTo("피드백1"); // 최신순
    }

    @Test
    @DisplayName("특정 과제의 피드백 조회")
    void 특정_과제의_피드백_조회() {
        //given
        User mentor = createAndSaveUser("mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createAndSaveUser("mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createAndSaveSubject("수학", "MATH");
        Task task = createAndSaveTask(mentor, mentee, subject, LocalDate.now());

        Feedback feedback = createAndSaveFeedback(task, mentee, mentor, subject, LocalDate.now(), "피드백");

        //when
        Optional<Feedback> foundFeedback = feedbackRepository.findByTaskId(task.getId());

        //then
        assertThat(foundFeedback).isPresent();
        assertThat(foundFeedback.get().getDetailContent()).isEqualTo("피드백");
    }

    @Test
    @DisplayName("특정 과제에 피드백 존재 여부 확인")
    void 특정_과제에_피드백_존재_여부_확인() {
        //given
        User mentor = createAndSaveUser("mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createAndSaveUser("mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createAndSaveSubject("수학", "MATH");
        Task task = createAndSaveTask(mentor, mentee, subject, LocalDate.now());

        createAndSaveFeedback(task, mentee, mentor, subject, LocalDate.now(), "피드백");

        //when
        boolean exists = feedbackRepository.existsByTaskId(task.getId());

        //then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("특정 과제에 피드백 없음")
    void 특정_과제에_피드백_없음() {
        //given
        User mentor = createAndSaveUser("mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createAndSaveUser("mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createAndSaveSubject("수학", "MATH");
        Task task = createAndSaveTask(mentor, mentee, subject, LocalDate.now());

        //when
        boolean exists = feedbackRepository.existsByTaskId(task.getId());

        //then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("특정 멘티의 특정 날짜 피드백 조회")
    void 특정_멘티의_특정_날짜_피드백_조회() {
        //given
        User mentor = createAndSaveUser("mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createAndSaveUser("mentee1", "멘티1", UserRole.MENTEE);
        Subject subject = createAndSaveSubject("수학", "MATH");
        LocalDate targetDate = LocalDate.now();
        Task task = createAndSaveTask(mentor, mentee, subject, targetDate);

        createAndSaveFeedback(task, mentee, mentor, subject, targetDate, "오늘 피드백");

        //when
        List<Feedback> feedbacks = feedbackRepository.findByMenteeIdAndFeedbackDate(mentee.getId(), targetDate);

        //then
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getDetailContent()).isEqualTo("오늘 피드백");
    }

    @Test
    @DisplayName("특정 멘티의 특정 과목 피드백 조회")
    void 특정_멘티의_특정_과목_피드백_조회() {
        //given
        User mentor = createAndSaveUser("mentor1", "멘토1", UserRole.MENTOR);
        User mentee = createAndSaveUser("mentee1", "멘티1", UserRole.MENTEE);
        Subject math = createAndSaveSubject("수학", "MATH");
        Subject english = createAndSaveSubject("영어", "ENG");

        Task mathTask = createAndSaveTask(mentor, mentee, math, LocalDate.now());
        Task englishTask = createAndSaveTask(mentor, mentee, english, LocalDate.now());

        createAndSaveFeedback(mathTask, mentee, mentor, math, LocalDate.now(), "수학 피드백");
        createAndSaveFeedback(englishTask, mentee, mentor, english, LocalDate.now(), "영어 피드백");

        //when
        List<Feedback> feedbacks = feedbackRepository.findByMenteeIdAndSubjectId(mentee.getId(), math.getId());

        //then
        assertThat(feedbacks).hasSize(1);
        assertThat(feedbacks.get(0).getDetailContent()).isEqualTo("수학 피드백");
    }

    // 헬퍼 메서드
    private User createAndSaveUser(String username, String name, UserRole role) {
        User user = User.builder()
                .username(username)
                .password("password")
                .name(name)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    private Subject createAndSaveSubject(String name, String code) {
        Subject subject = Subject.builder()
                .subjectName(name)
                .subjectCode(code)
                .build();
        return subjectRepository.save(subject);
    }

    private Task createAndSaveTask(User mentor, User mentee, Subject subject, LocalDate taskDate) {
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
        return taskRepository.save(task);
    }

    private Feedback createAndSaveFeedback(Task task, User mentee, User mentor,
                                           Subject subject, LocalDate date, String content) {
        Feedback feedback = Feedback.builder()
                .task(task)
                .mentee(mentee)
                .mentor(mentor)
                .subjectId(subject.getId())
                .feedbackDate(date)
                .detailContent(content)
                .build();
        return feedbackRepository.save(feedback);
    }
}