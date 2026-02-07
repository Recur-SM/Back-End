package com.seolstudy.backend.domain.feedback.repository;

import com.seolstudy.backend.domain.feedback.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    /**
     * 특정 멘티의 피드백 목록 조회
     */
    @Query("SELECT f FROM Feedback f WHERE f.mentee.id = :menteeId ORDER BY f.feedbackDate DESC, f.createdAt DESC")
    List<Feedback> findByMenteeId(@Param("menteeId") Long menteeId);

    /**
     * 특정 멘토의 피드백 목록 조회
     */
    @Query("SELECT f FROM Feedback f WHERE f.mentor.id = :mentorId ORDER BY f.feedbackDate DESC, f.createdAt DESC")
    List<Feedback> findByMentorId(@Param("mentorId") Long mentorId);

    /**
     * 특정 과제의 피드백 조회
     */
    Optional<Feedback> findByTaskId(Long taskId);

    /**
     * 특정 과제에 피드백 작성 여부 확인
     */
    boolean existsByTaskId(Long taskId);

    /**
     * 특정 멘티의 특정 날짜 피드백 목록 조회
     */
    @Query("SELECT f FROM Feedback f WHERE f.mentee.id = :menteeId AND f.feedbackDate = :feedbackDate ORDER BY f.createdAt DESC")
    List<Feedback> findByMenteeIdAndFeedbackDate(@Param("menteeId") Long menteeId, @Param("feedbackDate") LocalDate feedbackDate);

    /**
     * 특정 멘티의 특정 날짜 피드백 작성 여부 확인
     */
    @Query("SELECT COUNT(f) > 0 FROM Feedback f WHERE f.mentee.id = :menteeId AND f.feedbackDate = :feedbackDate")
    boolean existsByMenteeIdAndFeedbackDate(@Param("menteeId") Long menteeId, @Param("feedbackDate") LocalDate feedbackDate);

    /**
     * 특정 멘티의 특정 과목 피드백 목록 조회
     */
    @Query("SELECT f FROM Feedback f WHERE f.mentee.id = :menteeId AND f.subjectId = :subjectId ORDER BY f.feedbackDate DESC, f.createdAt DESC")
    List<Feedback> findByMenteeIdAndSubjectId(@Param("menteeId") Long menteeId, @Param("subjectId") Long subjectId);
}