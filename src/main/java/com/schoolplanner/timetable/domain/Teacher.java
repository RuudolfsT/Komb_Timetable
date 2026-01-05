package com.schoolplanner.timetable.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.*;
import java.util.List;
import java.util.Map;

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
    private List<TeachingUnit> qualifiedUnits; // skolotāja var pasniegt sākumskolas matemātiku, bet ne vidusskolas
    private Map<SchoolDay, List<TimeRange>> workingHours; // priekš skolotāju darba laika input
    private List<TimeSlot> workTimeSlots; // solver draudzīga versija
}