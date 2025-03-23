package com.example.dailydasoha.models;

import java.io.Serializable;

public class DailyData implements Serializable {
    private long date;
    private boolean isWorkingDay;
    private long attendance1to5;
    private long attendance6to8;
    private long attendance9to10;
    private String grainType;

    // Required empty constructor for Firestore
    public DailyData() {}

    // Getters and setters
    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public boolean isWorkingDay() { return isWorkingDay; }
    public void setWorkingDay(boolean workingDay) { isWorkingDay = workingDay; }

    public long getAttendance1to5() { return attendance1to5; }
    public void setAttendance1to5(long attendance1to5) { this.attendance1to5 = attendance1to5; }

    public long getAttendance6to8() { return attendance6to8; }
    public void setAttendance6to8(long attendance6to8) { this.attendance6to8 = attendance6to8; }

    public long getAttendance9to10() { return attendance9to10; }
    public void setAttendance9to10(long attendance9to10) { this.attendance9to10 = attendance9to10; }

    public String getGrainType() { return grainType; }
    public void setGrainType(String grainType) { this.grainType = grainType; }

    public long getTotalAttendance() {
        return attendance1to5 + attendance6to8 + attendance9to10;
    }
} 