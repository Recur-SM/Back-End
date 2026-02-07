package com.seolstudy.backend.domain.task.service;

import com.seolstudy.backend.domain.feedback.repository.FeedbackRepository;
import com.seolstudy.backend.domain.subject.entity.Subject;
import com.seolstudy.backend.domain.subject.repository.SubjectRepository;
import com.seolstudy.backend.domain.task.dto.*;
import com.seolstudy.backend.domain.task.entity.Task;
import com.seolstudy.backend.domain.task.entity.TaskCompletion;
import com.seolstudy.backend.domain.task.repository.TaskCompletionRepository;
import com.seolstudy.backend.domain.task.repository.TaskRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import com.seolstudy.backend.global.storage.LocalFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final FeedbackRepository feedbackRepository;
    private final TaskCompletionRepository taskCompletionRepository;
    private final LocalFileStorage localFileStorage;

    /**
     * 오늘 할일 전체 조회
     */
    public TaskListResponse getTasks(Long menteeId, String dateStr) {
        // 멘티 존재 여부 확인
        validateMenteeExists(menteeId);

        // 날짜 파싱 (미입력 시 오늘)
        LocalDate targetDate = parseDate(dateStr);

        // 과제 조회
        List<Task> tasks = taskRepository.findByMenteeIdAndTaskDate(menteeId, targetDate);

        // DTO 변환
        List<TaskResponse> taskResponses = tasks.stream()
                .map(task -> {
                    Subject subject = task.getSubject();
                    String subjectName = subject.getSubjectName();
                    String subjectCode = subject.getSubjectCode();
                    Boolean hasFeedback = feedbackRepository.existsByTaskId(task.getId());
                    return TaskResponse.from(task, subjectName, subjectCode, hasFeedback);
                })
                .collect(Collectors.toList());

        return TaskListResponse.builder()
                .plannerDate(targetDate)
                .tasks(taskResponses)
                .build();
    }

    /**
     * 특정 과목 할일 조회
     */
    public TaskListBySubjectResponse getTasksBySubject(Long menteeId, String dateStr, String subjectCode) {
        // 멘티 존재 여부 확인
        validateMenteeExists(menteeId);

        // 날짜 파싱
        LocalDate targetDate = parseDate(dateStr);

        Subject subject = subjectRepository.findBySubjectCode(subjectCode)
                .orElseThrow(() -> new GeneralException(ErrorStatus.SUBJECT_NOT_FOUND));
        Long subjectId = subject.getId();
        String subjectName = subject.getSubjectName();

        // 과제 조회
        List<Task> tasks = taskRepository.findByMenteeIdAndTaskDateAndSubjectId(menteeId, targetDate, subjectId);

        // DTO 변환
        List<TaskResponse> taskResponses = tasks.stream()
                .map(task -> {
                    Boolean hasFeedback = feedbackRepository.existsByTaskId(task.getId());
                    return TaskResponse.from(task, subjectName, subjectCode, hasFeedback);
                })
                .collect(Collectors.toList());

        return TaskListBySubjectResponse.builder()
                .plannerDate(targetDate)
                .subjectName(subjectName)
                .subjectCode(subjectCode)
                .tasks(taskResponses)
                .build();
    }

    /**
     * 오늘 할일 추가
     */
    @Transactional
    public TaskCreateResponse createTask(TaskCreateRequest request) {
        // 멘티/멘토 조회
        User mentee = getUserOrThrow(request.getMenteeId());
        User mentor = getUserOrThrow(request.getMentorId());

        // 과목 조회
        Subject subject = subjectRepository.findBySubjectCode(request.getSubjectCode())
                .orElseThrow(() -> new GeneralException(ErrorStatus.SUBJECT_NOT_FOUND));

        // 날짜 파싱
        LocalDate taskDate = parseDate(request.getTaskDate());

        // Task 엔티티 생성
        Task task = Task.builder()
                .mentee(mentee)
                .mentor(mentor)
                .subject(subject)
                .taskDate(taskDate)
                .taskName(request.getTaskName())
                .taskGoal(request.getTaskGoal())
                .taskType(request.getTaskType())
                .learningMaterialType(request.getLearningMaterialType())
                .pdfFileUrl(request.getPdfFileUrl())
                .columnContent(request.getColumnContent())
                .comment(request.getComment())
                .isFixed(Boolean.FALSE)
                .build();

        // 저장
        Task savedTask = taskRepository.save(task);

        // DTO 변환 및 반환
        return TaskCreateResponse.from(savedTask, subject.getSubjectName(), request.getSubjectCode());
    }

    /**
     * 과제 제출
     */
    @Transactional
    public TaskCompletionResponse submitTask(Long taskId, MultipartFile completionPhoto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TASK_NOT_FOUND));

        if (taskCompletionRepository.existsByTaskId(taskId)) {
            throw new GeneralException(ErrorStatus.TASK_COMPLETION_ALREADY_EXISTS);
        }

        MultipartFile photo = getSingleCompletionPhoto(completionPhoto);
        String photoUrl = localFileStorage.storeTaskCompletionImage(photo);

        TaskCompletion completion = TaskCompletion.builder()
                .task(task)
                .completionPhotoUrl(photoUrl)
                .isCompleted(Boolean.TRUE)
                .completedAt(LocalDateTime.now())
                .build();

        TaskCompletion savedCompletion = taskCompletionRepository.save(completion);
        return TaskCompletionResponse.from(savedCompletion);
    }

    /**
     * 날짜 문자열 파싱 (yyyy-MM-dd)
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return LocalDate.now();
        }

        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_DATE);
        }
    }

    /**
     * 멘티 존재 여부 검증
     */
    private void validateMenteeExists(Long menteeId) {
        if (menteeId == null || !userRepository.existsById(menteeId)) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        }
    }

    /**
     * 멘토 존재 여부 검증
     */
    private void validateMentorExists(Long mentorId) {
        if (mentorId == null || !userRepository.existsById(mentorId)) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        }
    }

    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private MultipartFile getSingleCompletionPhoto(MultipartFile completionPhoto) {
        MultipartFile photo = completionPhoto;
        if (photo == null || photo.isEmpty()) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_COMPLETION_IMAGE);
        }

        return photo;
    }
}