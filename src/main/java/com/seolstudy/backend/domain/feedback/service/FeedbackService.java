package com.seolstudy.backend.domain.feedback.service;

import com.seolstudy.backend.domain.feedback.dto.FeedbackCheckResponse;
import com.seolstudy.backend.domain.feedback.dto.FeedbackCreateRequest;
import com.seolstudy.backend.domain.feedback.dto.FeedbackListResponse;
import com.seolstudy.backend.domain.feedback.dto.FeedbackResponse;
import com.seolstudy.backend.domain.feedback.dto.FeedbackUpdateRequest;
import com.seolstudy.backend.domain.feedback.entity.Feedback;
import com.seolstudy.backend.domain.feedback.repository.FeedbackRepository;
import com.seolstudy.backend.domain.subject.entity.Subject;
import com.seolstudy.backend.domain.subject.repository.SubjectRepository;
import com.seolstudy.backend.domain.task.entity.Task;
import com.seolstudy.backend.domain.task.repository.TaskRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    /**
     * 피드백 생성
     */
    @Transactional
    public FeedbackResponse createFeedback(FeedbackCreateRequest request) {
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.TASK_NOT_FOUND));

        if (feedbackRepository.existsByTaskId(request.getTaskId())) {
            throw new GeneralException(ErrorStatus.FEEDBACK_ALREADY_EXISTS);
        }

        User mentee = getUserOrThrow(request.getMenteeId());
        User mentor = getUserOrThrow(request.getMentorId());

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.SUBJECT_NOT_FOUND));

        LocalDate feedbackDate = parseDate(request.getFeedbackDate());

        Feedback feedback = Feedback.builder()
                .task(task)
                .mentee(mentee)
                .mentor(mentor)
                .subjectId(subject.getId())
                .feedbackDate(feedbackDate)
                .detailContent(request.getDetailContent())
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);

        return FeedbackResponse.from(savedFeedback, task.getTaskName(),
                subject.getSubjectName(), subject.getSubjectCode());
    }

    /**
     * 피드백 상세 조회
     */
    public FeedbackResponse getFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.FEEDBACK_NOT_FOUND));

        Task task = feedback.getTask();
        Subject subject = subjectRepository.findById(feedback.getSubjectId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.SUBJECT_NOT_FOUND));

        return FeedbackResponse.from(feedback, task.getTaskName(),
                subject.getSubjectName(), subject.getSubjectCode());
    }

    /**
     * 멘티의 피드백 목록 조회
     */
    public FeedbackListResponse getFeedbacksByMentee(Long menteeId) {
        User mentee = getUserOrThrow(menteeId);

        List<Feedback> feedbacks = feedbackRepository.findByMenteeId(menteeId);

        List<FeedbackResponse> feedbackResponses = feedbacks.stream()
                .map(feedback -> {
                    Task task = feedback.getTask();
                    Subject subject = subjectRepository.findById(feedback.getSubjectId())
                            .orElseThrow(() -> new GeneralException(ErrorStatus.SUBJECT_NOT_FOUND));
                    return FeedbackResponse.from(feedback, task.getTaskName(),
                            subject.getSubjectName(), subject.getSubjectCode());
                })
                .collect(Collectors.toList());

        return FeedbackListResponse.builder()
                .menteeId(mentee.getId())
                .menteeName(mentee.getName())
                .feedbacks(feedbackResponses)
                .build();
    }

    /**
     * 과제의 피드백 작성 여부 확인
     */
    public FeedbackCheckResponse checkFeedback(Long taskId) {
        // Task 존재 여부 확인
        if (!taskRepository.existsById(taskId)) {
            throw new GeneralException(ErrorStatus.TASK_NOT_FOUND);
        }

        Boolean hasFeedback = feedbackRepository.existsByTaskId(taskId);
        return FeedbackCheckResponse.of(taskId, hasFeedback);
    }

    /**
     * 피드백 수정
     */
    @Transactional
    public FeedbackResponse updateFeedback(Long feedbackId, FeedbackUpdateRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.FEEDBACK_NOT_FOUND));

        feedback.update(request.getDetailContent());

        Task task = feedback.getTask();
        Subject subject = subjectRepository.findById(feedback.getSubjectId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.SUBJECT_NOT_FOUND));

        return FeedbackResponse.from(feedback, task.getTaskName(),
                subject.getSubjectName(), subject.getSubjectCode());
    }

    /**
     * 피드백 삭제
     */
    @Transactional
    public void deleteFeedback(Long feedbackId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.FEEDBACK_NOT_FOUND));

        feedbackRepository.delete(feedback);
    }

    /**
     * 날짜 파싱
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new GeneralException(ErrorStatus.INVALID_FEEDBACK_DATE);
        }
    }

    /**
     * User 조회
     */
    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        }
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
