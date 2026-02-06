package com.seolstudy.backend.domain.task.entity;

import com.seolstudy.backend.domain.subject.entity.Subject;
import com.seolstudy.backend.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "task")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "task_id")
    private Long id;

    // Many(Task) to One(User) - 멘티
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentee_id", nullable = false)
    private User mentee;

    // Many(Task) to One(User) - 멘토
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private User mentor;

    // Many(Task) to One(Subject) - 과목
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    @Column(name = "task_goal", columnDefinition = "TEXT")
    private String taskGoal;

    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private TaskType taskType;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_material_type", nullable = false)
    private LearningMaterialType learningMaterialType;

    @Column(name = "pdf_file_url")
    private String pdfFileUrl;

    @Column(name = "column_content", columnDefinition = "TEXT")
    private String columnContent;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "is_fixed", nullable = false)
    private Boolean isFixed;

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
}
