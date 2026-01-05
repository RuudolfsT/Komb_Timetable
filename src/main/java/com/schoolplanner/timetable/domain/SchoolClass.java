package com.schoolplanner.timetable.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Reprezentē kādu skolas klasi, piemēram, 7A
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SchoolClass {
    private Long id;
    private String name; // A...
    private int grade; // 1-12
    private TimeRange lunchTime;
}