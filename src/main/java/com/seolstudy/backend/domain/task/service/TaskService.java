package com.seolstudy.backend.domain.task.service;

import com.seolstudy.backend.domain.feedback.repository.FeedbackRepository;
import com.seolstudy.backend.domain.subject.entity.Subject;
import com.seolstudy.backend.domain.subject.repository.SubjectRepository;
import com.seolstudy.backend.domain.task.dto.*;
import com.seolstudy.backend.domain.task.entity.LearningMaterialType;
import com.seolstudy.backend.domain.task.entity.Task;
import com.seolstudy.backend.domain.task.entity.TaskCompletion;
import com.seolstudy.backend.domain.task.repository.TaskCompletionRepository;
import com.seolstudy.backend.domain.task.repository.TaskRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import com.seolstudy.backend.global.storage.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final FileStorage fileStorage;

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
                    String pdfFileUrl = fileStorage.createPresignedGetUrl(task.getPdfFileUrl(), Duration.ofHours(24));
                    return TaskResponse.from(task, subjectName, subjectCode, hasFeedback, pdfFileUrl);
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
                    String pdfFileUrl = fileStorage.createPresignedGetUrl(task.getPdfFileUrl(), Duration.ofHours(24));
                    return TaskResponse.from(task, subjectName, subjectCode, hasFeedback, pdfFileUrl);
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
     * 과제 상세 조회
     */
    public TaskDetailResponse getTaskDetail(Long taskId, Long menteeId) {
        // 멘티 존재 여부 확인
        validateMenteeExists(menteeId);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TASK_NOT_FOUND));

        if (!task.getMentee().getId().equals(menteeId)) {
            throw new GeneralException(ErrorStatus.TASK_NOT_FOUND);
        }

        String pdfFileUrl = fileStorage.createPresignedGetUrl(task.getPdfFileUrl(), Duration.ofHours(24));
        return TaskDetailResponse.from(task, pdfFileUrl);
    }

    /**
     * 월별 과제 조회
     */
    public TaskMonthlyResponse getMonthlyTasks(Long menteeId, int year, int month) {
        validateMenteeExists(menteeId);

        LocalDate startDate = getMonthStartDate(year, month);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<Task> tasks = taskRepository.findByMenteeIdAndTaskDateBetween(menteeId, startDate, endDate);
        Map<Long, TaskCompletion> completionMap = getCompletionMap(tasks);
        Map<LocalDate, List<Task>> tasksByDate = tasks.stream()
                .collect(Collectors.groupingBy(Task::getTaskDate));

        List<TaskMonthlyDateResponse> dateResponses = new ArrayList<>();
        int daysWithTasks = 0;
        int totalTasks = tasks.size();
        int completedTasks = 0;

        for (int day = 1; day <= startDate.lengthOfMonth(); day++) {
            LocalDate currentDate = startDate.withDayOfMonth(day);
            List<Task> dayTasks = tasksByDate.getOrDefault(currentDate, List.of());

            List<TaskMonthlyItemResponse> taskItems = dayTasks.stream()
                    .map(task -> {
                        TaskCompletion completion = completionMap.get(task.getId());
                        boolean isCompleted = completion != null && Boolean.TRUE.equals(completion.getIsCompleted());
                        return TaskMonthlyItemResponse.builder()
                                .taskId(task.getId())
                                .taskName(task.getTaskName())
                                .subjectName(task.getSubject().getSubjectName())
                                .isCompleted(isCompleted)
                                .build();
                    })
                    .collect(Collectors.toList());

            int dayTotalTasks = taskItems.size();
            int dayCompletedTasks = (int) taskItems.stream()
                    .filter(TaskMonthlyItemResponse::getIsCompleted)
                    .count();

            if (dayTotalTasks > 0) {
                daysWithTasks++;
            }
            completedTasks += dayCompletedTasks;

            dateResponses.add(TaskMonthlyDateResponse.builder()
                    .date(currentDate)
                    .dayOfWeek(toKoreanDayOfWeek(currentDate.getDayOfWeek()))
                    .tasks(taskItems)
                    .totalTasks(dayTotalTasks)
                    .completedTasks(dayCompletedTasks)
                    .build());
        }

        TaskMonthlySummaryResponse summary = TaskMonthlySummaryResponse.builder()
                .totalDays(startDate.lengthOfMonth())
                .daysWithTasks(daysWithTasks)
                .totalTasks(totalTasks)
                .completedTasks(completedTasks)
                .completionRate(calculateCompletionRate(totalTasks, completedTasks))
                .build();

        return TaskMonthlyResponse.builder()
                .year(year)
                .month(month)
                .dates(dateResponses)
                .summary(summary)
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

        LearningMaterialType learningMaterialType = request.getLearningMaterialType();
        if (learningMaterialType == null) {
            learningMaterialType = LearningMaterialType.COLUMN;
        }


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
                .learningMaterialType(learningMaterialType)
                .pdfFileUrl(null)
                .columnContent(request.getColumnContent())
                .comment(request.getComment())
                .isFixed(Boolean.FALSE)
                .build();

        // 저장
        Task savedTask = taskRepository.save(task);

        // DTO 변환 및 반환
        String pdfFileUrl = fileStorage.createPresignedGetUrl(savedTask.getPdfFileUrl(), Duration.ofHours(24));
        return TaskCreateResponse.from(savedTask, subject.getSubjectName(), request.getSubjectCode(), pdfFileUrl);
    }

    /**
     * 오늘 할일 첨부파일 업로드
     */
    @Transactional
    public TaskAttachmentResponse uploadTaskAttachment(Long taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TASK_NOT_FOUND));

        String fileKey = fileStorage.uploadTaskAttachment(file);
        String oldFileKey = task.getPdfFileUrl();
        task.updateAttachmentUrl(fileKey);

        if (oldFileKey != null && !oldFileKey.equals(fileKey)) {
            fileStorage.deleteObject(oldFileKey);
        }

        String attachmentUrl = fileStorage.createPresignedGetUrl(fileKey, Duration.ofHours(24));
        return TaskAttachmentResponse.of(task.getId(), attachmentUrl);
    }

    /**
     * 과제 제출
     */
    @Transactional
    public TaskCompletionResponse submitTask(Long taskId, MultipartFile completionPhoto) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TASK_NOT_FOUND));

        MultipartFile photo = getSingleCompletionPhoto(completionPhoto);
        String photoKey = fileStorage.uploadTaskCompletionImage(photo);
        LocalDateTime completedAt = LocalDateTime.now();

        TaskCompletion existingCompletion = taskCompletionRepository.findByTaskId(taskId).orElse(null);
        String oldPhotoKey = existingCompletion != null ? existingCompletion.getCompletionPhotoUrl() : null;

        TaskCompletion completion = existingCompletion != null
                ? existingCompletion
                : TaskCompletion.builder()
                .task(task)
                .completionPhotoUrl(photoKey)
                .isCompleted(Boolean.TRUE)
                .completedAt(completedAt)
                .build();

        if (existingCompletion != null) {
            existingCompletion.overwriteCompletion(photoKey, completedAt);
        }

        TaskCompletion savedCompletion = taskCompletionRepository.save(completion);
        if (oldPhotoKey != null && !oldPhotoKey.equals(photoKey)) {
            fileStorage.deleteObject(oldPhotoKey);
        }
        String completionPhotoUrl = fileStorage.createPresignedGetUrl(photoKey, Duration.ofHours(24));
        return TaskCompletionResponse.from(savedCompletion, completionPhotoUrl);
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

    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }

    private MultipartFile getSingleCompletionPhoto(MultipartFile completionPhoto) {
        if (completionPhoto == null || completionPhoto.isEmpty()) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_COMPLETION_IMAGE);
        }

        return completionPhoto;
    }

    private LocalDate getMonthStartDate(int year, int month) {
        try {
            return LocalDate.of(year, month, 1);
        } catch (DateTimeException e) {
            throw new GeneralException(ErrorStatus.INVALID_TASK_DATE);
        }
    }

    private Map<Long, TaskCompletion> getCompletionMap(List<Task> tasks) {
        if (tasks.isEmpty()) {
            return new HashMap<>();
        }

        List<Long> taskIds = tasks.stream()
                .map(Task::getId)
                .collect(Collectors.toList());

        return taskCompletionRepository.findByTaskIdIn(taskIds).stream()
                .collect(Collectors.toMap(
                        completion -> completion.getTask().getId(),
                        completion -> completion
                ));
    }

    private Double calculateCompletionRate(int totalTasks, int completedTasks) {
        if (totalTasks == 0) {
            return 0.0;
        }
        double rate = (double) completedTasks * 100.0 / totalTasks;
        return BigDecimal.valueOf(rate).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private String toKoreanDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> "월";
            case TUESDAY -> "화";
            case WEDNESDAY -> "수";
            case THURSDAY -> "목";
            case FRIDAY -> "금";
            case SATURDAY -> "토";
            case SUNDAY -> "일";
        };
    }
}
