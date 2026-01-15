package com.schoolplanner.timetable.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    @PlanningId
    private String id;
    private String firstName;
    private String lastName;
    private Room homeRoom; // kabinets, kurā ikdienā strādā skolotāja
    private Set<TeachingUnit> qualifiedUnits; // skolotāja var pasniegt sākumskolas matemātiku, bet ne vidusskolas
    private Set<TimeSlot> workTimeSlots; // solver draudzīga versija
}