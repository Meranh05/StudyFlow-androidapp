package com.example.studyflow.data.model;

import com.google.firebase.Timestamp;

public class Note {
    private String id;
    private String title;
    private String content;
    private String subjectId;
    private String subjectName;
    private String attachmentUrl;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public Note() {}

    public Note(String title, String content, String subjectId, String subjectName) {
        this.title = title;
        this.content = content;
        this.subjectId = subjectId;
        this.subjectName = subjectName;
        this.createdAt = Timestamp.now();
        this.updatedAt = Timestamp.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getSubjectId() { return subjectId; }
    public void setSubjectId(String subjectId) { this.subjectId = subjectId; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public String getAttachmentUrl() { return attachmentUrl; }
    public void setAttachmentUrl(String url) { this.attachmentUrl = url; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp t) { this.createdAt = t; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp t) { this.updatedAt = t; }
}