package com.findclass.ajvm.findclassapp.Model;

public class Subject_Professor {
    private User user;
    private Subject subject;
    private Professor_Subject professorSubject;

    public Subject_Professor() { }

    public Subject_Professor(User user, Subject subject, Professor_Subject professorSubject) {
        this.user = user;
        this.subject = subject;
        this.professorSubject = professorSubject;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Professor_Subject getProfessorSubject() {
        return professorSubject;
    }

    public void setProfessorSubject(Professor_Subject professorSubject) {
        this.professorSubject = professorSubject;
    }
}
