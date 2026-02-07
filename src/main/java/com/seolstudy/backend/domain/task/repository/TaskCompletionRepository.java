package com.seolstudy.backend.domain.task.repository;

import com.seolstudy.backend.domain.task.entity.TaskCompletion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TaskCompletionRepository extends JpaRepository<TaskCompletion, Long> {
    boolean existsByTaskId(Long taskId);
    Optional<TaskCompletion> findByTaskId(Long taskId);
}
