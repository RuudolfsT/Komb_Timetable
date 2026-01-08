package com.schoolplanner.timetable.controller;

import com.schoolplanner.timetable.controller.dto.SolveJob;
import com.schoolplanner.timetable.controller.dto.SolveStatus;
import com.schoolplanner.timetable.controller.dto.TimeTableResponse;
import com.schoolplanner.timetable.domain.TimeTable;
import com.schoolplanner.timetable.service.AsyncSolveService;
import com.schoolplanner.timetable.service.GenerateFromCsv;
import com.schoolplanner.timetable.service.SampleData;
import com.schoolplanner.timetable.service.TimeTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/timetable")
public class TimeTableController {

    private final AsyncSolveService asyncSolveService;
    private final TimeTableService timeTableService;

    public TimeTableController(
            AsyncSolveService asyncSolveService,
            TimeTableService timeTableService
    ) {
        this.asyncSolveService = asyncSolveService;
        this.timeTableService = timeTableService;
    }

    // Nosūtīt problēmu JSON formātā
    @PostMapping("/jobs")
    public ResponseEntity<Map<String, String>> submitJsonProblem(
            @RequestBody TimeTable problem
    ) {
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    // Nosūta problēmu no csv
    @PostMapping("/jobs/from-csv")
    public ResponseEntity<Map<String, String>> submitCsvProblem() {

        TimeTable problem = GenerateFromCsv.generateFromCsv("data/lesson_list.csv");
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    // Izgūst risinājuma statusu
    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<Map<String, Object>> getJobStatus(
            @PathVariable String jobId
    ) {
        SolveJob job = asyncSolveService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(Map.of("jobId", job.getJobId(), "status", job.getStatus()));
    }

    // Risinājuma rezultāta iegūšana
    @GetMapping("/jobs/{jobId}/solution")
    public ResponseEntity<TimeTableResponse> getSolution(
            @PathVariable String jobId
    ) {
        SolveJob job = asyncSolveService.getJob(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }

        if (job.getStatus() != SolveStatus.COMPLETED) {
            return ResponseEntity.status(409)
                    .body(null);
        }

        TimeTable solution = job.getSolution();
        var explanation = timeTableService.getSolutionManager().explain(solution);

        return ResponseEntity.ok(new TimeTableResponse(solution, explanation));
    }

    // Atgriež visus risinājumu statusus
    @GetMapping("/alljobs")
    public ResponseEntity<Map<String, Object>> getAllJobStatuses(
    ) {
        var jobs = asyncSolveService.getJobs();
        if (jobs == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = new HashMap<>();
        for (SolveJob job : jobs.values()) {
            result.put(job.getJobId(), job.getStatus().toString());
        }

        return ResponseEntity.ok(result);
    }
}
