package com.schoolplanner.timetable.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Lesson {
    @PlanningId
    private Long id;
    private TeachingUnit teachingUnit;
    private SchoolClass schoolClass;
    private Teacher teacher;

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    private TimeSlot timeSlot;

    public Lesson(
            Long id,
            TeachingUnit teachingUnit,
            SchoolClass schoolClass,
            Teacher teacher
    ) {
        this.id = id;
        this.teachingUnit = teachingUnit;
        this.schoolClass = schoolClass;
        this.teacher = teacher;
        this.timeSlot = null; // solver piekārtos
    }
}
