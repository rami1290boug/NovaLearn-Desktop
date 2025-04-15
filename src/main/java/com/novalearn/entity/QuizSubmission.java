package com.novalearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_submission")
public class QuizSubmission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false, referencedColumnName = "quiz_id")
    private Quiz quiz;

    @Column(name = "responses", columnDefinition = "JSON", nullable = true)
    private String responsesJson;

    @NotNull
    @Column(name = "score", nullable = false)
    private Integer score = 0;

    @NotNull
    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    public QuizSubmission() {
        this.submittedAt = LocalDateTime.now();
    }

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
    }
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    /**
     * Get raw JSON string of responses.
     * You can parse it into a List/Map yourself.
     */
    public String getResponsesJson() {
        return responsesJson;
    }
    public void setResponsesJson(String responsesJson) {
        this.responsesJson = responsesJson;
    }

    public Integer getScore() {
        return score;
    }
    public void setScore(Integer score) {
        this.score = score;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }
}
