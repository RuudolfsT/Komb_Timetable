package com.schoolplanner.timetable.domain;

import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot {
    private Long id;
    private SchoolDay schoolDay;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isLunchBreak;
}
