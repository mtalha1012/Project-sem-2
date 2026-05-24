package com.talha.quiz.projectsem2.model;

import java.util.ArrayList;
import java.util.List;

public class Quiz {
    // Attributes
    private Integer id;
    private String title;
    private String subject;
    private List<Question> questions = new ArrayList<>();

    // Constructor
    public Quiz(String title, String subject, List<Question> questions) {
        this.title = title;
        this.subject = subject;
        this.questions = questions;
    }

    public Quiz(String title, String subject) {
        this.title = title;
        this.subject = subject;
    }

    public Quiz() {
    }

    public Integer getId() {
        return id;
    }

    // Only to be used by service classes when loading from database
    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}
