package com.schoolplanner.timetable.service;

import com.schoolplanner.timetable.controller.dto.SolveJob;
import com.schoolplanner.timetable.controller.dto.SolveStatus;
import com.schoolplanner.timetable.domain.TimeTable;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Getter
public class AsyncSolveService {

    private final TimeTableService timeTableService;
    private final Map<String, SolveJob> jobs = new ConcurrentHashMap<>();

    public AsyncSolveService(TimeTableService timeTableService) {
        this.timeTableService = timeTableService;
    }

    public String submit(TimeTable problem) {
        // Izveido jaunu procesu, kurš risinās problēmu
        String jobId = UUID.randomUUID().toString();
        SolveJob job = new SolveJob(jobId);
        jobs.put(jobId, job);

        CompletableFuture.runAsync(() -> {
            job.setStatus(SolveStatus.SOLVING);
            try {
                // Mēģina risināt problēmu
                TimeTable solution = timeTableService.solve(problem);
                // Pēc atrisināšanas uzstāda procesam jauno atrisinājumu
                job.setSolution(solution);
                job.setStatus(SolveStatus.COMPLETED);
            } catch (Exception e) {
                job.setError(e);
                job.setStatus(SolveStatus.FAILED);
            }
        });

        return jobId;
    }

    public SolveJob getJob(String jobId) {
        return jobs.get(jobId);
    }
}
