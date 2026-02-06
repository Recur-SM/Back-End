package com.seolstudy.backend.domain.task.repository;

import com.seolstudy.backend.domain.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * 특정 멘티의 특정 날짜 과제 목록 조회
     */
    @Query("SELECT t FROM Task t WHERE t.mentee.id = :menteeId AND t.taskDate = :taskDate ORDER BY t.createdAt ASC")
    List<Task> findByMenteeIdAndTaskDate(@Param("menteeId") Long menteeId, @Param("taskDate") LocalDate taskDate);

    /**
     * 특정 멘티의 특정 날짜, 특정 과목 과제 목록 조회
     */
    @Query("SELECT t FROM Task t WHERE t.mentee.id = :menteeId AND t.taskDate = :taskDate AND t.subject.id = :subjectId ORDER BY t.createdAt ASC")
    List<Task> findByMenteeIdAndTaskDateAndSubjectId(
            @Param("menteeId") Long menteeId,
            @Param("taskDate") LocalDate taskDate,
            @Param("subjectId") Long subjectId
    );
}