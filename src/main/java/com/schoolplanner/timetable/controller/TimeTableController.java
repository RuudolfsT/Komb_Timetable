package com.schoolplanner.timetable.controller;

import com.schoolplanner.timetable.domain.TimeTable;
import com.schoolplanner.timetable.service.GenerateFromCsv;
import com.schoolplanner.timetable.service.SampleData;
import com.schoolplanner.timetable.service.TimeTableService;
import io.micrometer.core.instrument.Timer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;


@RestController
@RequestMapping("/api/timetable")
public class TimeTableController {

    private final TimeTableService timeTableService;

    public TimeTableController(TimeTableService timeTableService) {
        this.timeTableService = timeTableService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> getSample() {
        return ResponseEntity.ok("Testing OK ! :D");
    }

    @GetMapping("/solve-sample")
    public ResponseEntity<TimeTable> solveSample() {
        try {
            TimeTable problem = SampleData.smallDemo();
            TimeTable solution = timeTableService.solve(problem);
            return ResponseEntity.ok(solution);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/solve-csv")
    public ResponseEntity<TimeTable> solveCsv() {
        try {
            TimeTable problem = GenerateFromCsv.generateFromCsv("src/main/java/com/schoolplanner/timetable/service/lesson_list.csv");
            TimeTable solution = timeTableService.solve(problem);
            return ResponseEntity.ok(solution);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
