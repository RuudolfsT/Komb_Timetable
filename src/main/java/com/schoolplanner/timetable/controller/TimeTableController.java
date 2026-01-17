package com.schoolplanner.timetable.controller;

import com.schoolplanner.timetable.controller.dto.SolveJob;
import com.schoolplanner.timetable.controller.dto.SolveStatus;
import com.schoolplanner.timetable.controller.dto.TimeTableResponse;
import com.schoolplanner.timetable.domain.TimeTable;
import com.schoolplanner.timetable.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping("/jobs/from-csv-test")
    public ResponseEntity<Map<String, String>> submitCsvTestProblem() {

        TimeTable problem = GenerateFromCsv.generateFromCsv("data/lesson_list_678.csv");
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PostMapping("/jobs/smalldemo")
    public ResponseEntity<Map<String, String>> submitSmallDemo() {

        TimeTable problem = SampleData.smallDemo();
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PostMapping("/jobs/testcase")
    public ResponseEntity<Map<String, String>> submitTestCase() {

        TimeTable problem = TestCase.testCase();
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    // Ielādē problēmu no augšupielādētiem CSV failiem
    @PostMapping("/jobs/upload")
    public ResponseEntity<Map<String, String>> submitFromCsvFiles(
            @RequestParam("roomsCsv") MultipartFile roomsCsv,
            @RequestParam("teachersCsv") MultipartFile teachersCsv,
            @RequestParam("lunchGroupsCsv") MultipartFile lunchGroupsCsv,
            @RequestParam("lessonsCsv") MultipartFile lessonsCsv,
            @RequestParam("classCount") int classCount
    ) {
        TimeTable problem = CsvDataLoader.generateFromUploadedCsvFiles(
                roomsCsv,
                teachersCsv,
                lunchGroupsCsv,
                lessonsCsv,
                classCount
        );
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    // Ielādē problēmu no visiem CSV failiem (rooms.csv, teachers.csv, lunch_groups.csv, lesson_list.csv)
    @PostMapping("/jobs/from-all-csv")
    public ResponseEntity<Map<String, String>> submitFromAllCsvFiles() {
        TimeTable problem = CsvDataLoader.generateFromAllCsvFiles();
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PostMapping("/jobs/from-all-csv-678-A-lim")
    public ResponseEntity<Map<String, String>> submitFromAllCsvFiles678Alim() {
        TimeTable problem = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers_limited.csv", "data/lunch_groups.csv", "data/lesson_list_678.csv", 1);
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PostMapping("/jobs/from-all-csv-678-AB")
    public ResponseEntity<Map<String, String>> submitFromAllCsvFiles678AB() {
        TimeTable problem = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers.csv", "data/lunch_groups.csv", "data/lesson_list_678.csv", 2);
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PostMapping("/jobs/from-all-csv-9-ABC")
    public ResponseEntity<Map<String, String>> submitFromAllCsvFiles9ABC() {
        TimeTable problem = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers.csv", "data/lunch_groups.csv", "data/lesson_list_9.csv", 3);
        String jobId = asyncSolveService.submit(problem);
        return ResponseEntity.accepted().body(Map.of("jobId", jobId));
    }

    @PostMapping("/jobs/from-all-csv-9-ABC-lim")
    public ResponseEntity<Map<String, String>> submitFromAllCsvFilesABC() {
        TimeTable problem = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers_limited.csv", "data/lunch_groups.csv", "data/lesson_list_9.csv", 3);
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
