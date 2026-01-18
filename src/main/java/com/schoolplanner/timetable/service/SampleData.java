package com.schoolplanner.timetable.service;

import com.schoolplanner.timetable.domain.*;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import java.time.LocalTime;
import java.util.*;

public class SampleData {

    public static TimeTable smallDemo() {
        // 4 time slots per day, 5 days = 20 slots total
        List<TimeSlot> allTimeSlots = new ArrayList<>();
        long slotId = 1L;
        for (SchoolDay day : List.of(SchoolDay.MONDAY, SchoolDay.TUESDAY, SchoolDay.WEDNESDAY, SchoolDay.THURSDAY, SchoolDay.FRIDAY)) {
            allTimeSlots.add(new TimeSlot(slotId++, day, LocalTime.of(9, 0), LocalTime.of(9, 45)));
            allTimeSlots.add(new TimeSlot(slotId++, day, LocalTime.of(10, 0), LocalTime.of(10, 45)));
            allTimeSlots.add(new TimeSlot(slotId++, day, LocalTime.of(11, 0), LocalTime.of(11, 45)));
            allTimeSlots.add(new TimeSlot(slotId++, day, LocalTime.of(12, 0), LocalTime.of(12, 45)));
        }

        // 3 rooms - one of each type needed
        Room room101 = new Room("101", RoomType.NORMAL);
        Room chemLab = new Room("LAB_CHEM", RoomType.CHEMISTRY_LAB);
        Room gym = new Room("GYM", RoomType.GYM);

        List<Room> allRooms = List.of(room101, chemLab, gym);

        // 2 classes
        SchoolClass class7A = new SchoolClass(1L, "7A", 7);
        SchoolClass class8B = new SchoolClass(2L, "8B", 8);

        // Lunch group - 11:00 slot each day
        List<TimeSlot> lunchSlots = allTimeSlots.stream()
                .filter(ts -> ts.getStartTime().equals(LocalTime.of(11, 0)))
                .toList();
        LunchGroup lunchGroup1 = new LunchGroup(
                "Grades 7–12 lunch",
                7,
                12,
                lunchSlots
        );
        List<LunchGroup> allLunchGroups = List.of(lunchGroup1);

        // Teaching units
        TeachingUnit math7 = new TeachingUnit(1L, Subject.MATH, 7, RoomType.NORMAL);
        TeachingUnit math8 = new TeachingUnit(2L, Subject.MATH, 8, RoomType.NORMAL);
        TeachingUnit chem7 = new TeachingUnit(3L, Subject.CHEMISTRY, 7, RoomType.CHEMISTRY_LAB);
        TeachingUnit chem8 = new TeachingUnit(4L, Subject.CHEMISTRY, 8, RoomType.CHEMISTRY_LAB);
        TeachingUnit sport7 = new TeachingUnit(5L, Subject.SPORT, 7, RoomType.GYM);
        TeachingUnit sport8 = new TeachingUnit(6L, Subject.SPORT, 8, RoomType.GYM);

        List<SchoolClass> allClasses = List.of(class7A, class8B);

        // 2 teachers with good availability
        Teacher anna = new Teacher();
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, math8, chem7, chem8));
        anna.setWorkTimeSlots(new HashSet<>(allTimeSlots));
        anna.setHomeRoom(room101);

        Teacher janis = new Teacher();
        janis.setId("T2");
        janis.setFirstName("Jānis");
        janis.setLastName("Bērziņš");
        janis.setQualifiedUnits(Set.of(sport7, sport8, chem7, chem8));
        janis.setWorkTimeSlots(new HashSet<>(allTimeSlots));

        List<Teacher> allTeachers = List.of(anna, janis);

        // 10 lessons total - a realistic small week schedule
        // 5 lessons per class, spread across the week
        List<Lesson> lessons = new ArrayList<>();
        long lessonId = 101L;
        
        // Class 7A: 2 Math, 1 Chemistry, 2 Sport
        lessons.add(new Lesson(lessonId++, math7, class7A));
        lessons.add(new Lesson(lessonId++, math7, class7A));
        lessons.add(new Lesson(lessonId++, chem7, class7A));
        lessons.add(new Lesson(lessonId++, sport7, class7A));
        lessons.add(new Lesson(lessonId++, sport7, class7A));

        // Class 8B: 2 Math, 2 Chemistry, 1 Sport
        lessons.add(new Lesson(lessonId++, math8, class8B));
        lessons.add(new Lesson(lessonId++, math8, class8B));
        lessons.add(new Lesson(lessonId++, chem8, class8B));
        lessons.add(new Lesson(lessonId++, chem8, class8B));
        lessons.add(new Lesson(lessonId++, sport8, class8B));

        return new TimeTable(
                allTimeSlots,
                allRooms,
                allTeachers,
                allLunchGroups,
                allClasses,
                lessons,
                HardSoftScore.ZERO
        );
    }
}