package com.schoolplanner.timetable.service;

import ai.timefold.solver.core.api.solver.SolverManager;
import com.schoolplanner.timetable.domain.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

public class TimeTableService {

    private final SolverManager<TimeTable, UUID> solverManager;

    public TimeTableService(SolverManager<TimeTable, UUID> solverManager) {
        this.solverManager = solverManager;
    }
}
