package com.seolstudy.backend.domain.mentoring.service;

import com.seolstudy.backend.domain.mentoring.dto.MenteeInfo;
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
import com.seolstudy.backend.global.payload.status.ErrorStatus;
import java.util.List;
import java.util.stream.Collectors;
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
            throw new GeneralException(ErrorStatus.MENTORING_INVALID_MENTOR_ROLE);
        }

        User mentee = userRepository.findById(request.getMenteeId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (mentee.getRole() != UserRole.MENTEE) {
            throw new GeneralException(ErrorStatus.MENTORING_INVALID_MENTEE_ROLE);
        }

        if (mentoringRepository.existsActiveRelationship(mentor.getId(), mentee.getId())) {
            throw new GeneralException(ErrorStatus.MENTORING_MENTEE_ALREADY_REGISTERED);
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
            throw new GeneralException(ErrorStatus.MENTORING_ALREADY_INACTIVE);
        }

        mentoring.deactivate();
    }

    /**
     * 멘토의 담당 멘티 목록 조회
     */
    public MenteeListResponse getMentees(String mentorUsername) {
        User mentor = userRepository.findByUsername(mentorUsername)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (mentor.getRole() != UserRole.MENTOR) {
            throw new GeneralException(ErrorStatus.MENTORING_INVALID_MENTOR_ROLE);
        }

        List<Mentoring> mentorings = mentoringRepository.findActiveByMentorId(mentor.getId());

        List<MenteeInfo> mentees = mentorings.stream()
                .map(mentoring -> MenteeInfo.builder()
                        .menteeId(mentoring.getMentee().getId())
                        .menteeName(mentoring.getMentee().getName())
                        .username(mentoring.getMentee().getUsername())
                        .build())
                .collect(Collectors.toList());

        return MenteeListResponse.builder()
                .mentorId(mentor.getId())
                .mentorName(mentor.getName())
                .mentees(mentees)
                .build();
    }

    /**
     * 멘티의 담당 멘토 조회
     */
    public MentorResponse getMentor(String menteeUsername) {
        User mentee = userRepository.findByUsername(menteeUsername)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        if (mentee.getRole() != UserRole.MENTEE) {
            throw new GeneralException(ErrorStatus.MENTORING_INVALID_MENTEE_ROLE);
        }

        Mentoring mentoring = mentoringRepository.findActiveByMenteeId(mentee.getId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.MENTORING_NOT_FOUND));

        User mentor = mentoring.getMentor();

        return MentorResponse.builder()
                .menteeId(mentee.getId())
                .menteeName(mentee.getName())
                .mentorId(mentor.getId())
                .mentorName(mentor.getName())
                .mentorUsername(mentor.getUsername())
                .build();
    }
}
