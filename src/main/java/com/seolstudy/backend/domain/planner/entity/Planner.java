package com.seolstudy.backend.domain.planner.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "planner")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Planner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "planner_id")
    private Long id;

    @Column(name = "mentee_id", nullable = false)
    private Long menteeId;

    @Column(name = "mentor_id")
    private Long mentorId;

    @Column(name = "planner_date", nullable = false)
    private LocalDate plannerDate;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "mentor_comment", columnDefinition = "TEXT")
    private String mentorComment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void updateMentorComment(Long mentorId, String mentorComment) {
        this.mentorId = mentorId;
        this.mentorComment = mentorComment;
    }

}
