package com.talha.quiz.projectsem2.model;

import java.time.LocalDateTime;

public class Result {
    // Attributes
    private Integer id;
    private Integer userId;
    private Integer quizId;
    private int score;
    private int totalQuestions;
    private LocalDateTime completedAt;

    // Constructor
    public Result(Integer userId, Integer quizId, int score, int totalQuestions) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.completedAt = LocalDateTime.now();
    }

    public Result() {
    }

    public Integer getId() {
        return id;
    }

    // Only to be used by service classes when loading from database
    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
