package com.example.studyflow.data.model;

import com.google.firebase.Timestamp;

public class Schedule {
    private String id;
    private String subjectId;
    private String subjectName;
    private int dayOfWeek;      // 1=Thứ 2 ... 7=Chủ nhật
    private String startTime;   // "07:30"
    private String endTime;     // "09:30"
    private String room;
    private Timestamp createdAt;

    public Schedule() {}

    public Schedule(String subjectId, String subjectName,
                    int dayOfWeek, String startTime,
                    String endTime, String room) {
        this.subjectId   = subjectId;
        this.subjectName = subjectName;
        this.dayOfWeek   = dayOfWeek;
        this.startTime   = startTime;
        this.endTime     = endTime;
        this.room        = room;
        this.createdAt   = Timestamp.now();
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String v) { this.subjectId = v; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String v) { this.subjectName = v; }
    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int v) { this.dayOfWeek = v; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String v) { this.startTime = v; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String v) { this.endTime = v; }
    public String getRoom() { return room; }
    public void setRoom(String v) { this.room = v; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp v) { this.createdAt = v; }
}