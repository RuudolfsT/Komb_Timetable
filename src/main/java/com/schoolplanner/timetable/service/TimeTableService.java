package com.schoolplanner.timetable.service;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;
import com.schoolplanner.timetable.domain.*;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class TimeTableService {

    private final SolverManager<TimeTable, UUID> solverManager;

    public TimeTableService(SolverManager<TimeTable, UUID> solverManager) {
        this.solverManager = solverManager;
    }

    public TimeTable solve(TimeTable problem) throws ExecutionException, InterruptedException {
        UUID problemId = UUID.randomUUID();
        SolverJob<TimeTable, UUID> solverJob = solverManager.solve(problemId, problem);
        return solverJob.getFinalBestSolution();
    }
}
