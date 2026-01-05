package com.schoolplanner.timetable.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Getter
@Setter
@NoArgsConstructor
public class Lesson {

    @PlanningId
    private Long id;

    private TeachingUnit teachingUnit;
    private SchoolClass schoolClass;

    @PlanningVariable(valueRangeProviderRefs = "teacherRange")
    private Teacher teacher;

    @PlanningVariable(valueRangeProviderRefs = "timeslotRange")
    private TimeSlot timeSlot;

    @PlanningVariable(valueRangeProviderRefs = "roomRange")
    private Room room;

    public Lesson(Long id, TeachingUnit teachingUnit, SchoolClass schoolClass) {
        this.id = id;
        this.teachingUnit = teachingUnit;
        this.schoolClass = schoolClass;
    }

    public boolean isValidRoom() {
        if (room == null || teachingUnit == null) return false;
        return room.getRoomType() == teachingUnit.getRoomType();
    }
}