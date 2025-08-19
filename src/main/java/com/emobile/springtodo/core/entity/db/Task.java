package com.emobile.springtodo.core.entity.db;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 64)
    private String title;

    @Column(nullable = false, length = 256)
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private Timestamp deadline;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "end_date")
    private Timestamp endDate;
}
