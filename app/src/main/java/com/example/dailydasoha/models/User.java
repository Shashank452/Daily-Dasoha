package com.example.dailydasoha.models;

public class User {
    private String uid;
    private String email;
    private String name;
    private String schoolName;

    public User(String uid, String email, String name, String schoolName) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.schoolName = schoolName;
    }

    // Getters and setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = schoolName; }
} 