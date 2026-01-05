package com.schoolplanner.timetable.service;

import com.schoolplanner.timetable.domain.*;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SampleData {

    public static TimeTable smallDemo() {

        TimeSlot slot1_Mon_0900 = new TimeSlot(1L, SchoolDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 45), false);
        TimeSlot slot2_Mon_1000 = new TimeSlot(2L, SchoolDay.MONDAY, LocalTime.of(10, 0), LocalTime.of(10, 45), false);
        TimeSlot slot3_Mon_1100 = new TimeSlot(3L, SchoolDay.MONDAY, LocalTime.of(11, 0), LocalTime.of(11, 45), false);

        List<TimeSlot> allTimeSlots = List.of(slot1_Mon_0900, slot2_Mon_1000, slot3_Mon_1100);

        Room room101 = new Room("101", RoomType.NORMAL, 30);
        Room room102 = new Room("102", RoomType.NORMAL, 30);
        Room chemLab = new Room("LAB_CHEM", RoomType.CHEMISTRY_LAB, 20);
        Room gym = new Room("GYM", RoomType.GYM, 50);

        List<Room> allRooms = List.of(room101, room102, chemLab, gym);

        SchoolClass class7A = new SchoolClass(1L, "7A", 7);
        SchoolClass class8B = new SchoolClass(2L, "8B", 8);

        List<SchoolClass> allClasses = List.of(class7A, class8B);

        Teacher anna = new Teacher();
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setWorkTimeSlots(allTimeSlots);
        anna.setHomeRoom(room101);

        Teacher janis = new Teacher();
        janis.setId("T2");
        janis.setFirstName("Jānis");
        janis.setLastName("Bērziņš");
        janis.setWorkTimeSlots(allTimeSlots);

        Teacher peteris = new Teacher();
        peteris.setId("T3");
        peteris.setFirstName("Pēteris");
        peteris.setLastName("Kļaviņš");
        peteris.setWorkTimeSlots(List.of(slot2_Mon_1000, slot3_Mon_1100));

        List<Teacher> allTeachers = List.of(anna, janis, peteris);

        TeachingUnit math7 = new TeachingUnit(Subject.MATH, 7, RoomType.NORMAL);
        TeachingUnit chem8 = new TeachingUnit(Subject.CHEMISTRY, 8, RoomType.CHEMISTRY_LAB);
        TeachingUnit sport7 = new TeachingUnit(Subject.SPORT, 7, RoomType.GYM);

        List<Lesson> lessons = new ArrayList<>();

        lessons.add(new Lesson(101L, math7, class7A, anna));

        lessons.add(new Lesson(102L, math7, class7A, anna));

        lessons.add(new Lesson(103L, chem8, class8B, janis));

        lessons.add(new Lesson(104L, sport7, class7A, peteris));

        return new TimeTable(
                allTimeSlots,
                allRooms,
                allTeachers,
                allClasses,
                lessons,
                HardSoftScore.ZERO
        );
    }
}