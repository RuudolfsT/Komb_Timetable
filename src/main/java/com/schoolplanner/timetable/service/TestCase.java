package com.schoolplanner.timetable.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.schoolplanner.timetable.domain.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestCase {

    public static TimeTable testCase() {

        TimeSlot slot_Mon_1 = new TimeSlot(1L, SchoolDay.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 10));
        TimeSlot slot_Mon_2 = new TimeSlot(2L, SchoolDay.MONDAY, LocalTime.of(9, 20), LocalTime.of(10, 0));
        TimeSlot slot_Mon_3 = new TimeSlot(3L, SchoolDay.MONDAY, LocalTime.of(10, 10), LocalTime.of(10, 50));
        TimeSlot slot_Mon_4 = new TimeSlot(4L, SchoolDay.MONDAY, LocalTime.of(11, 0), LocalTime.of(11, 40));
        TimeSlot slot_Mon_5 = new TimeSlot(5L, SchoolDay.MONDAY, LocalTime.of(11, 50), LocalTime.of(12, 30));
        TimeSlot slot_Mon_6 = new TimeSlot(6L, SchoolDay.MONDAY, LocalTime.of(12, 40), LocalTime.of(13, 20));
        TimeSlot slot_Mon_7 = new TimeSlot(7L, SchoolDay.MONDAY, LocalTime.of(13, 30), LocalTime.of(14, 10));
        TimeSlot slot_Mon_8 = new TimeSlot(8L, SchoolDay.MONDAY, LocalTime.of(14, 20), LocalTime.of(15, 0));
        TimeSlot slot_Mon_9 = new TimeSlot(9L, SchoolDay.MONDAY, LocalTime.of(15, 10), LocalTime.of(15, 50));
        TimeSlot slot_Mon_10 = new TimeSlot(10L, SchoolDay.MONDAY, LocalTime.of(16, 0), LocalTime.of(16, 40));

        List<TimeSlot> allTimeSlots = List.of(slot_Mon_1, slot_Mon_2, slot_Mon_3, slot_Mon_4, slot_Mon_5, slot_Mon_6, slot_Mon_7, slot_Mon_8, slot_Mon_9, slot_Mon_10);

        Room room101 = new Room("101", RoomType.NORMAL);
        Room room102 = new Room("102", RoomType.NORMAL);
        Room room103 = new Room("103", RoomType.NORMAL);
        Room chemLab = new Room("LAB_CHEM", RoomType.CHEMISTRY_LAB);
        Room music = new Room("MUSIC", RoomType.MUSIC);
        Room gym = new Room("GYM", RoomType.GYM);

        List<Room> allRooms = List.of(room101, room102, chemLab, music, gym);

        SchoolClass class5A = new SchoolClass(1L, "5A", 5);
        SchoolClass class8A = new SchoolClass(2L, "8A", 8);

        List<SchoolClass> allClasses = List.of(class5A, class8A);

        List<TimeSlot> lunchSlots1to6 = new ArrayList<>();
        List<TimeSlot> lunchSlots7to12 = new ArrayList<>();
        for (TimeSlot slot : allTimeSlots) {
            LocalTime start = slot.getStartTime();
            LocalTime end = slot.getEndTime();

            // 1–6 klašu pusdienas: 10:10–11:50 (katru dienu)
            if (start.isBefore(LocalTime.of(11, 50)) && end.isAfter(LocalTime.of(10, 10))) {
                lunchSlots1to6.add(slot);
            }

            // 7–12 klašu pusdienas: 11:00–12:40 (katru dienu)
            if (start.isBefore(LocalTime.of(12, 40)) && end.isAfter(LocalTime.of(11, 0))) {
                lunchSlots7to12.add(slot);
            }
        }

        List<LunchGroup> allLunchGroups = List.of(
                new LunchGroup("Grades 1–6 lunch", 1, 6, lunchSlots1to6),
                new LunchGroup("Grades 7–12 lunch", 7, 12, lunchSlots7to12)
        );

        TeachingUnit math5 = new TeachingUnit(1L, Subject.MATH, 5, RoomType.NORMAL);
        TeachingUnit math8 = new TeachingUnit(2L, Subject.MATH, 8, RoomType.NORMAL);
        TeachingUnit chem8 = new TeachingUnit(3L, Subject.CHEMISTRY, 8, RoomType.CHEMISTRY_LAB);
        TeachingUnit sport5 = new TeachingUnit(4L, Subject.SPORT, 5, RoomType.GYM);
        TeachingUnit sport8 = new TeachingUnit(5L, Subject.SPORT, 5, RoomType.GYM);
        TeachingUnit music5 = new TeachingUnit(6L, Subject.MUSIC, 5, RoomType.MUSIC);
        TeachingUnit music8 = new TeachingUnit(7L, Subject.MUSIC, 8, RoomType.MUSIC);
        TeachingUnit art5 = new TeachingUnit(8L, Subject.ART, 5, RoomType.NORMAL);
        TeachingUnit art8 = new TeachingUnit(9L, Subject.ART, 8, RoomType.NORMAL);
        TeachingUnit latvian5 = new TeachingUnit(10L, Subject.LATVIAN, 5, RoomType.NORMAL);

        Teacher anna = new Teacher();
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math5, math8));
        anna.setWorkTimeSlots(new HashSet<>(Set.of(slot_Mon_1, slot_Mon_2, slot_Mon_3, slot_Mon_4, slot_Mon_5)));
        anna.setHomeRoom(room101);

        Teacher janis = new Teacher();
        janis.setId("T2");
        janis.setFirstName("Jānis");
        janis.setLastName("Bērziņš");
        janis.setQualifiedUnits(Set.of(sport5, sport8));
        janis.setWorkTimeSlots(new HashSet<>(Set.of(slot_Mon_5, slot_Mon_6, slot_Mon_7)));
        janis.setHomeRoom(gym);

        Teacher peteris = new Teacher();
        peteris.setId("T3");
        peteris.setFirstName("Pēteris");
        peteris.setLastName("Kļaviņš");
        peteris.setQualifiedUnits(Set.of(chem8));
        peteris.setWorkTimeSlots(new HashSet<>(Set.of(slot_Mon_1, slot_Mon_2, slot_Mon_3)));
        peteris.setHomeRoom(chemLab);

        Teacher ieva = new Teacher();
        ieva.setId("T4");
        ieva.setFirstName("Ieva");
        ieva.setLastName("Lapiņa");
        ieva.setQualifiedUnits(Set.of(music5, music8));
        ieva.setWorkTimeSlots(new HashSet<>(Set.of(slot_Mon_5, slot_Mon_6, slot_Mon_7, slot_Mon_8)));
        ieva.setHomeRoom(music);

        Teacher dace = new Teacher();
        dace.setId("T5");
        dace.setFirstName("Dace");
        dace.setLastName("Lapsa");
        dace.setQualifiedUnits(Set.of(art5, art8));
        dace.setWorkTimeSlots(new HashSet<>(Set.of(slot_Mon_1, slot_Mon_2, slot_Mon_3, slot_Mon_4, slot_Mon_5)));
        dace.setHomeRoom(room102);

        Teacher aija = new Teacher();
        aija.setId("T6");
        aija.setFirstName("Aija");
        aija.setLastName("Pele");
        aija.setQualifiedUnits(Set.of(latvian5));
        aija.setWorkTimeSlots(new HashSet<>(allTimeSlots));
        aija.setHomeRoom(room103);

        List<Teacher> allTeachers = List.of(anna, janis, peteris, ieva, dace, aija);

        List<Lesson> lessons = new ArrayList<>();

        //5.klase
        lessons.add(new Lesson(501L, math5, class5A));

        lessons.add(new Lesson(502L, sport5, class5A));

        lessons.add(new Lesson(503L, art5, class5A));
        lessons.add(new Lesson(504L, art5, class5A));

        lessons.add(new Lesson(505L, music5, class5A));

        lessons.add(new Lesson(506L, latvian5, class5A));


        //8.klase
        lessons.add(new Lesson(801L, math8, class8A));

        lessons.add(new Lesson(802L, sport8, class8A));

        lessons.add(new Lesson(803L, art8, class8A));
        lessons.add(new Lesson(804L, art8, class8A));

        lessons.add(new Lesson(805L, music8, class8A));

        lessons.add(new Lesson(806L, chem8, class8A));


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