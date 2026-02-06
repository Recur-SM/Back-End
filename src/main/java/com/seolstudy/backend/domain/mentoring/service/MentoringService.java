package com.seolstudy.backend.domain.mentoring.service;

import com.seolstudy.backend.domain.mentoring.dto.MentoringCreateRequest;
import com.seolstudy.backend.domain.mentoring.dto.MentoringResponse;
import com.seolstudy.backend.domain.mentoring.entity.Mentoring;
import com.seolstudy.backend.domain.mentoring.repository.MentoringRepository;
import com.seolstudy.backend.domain.user.entity.User;
import com.seolstudy.backend.domain.user.entity.UserRole;
import com.seolstudy.backend.domain.user.repository.UserRepository;
import com.seolstudy.backend.global.exception.GeneralException;
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MentoringService {

    private final MentoringRepository mentoringRepository;
    private final UserRepository userRepository;

    /**
     * 멘토-멘티 관계 등록
     */
    @Transactional
    public MentoringResponse createMentoring(String mentorUsername, MentoringCreateRequest request) {
        User mentor = userRepository.findByUsername(mentorUsername)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (mentor.getRole() != UserRole.MENTOR) {
            throw new IllegalArgumentException("멘토 역할을 가진 사용자만 등록할 수 있습니다.");
        }

        User mentee = userRepository.findById(request.getMenteeId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (mentee.getRole() != UserRole.MENTEE) {
            throw new IllegalArgumentException("멘티 역할을 가진 사용자만 등록할 수 있습니다.");
        }

        if (mentoringRepository.existsActiveRelationship(mentor.getId(), mentee.getId())) {
            throw new IllegalArgumentException("이미 등록된 멘티입니다.");
        }

        Mentoring mentoring = mentoringRepository.findByMentorIdAndMenteeId(mentor.getId(), mentee.getId())
                .map(existing -> {
                    existing.activate();
                    return existing;
                })
                .orElseGet(() -> Mentoring.builder()
                        .mentor(mentor)
                        .mentee(mentee)
                        .build());

        Mentoring savedMentoring = mentoringRepository.save(mentoring);

        return MentoringResponse.from(savedMentoring);
    }

    /**
     * 멘토-멘티 관계 비활성화
     */
    @Transactional
    public void deactivateMentoring(String mentorUsername, Long menteeId) {
        User mentor = userRepository.findByUsername(mentorUsername)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (mentor.getRole() != UserRole.MENTOR) {
            throw new GeneralException(ErrorStatus.FORBIDDEN);
        }

        Mentoring mentoring = mentoringRepository.findByMentorIdAndMenteeId(mentor.getId(), menteeId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (!mentoring.getIsActive()) {
            throw new IllegalArgumentException("이미 비활성화된 관계입니다.");
        }

        mentoring.deactivate();
    }
}
