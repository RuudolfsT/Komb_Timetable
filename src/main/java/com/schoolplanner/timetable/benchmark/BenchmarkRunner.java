package com.schoolplanner.timetable.benchmark;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import com.schoolplanner.timetable.domain.TimeTable;
import com.schoolplanner.timetable.service.CsvDataLoader;
import com.schoolplanner.timetable.service.GenerateFromCsv;

public class BenchmarkRunner {

    public static void main(String[] args) {
        PlannerBenchmarkFactory benchmarkFactory =
                PlannerBenchmarkFactory.createFromXmlResource("benchmarkConfig.xml");


        //Visas klases A
        TimeTable problem1 = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers.csv", "data/lunch_groups.csv", "data/lesson_list.csv", 1);

        //6, 7, 8 klase A ar ierobežotiem skolotāju laikiem
        TimeTable problem2 = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers_limited.csv", "data/lunch_groups.csv", "data/lesson_list_678.csv", 1);

        //9 klase A,B,C
        TimeTable problem3 = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers.csv", "data/lunch_groups.csv", "data/lesson_list_9.csv", 3);

        //9 klase A,B,C ar ierobežotiem skolotāju laikiem
        TimeTable problem4 = CsvDataLoader.generateFromAllCsvFiles("data/rooms.csv", "data/teachers_limited.csv", "data/lunch_groups.csv", "data/lesson_list_9.csv", 3);

        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(problem1, problem2, problem3, problem4);

        // palaiž benchmark
        benchmark.benchmarkAndShowReportInBrowser();
    }
}