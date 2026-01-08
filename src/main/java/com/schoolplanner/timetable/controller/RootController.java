package com.schoolplanner.timetable.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> getApiInfo() {
        return ResponseEntity.ok(Map.of(
                "message", "School Timetable Optimizer API",
                "version", "0.0.1-SNAPSHOT",
                "endpoints", Map.of(
                        "POST /api/timetable/jobs", "Submit a timetable problem as JSON",
                        "POST /api/timetable/jobs/from-csv", "Submit a timetable problem from CSV file",
                        "GET /api/timetable/jobs/{jobId}", "Get the status of a solving job",
                        "GET /api/timetable/jobs/{jobId}/solution", "Get the solution for a completed job",
                        "GET /api/timetable/alljobs", "Get all job statuses"
                )
        ));
    }
}
