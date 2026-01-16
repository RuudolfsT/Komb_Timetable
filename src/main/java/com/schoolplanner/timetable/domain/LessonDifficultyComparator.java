package com.schoolplanner.timetable.domain;

import java.util.Comparator;

public class LessonDifficultyComparator implements Comparator<Lesson> {

    @Override
    public int compare(Lesson a, Lesson b) {
        int aSlots = (a.getTeacher() == null) ? Integer.MAX_VALUE : a.getTeacher().getWorkTimeSlots().size();
        int bSlots = (b.getTeacher() == null) ? Integer.MAX_VALUE : b.getTeacher().getWorkTimeSlots().size();

        int availabilityComparison = Integer.compare(bSlots, aSlots);

        if (availabilityComparison != 0) {
            return availabilityComparison;
        }

        return Long.compare(a.getId(), b.getId());
    }
}