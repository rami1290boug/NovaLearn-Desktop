package com.novalearn.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

@Entity
@Table(name = "question")
public class Question implements Serializable {

    @Id
    @Column(name = "question_id", length = 255)
    private String questionId;

    @NotBlank
    @Column(name = "question", length = 255, nullable = false)
    private String question;

    @NotBlank
    @Column(name = "correction", length = 255, nullable = false)
    private String correction;

    @NotBlank
    @Column(name = "a", length = 255, nullable = false)
    private String optionA;

    @NotBlank
    @Column(name = "b", length = 255, nullable = false)
    private String optionB;

    @NotBlank
    @Column(name = "c", length = 255, nullable = false)
    private String optionC;

    @Column(name = "audio", length = 255, nullable = true)
    private String audio;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    // --- Getters and Setters ---
    public String getQuestionId() {
        return questionId;
    }
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }
    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrection() {
        return correction;
    }
    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public String getOptionA() {
        return optionA;
    }
    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }
    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }
    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getAudio() {
        return audio;
    }
    public void setAudio(String audio) {
        this.audio = audio;
    }

    public Quiz getQuiz() {
        return quiz;
    }
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }
}
