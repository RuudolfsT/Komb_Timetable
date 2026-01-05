package com.schoolplanner.timetable.domain;
import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningSolution
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeTable {
    @ValueRangeProvider(id = "timeslotRange")
    private List<TimeSlot> timeSlots;

    @PlanningEntityCollectionProperty
    private List<Lesson> lessons;

    @PlanningScore
    private HardSoftScore score;

    public TimeTable(List<TimeSlot> timeSlots, List<Lesson> lessons) {
        this.timeSlots = timeSlots;
        this.lessons = lessons;
        this.score = null; // solver piekārtos
    }
}
