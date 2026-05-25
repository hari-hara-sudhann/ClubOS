package com.codeygen.clubos.entities.tasks;

import com.codeygen.clubos.entities.tasks.enums.SubmissionStatus;
import com.codeygen.clubos.entities.user.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class TaskSubmission {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String submissionId;

    @ManyToOne
    @JoinColumn(name="member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name="task_id")
    private Task task;
    private String proofOfSubmission;
    private LocalDateTime submittedAt;
    private SubmissionStatus status = SubmissionStatus.SUBMITTED;
}
