package com.seolstudy.backend.domain.planner.service;

import com.seolstudy.backend.domain.planner.dto.PlannerCommentRequest;
import com.seolstudy.backend.domain.planner.dto.PlannerCommentResponse;
import com.seolstudy.backend.domain.planner.dto.PlannerCreateRequest;
import com.seolstudy.backend.domain.planner.dto.PlannerCreateResponse;
import com.seolstudy.backend.domain.planner.entity.Planner;
import com.seolstudy.backend.domain.planner.repository.PlannerRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import com.seolstudy.backend.global.storage.LocalFileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlannerService {

    private final PlannerRepository plannerRepository;
    private final LocalFileStorage localFileStorage;
    private final UserRepository userRepository;

    /**
     * 일자별 플래너 등록 (멘티)
     */
    @Transactional
    public PlannerCreateResponse createPlanner(PlannerCreateRequest request, MultipartFile image) {
        // 아래 역할 검증은 "권한 검증" 커밋으로 분리 예정
        User mentee = getUserOrThrow(request.getMenteeId());
        if (mentee.getRole() != UserRole.MENTEE) {
            throw new GeneralException(ErrorStatus.INVALID_MENTEE_ROLE);
        }

        LocalDate plannerDate = parseDate(request.getPlannerDate());
        // 이미지 업로드는 "업로드" 커밋으로 분리 예정
        String imageUrl = localFileStorage.storePlannerImage(image);

        Planner planner = Planner.builder()
                .menteeId(request.getMenteeId())
                .plannerDate(plannerDate)
                .content(request.getContent())
                // .imageUrl(imageUrl)
                .build();

        Planner savedPlanner = plannerRepository.save(planner);
        return PlannerCreateResponse.from(savedPlanner);
    }

    /**
     * 일자별 플래너 코멘트 등록 (멘토)
     */
    @Transactional
    public PlannerCommentResponse addMentorComment(PlannerCommentRequest request) {
        // 아래 역할 검증은 "권한 검증" 커밋으로 분리 예정
        User mentee = getUserOrThrow(request.getMenteeId());
        if (mentee.getRole() != UserRole.MENTEE) {
            throw new GeneralException(ErrorStatus.INVALID_MENTEE_ROLE);
        }

        User mentor = getUserOrThrow(request.getMentorId());
        if (mentor.getRole() != UserRole.MENTOR) {
            throw new GeneralException(ErrorStatus.INVALID_MENTOR_ROLE);
        }

        Planner planner = plannerRepository.findById(request.getPlannerId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLANNER_NOT_FOUND));

        if (!planner.getMenteeId().equals(request.getMenteeId())) {
            throw new GeneralException(ErrorStatus.PLANNER_ACCESS_DENIED);
        }

        planner.updateMentorComment(request.getMentorId(), request.getMentorComment());

        return PlannerCommentResponse.from(planner);
    }

    /**
     * 날짜 문자열 파싱 (yyyy-MM-dd)
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new GeneralException(ErrorStatus.INVALID_PLANNER_DATE);
        }
    }

    // 역할 검증용 (권한 검증 커밋으로 분리)
    private User getUserOrThrow(Long userId) {
        if (userId == null) {
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
