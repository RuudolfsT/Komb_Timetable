package com.schoolplanner.timetable.domain;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Teacher {
    private String firstName;
    private String lastName;
    private Room homeRoom; // kabinets, kurā ikdienā strādā skolotāja
    private List<TeachingUnit> qualifiedUnits; // skolotāja var pasniegt sākumskolas matemātiku, bet ne vidusskolas

    private Map<SchoolDay, List<TimeRange>> workingHours; // priekš skolotāju darba laika input
    private List<TimeSlot> workTimeSlots; // solver draudzīga versija
}
