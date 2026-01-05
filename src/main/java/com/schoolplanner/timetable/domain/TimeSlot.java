package com.schoolplanner.timetable.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.*;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
    @PlanningId
    private Long id;
    private SchoolDay schoolDay;
    private LocalTime startTime;
    private LocalTime endTime;
}