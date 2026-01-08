package com.schoolplanner.timetable.benchmark;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import com.schoolplanner.timetable.domain.TimeTable;

public class BenchmarkRunner {

    public static void main(String[] args) {
        PlannerBenchmarkFactory benchmarkFactory =
                PlannerBenchmarkFactory.createFromXmlResource("benchmarkConfig.xml");

        TimeTable problem = TimeTableBenchmarkFactory.generateProblem();

        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(problem);

        // palaiž benchmark
        benchmark.benchmarkAndShowReportInBrowser();
    }
}