package com.seolstudy.backend.domain.task.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "task_completion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TaskCompletion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "completion_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "study_time")
    private Integer studyTime;

    @Column(name = "completion_photo_url", nullable = false)
    private String completionPhotoUrl;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (completedAt == null) {
            completedAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void overwriteCompletion(String completionPhotoUrl, LocalDateTime completedAt) {
        if (completionPhotoUrl != null) {
            this.completionPhotoUrl = completionPhotoUrl;
        }
        if (completedAt != null) {
            this.completedAt = completedAt;
        }
        this.isCompleted = Boolean.TRUE;
    }
}
