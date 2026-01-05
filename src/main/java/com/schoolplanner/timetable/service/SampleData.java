package com.schoolplanner.timetable.service;

import com.schoolplanner.timetable.domain.*;

import java.time.LocalTime;
import java.util.List;

public class SampleData {

    public static TimeTable smallDemo() {

        // 2 laika intervāli pirmdienā
        TimeSlot slot1 = new TimeSlot(
                1L,
                SchoolDay.MONDAY,
                LocalTime.of(9, 0),
                LocalTime.of(9, 45),
                false
        );

        TimeSlot slot2 = new TimeSlot(
                2L,
                SchoolDay.MONDAY,
                LocalTime.of(10, 0),
                LocalTime.of(10, 45),
                false
        );

        List<TimeSlot> timeSlots = List.of(slot1, slot2);

        Teacher anna = new Teacher();
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setWorkTimeSlots(timeSlots); // strādā abos laika intervālos

        Teacher janis = new Teacher();
        janis.setFirstName("Jānis");
        janis.setLastName("Bērziņš");
        janis.setWorkTimeSlots(timeSlots); // strādā abos laika intervālos

        SchoolClass schoolClass = new SchoolClass(1L, "A", 7);

        TeachingUnit math7 = new TeachingUnit(Subject.MATH, 7, RoomType.NORMAL);
        TeachingUnit latvian7 = new TeachingUnit(Subject.LATVIAN, 7, RoomType.NORMAL);

        // Anna māca 2 priekšmetus -> tiem jābūt dažādos laikos
        Lesson lesson1 = new Lesson(
            1L,
                math7,
                schoolClass,
                anna
        );

        Lesson lesson2 = new Lesson(
                2L,
                math7,
                schoolClass,
                anna
        );

        // Jānis māca vienu priekšmetu
        Lesson lesson3 = new Lesson(
                3L,
                math7,
                schoolClass,
                janis
        );

        List<Lesson> lessons = List.of(lesson1, lesson2, lesson3);

        return new TimeTable(timeSlots, lessons);
    }
}
