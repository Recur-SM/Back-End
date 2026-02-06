package com.seolstudy.backend.domain.mentoring.repository;

import com.seolstudy.backend.domain.mentoring.entity.Mentoring;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MentoringRepository extends JpaRepository<Mentoring, Long> {

    /**
     * 멘토의 활성화된 멘티 목록 조회
     */
    @Query("SELECT m FROM Mentoring m WHERE m.mentor.id = :mentorId AND m.isActive = true")
    List<Mentoring> findActiveByMentorId(@Param("mentorId") Long mentorId);

    /**
     * 멘티의 활성화된 멘토 조회
     */
    @Query("SELECT m FROM Mentoring m WHERE m.mentee.id = :menteeId AND m.isActive = true")
    Optional<Mentoring> findActiveByMenteeId(@Param("menteeId") Long menteeId);

    /**
     * 멘토-멘티 관계 존재 여부 확인 (활성화된 것만)
     */
    @Query("SELECT COUNT(m) > 0 FROM Mentoring m WHERE m.mentor.id = :mentorId AND m.mentee.id = :menteeId AND m.isActive = true")
    boolean existsActiveRelationship(@Param("mentorId") Long mentorId, @Param("menteeId") Long menteeId);

    /**
     * 특정 멘토-멘티 관계 조회 (활성화 여부 무관)
     */
    Optional<Mentoring> findByMentorIdAndMenteeId(Long mentorId, Long menteeId);
}
