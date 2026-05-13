package com.example.studyflow.data.model;

import com.google.firebase.Timestamp;

public class Deadline {
    private String id;
    private String title;
    private String description;
    private String subjectId;
    private String subjectName;
    private Timestamp dueDate;
    private String priority;   // LOW / MEDIUM / HIGH
    private String status;     // TODO / IN_PROGRESS / DONE
    private boolean notified;
    private int reminderMinutes;
    private String flagColor;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Deadline() {}

    public Deadline(String title, String description, String subjectId,
                    String subjectName, Timestamp dueDate,
                    String priority, String status) {
        this.title = title;
        this.description = description;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.dueDate = dueDate;
        this.priority = priority;
        this.status = status;
        this.notified = false;
        this.reminderMinutes = 60;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public Timestamp getDueDate() { return dueDate; }
    public void setDueDate(Timestamp dueDate) { this.dueDate = dueDate; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }
    public int getReminderMinutes() { return reminderMinutes; }
    public void setReminderMinutes(int reminderMinutes) { this.reminderMinutes = reminderMinutes; }
    public String getFlagColor() { return flagColor; }
    public void setFlagColor(String flagColor) { this.flagColor = flagColor; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp t) { this.createdAt = t; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp t) { this.updatedAt = t; }
}