package com.schoolplanner.timetable.benchmark;

import ai.timefold.solver.benchmark.api.PlannerBenchmark;
import ai.timefold.solver.benchmark.api.PlannerBenchmarkFactory;
import com.schoolplanner.timetable.domain.TimeTable;

public class BenchmarkRunner {

    public static void main(String[] args) {
        // 1. Load the configuration
        PlannerBenchmarkFactory benchmarkFactory =
                PlannerBenchmarkFactory.createFromXmlResource("benchmarkConfig.xml");

        // 2. Generate the problem instance from your CSV
        TimeTable problem = TimeTableBenchmarkFactory.generateProblem();

        // 3. Pass the problem instance to the benchmark builder
        PlannerBenchmark benchmark = benchmarkFactory.buildPlannerBenchmark(problem);

        // 4. Run the benchmark
        benchmark.benchmarkAndShowReportInBrowser();
    }
}