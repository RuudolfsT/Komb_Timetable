package com.schoolplanner.timetable.controller.dto;

import org.springframework.web.multipart.MultipartFile;

public class CsvFilesRequest {

    private MultipartFile roomsCsv;
    private MultipartFile teachersCsv;
    private MultipartFile lunchGroupsCsv;
    private MultipartFile lessonsCsv;
    private int classCount;

    // Default constructor
    public CsvFilesRequest() {
    }

    public MultipartFile getRoomsCsv() {
        return roomsCsv;
    }

    public void setRoomsCsv(MultipartFile roomsCsv) {
        this.roomsCsv = roomsCsv;
    }

    public MultipartFile getTeachersCsv() {
        return teachersCsv;
    }

    public void setTeachersCsv(MultipartFile teachersCsv) {
        this.teachersCsv = teachersCsv;
    }

    public MultipartFile getLunchGroupsCsv() {
        return lunchGroupsCsv;
    }

    public void setLunchGroupsCsv(MultipartFile lunchGroupsCsv) {
        this.lunchGroupsCsv = lunchGroupsCsv;
    }

    public MultipartFile getLessonsCsv() {
        return lessonsCsv;
    }

    public void setLessonsCsv(MultipartFile lessonsCsv) {
        this.lessonsCsv = lessonsCsv;
    }

    public int getClassCount() {
        return classCount;
    }

    public void setClassCount(int classCount) {
        this.classCount = classCount;
    }
}
