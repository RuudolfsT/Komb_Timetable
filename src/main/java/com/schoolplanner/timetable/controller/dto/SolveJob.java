package com.schoolplanner.timetable.controller.dto;

import com.schoolplanner.timetable.domain.TimeTable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolveJob {

    private final String jobId;
    private volatile SolveStatus status;
    private volatile TimeTable solution;
    private volatile Exception error;

    public SolveJob(String jobId) {
        this.jobId = jobId;
        this.status = SolveStatus.PENDING;
    }
}
