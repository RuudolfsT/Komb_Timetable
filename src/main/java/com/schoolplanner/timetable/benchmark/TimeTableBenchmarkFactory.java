package com.schoolplanner.timetable.benchmark;

import com.schoolplanner.timetable.domain.TimeTable;
import com.schoolplanner.timetable.service.GenerateFromCsv;
import com.schoolplanner.timetable.service.TestCase;

public class TimeTableBenchmarkFactory {

    public static TimeTable generateProblem() {
        return GenerateFromCsv.generateFromCsv("data/lesson_list.csv");
    }
//    public static TimeTable generateProblem() {
//        return TestCase.testCase();
//    }
}
