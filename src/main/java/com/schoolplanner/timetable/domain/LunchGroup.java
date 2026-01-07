package com.schoolplanner.timetable.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class LunchGroup {
    private String name;
    private int minGrade;
    private int maxGrade;

    private List<TimeSlot> lunchTimeSlots; // satur vairāku dienu pusdienu laikus (Pirmdiena 12:45 - 13:00 vai Otrdiena 13:45 - 15:00 utt., bet praktiski tiks izmantots tas pats laiks visās dienās)

    public boolean appliesToGrade(int grade) {
        return grade >= minGrade && grade <= maxGrade;
    }
}