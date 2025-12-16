package com.schoolplanner.timetable.controller;

import com.schoolplanner.timetable.service.TimeTableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/timetable")
public class TimeTableController {

    //private final TimeTableService timeTableService;

    //public TimeTableController(TimeTableService timeTableService) {
    //    this.timeTableService = timeTableService;
    //}

    @GetMapping("/test")
    public ResponseEntity<String> getSample() {
        return ResponseEntity.ok("Testing OK ! :D");
    }
}
