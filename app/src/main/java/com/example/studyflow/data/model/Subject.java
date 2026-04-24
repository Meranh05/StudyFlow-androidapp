package com.example.studyflow.data.model;

import com.google.firebase.Timestamp;

public class Subject {
    private String id;
    private String name;
    private String lecturer;
    private int credits;
    private String colorTag;   // hex, e.g. "#5E92F3"
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Subject() {}

    public Subject(String name, String lecturer, int credits, String colorTag) {
        this.name = name;
        this.lecturer = lecturer;
        this.credits = credits;
        this.colorTag = colorTag;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }
    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
    public String getColorTag() { return colorTag; }
    public void setColorTag(String colorTag) { this.colorTag = colorTag; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp t) { this.createdAt = t; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp t) { this.updatedAt = t; }
}