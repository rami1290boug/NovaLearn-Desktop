package com.novalearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "quiz")
public class Quiz implements Serializable {

    @Id
    @Column(name = "quiz_id", length = 255)
    private String quizId;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String difficulty;

    @NotBlank
    @Column(length = 255, nullable = false)
    private String matiere;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Question> questions = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private User user;

    // --- Constructors ---
    public Quiz() {
    }



    // --- Getters and Setters ---
    public String getQuizId() {
        return quizId;
    }
    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public String getDifficulty() {
        return difficulty;
    }
    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getMatiere() {
        return matiere;
    }
    public void setMatiere(String matiere) {
        this.matiere = matiere;
    }

    public Set<Question> getQuestions() {
        return questions;
    }
    public void setQuestions(Set<Question> questions) {
        this.questions = questions;
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    public void addQuestion(Question question) {
        this.questions.add(question);
        question.setQuiz(this);
    }

    public void removeQuestion(Question question) {
        this.questions.remove(question);
        question.setQuiz(null);
    }
}
