package com.schoolplanner.timetable.solver;

import com.schoolplanner.timetable.domain.*;
import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class TimeTableConstraintProviderTest {

    Room room101 = new Room("101", RoomType.NORMAL);
    Room room102 = new Room("102", RoomType.NORMAL);
    Room room103 = new Room("103", RoomType.NORMAL);
    Room gym1 = new Room("104", RoomType.GYM);
    Room chem1 = new Room("104", RoomType.CHEMISTRY_LAB);

    TimeSlot slot_Mon_0900 = new TimeSlot(1L, SchoolDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 40));
    TimeSlot slot_Mon_1000 = new TimeSlot(2L, SchoolDay.MONDAY, LocalTime.of(10, 0), LocalTime.of(10, 40));
    TimeSlot slot_Mon_1100 = new TimeSlot(3L, SchoolDay.MONDAY, LocalTime.of(11, 0), LocalTime.of(11, 40));
    TimeSlot slot_Mon_1200 = new TimeSlot(4L, SchoolDay.MONDAY, LocalTime.of(12, 0), LocalTime.of(12, 40));
    TimeSlot slot_Mon_1300 = new TimeSlot(5L, SchoolDay.MONDAY, LocalTime.of(13, 0), LocalTime.of(13, 40));
    TimeSlot slot_Mon_1400 = new TimeSlot(6L, SchoolDay.MONDAY, LocalTime.of(14, 0), LocalTime.of(14, 40));
    TimeSlot slot_Mon_1500 = new TimeSlot(7L, SchoolDay.MONDAY, LocalTime.of(15, 0), LocalTime.of(15, 40));
    TimeSlot slot_Mon_1600 = new TimeSlot(8L, SchoolDay.MONDAY, LocalTime.of(16, 0), LocalTime.of(16, 40));
    TimeSlot slot_Mon_1700 = new TimeSlot(9L, SchoolDay.MONDAY, LocalTime.of(17, 0), LocalTime.of(17, 40));
    TimeSlot slot_Mon_1800 = new TimeSlot(10L, SchoolDay.MONDAY, LocalTime.of(18, 0), LocalTime.of(18, 40));
    TimeSlot slot_Tue_0900 = new TimeSlot(11L, SchoolDay.TUESDAY, LocalTime.of(9, 0), LocalTime.of(9, 40));
    TimeSlot slot_Tue_1000 = new TimeSlot(11L, SchoolDay.TUESDAY, LocalTime.of(10, 0), LocalTime.of(10, 40));
    TimeSlot slot_Wed_0900 = new TimeSlot(11L, SchoolDay.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(9, 40));
    TimeSlot slot_Wed_1000 = new TimeSlot(11L, SchoolDay.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(10, 40));
    TimeSlot slot_Thu_0900 = new TimeSlot(11L, SchoolDay.THURSDAY, LocalTime.of(9, 0), LocalTime.of(9, 40));
    TimeSlot slot_Thu_1000 = new TimeSlot(11L, SchoolDay.THURSDAY, LocalTime.of(10, 0), LocalTime.of(10, 40));
    TimeSlot slot_Fri_0900 = new TimeSlot(11L, SchoolDay.FRIDAY, LocalTime.of(9, 0), LocalTime.of(9, 40));
    TimeSlot slot_Fri_1000 = new TimeSlot(11L, SchoolDay.FRIDAY, LocalTime.of(10, 0), LocalTime.of(10, 40));
    TimeSlot slot_Fri_1100 = new TimeSlot(11L, SchoolDay.FRIDAY, LocalTime.of(11, 0), LocalTime.of(11, 40));
    TimeSlot slot_Fri_1200 = new TimeSlot(11L, SchoolDay.FRIDAY, LocalTime.of(12, 0), LocalTime.of(12, 40));


    TeachingUnit math6 = new TeachingUnit(0L, Subject.MATH, 6, RoomType.NORMAL);
    TeachingUnit math7 = new TeachingUnit(1L, Subject.MATH, 7, RoomType.NORMAL);
    TeachingUnit chem7 = new TeachingUnit(2L, Subject.CHEMISTRY, 7, RoomType.NORMAL);
    TeachingUnit sport7 = new TeachingUnit(3L, Subject.SPORT, 7, RoomType.GYM);
    TeachingUnit art7 = new TeachingUnit(3L, Subject.ART, 7, RoomType.NORMAL);

    SchoolClass class6A = new SchoolClass(1L, "6A", 6);
    SchoolClass class7A = new SchoolClass(2L, "7A", 7);
    SchoolClass class7B = new SchoolClass(3L, "7B", 7);
    SchoolClass class7C = new SchoolClass(4L, "7C", 7);

    Teacher anna = new Teacher();
    Teacher janis = new Teacher();
    Teacher laura = new Teacher();

    LunchGroup lunch1_6 = new LunchGroup("Grades 1–6 lunch", 1, 6, List.of(slot_Mon_1200));
    LunchGroup lunch7_12 = new LunchGroup("Grades 7–12 lunch", 7, 12, List.of(slot_Mon_1300));

    ConstraintVerifier<TimeTableConstraintProvider, TimeTable> constraintVerifier = ConstraintVerifier.build(
            new TimeTableConstraintProvider(),
            TimeTable.class,
            Lesson.class
    );

    @Test
    void teacherConflict() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        anna.setHomeRoom(room101);

        Lesson nonConflictingLesson = new Lesson(101L, math7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_0900);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, math7, class7B);
        conflictingLesson.setTimeSlot(slot_Mon_0900);
        conflictingLesson.setRoom(room101);
        conflictingLesson.setTeacher(anna);

        Lesson nonConflictingLesson2 = new Lesson(103L, math7, class7C);
        nonConflictingLesson2.setTimeSlot(slot_Mon_1000);
        nonConflictingLesson2.setRoom(room102);
        nonConflictingLesson2.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherConflict)
                .given(nonConflictingLesson, conflictingLesson)
                .penalizesBy(1);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherConflict)
                .given(nonConflictingLesson, nonConflictingLesson2)
                .penalizesBy(0);
    }

    @Test
    void roomConflict() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        anna.setHomeRoom(room101);

        janis.setId("T2");
        janis.setFirstName("Janis");
        janis.setLastName("Ozols");
        janis.setQualifiedUnits(Set.of(math7));
        janis.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        janis.setHomeRoom(room102);

        Lesson nonConflictingLesson = new Lesson(101L, math7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_0900);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, math7, class7B);
        conflictingLesson.setTimeSlot(slot_Mon_0900);
        conflictingLesson.setRoom(room101);
        conflictingLesson.setTeacher(janis);

        Lesson nonConflictingLesson2 = new Lesson(103L, math7, class7C);
        nonConflictingLesson2.setTimeSlot(slot_Mon_1000);
        nonConflictingLesson2.setRoom(room102);
        nonConflictingLesson2.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomConflict)
                .given(nonConflictingLesson, conflictingLesson)
                .penalizesBy(1);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomConflict)
                .given(nonConflictingLesson, nonConflictingLesson2)
                .penalizesBy(0);
    }

    @Test
    void studentLunchBreak() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_1200, slot_Mon_1300)));
        anna.setHomeRoom(room101);

        janis.setId("T2");
        janis.setFirstName("Janis");
        janis.setLastName("Ozols");
        janis.setQualifiedUnits(Set.of(math6));
        janis.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_1200, slot_Mon_1300)));
        janis.setHomeRoom(room102);

        Lesson conflictingLesson_7 = new Lesson(101L, math7, class7A);
        conflictingLesson_7.setTimeSlot(slot_Mon_1300);
        conflictingLesson_7.setRoom(room101);
        conflictingLesson_7.setTeacher(anna);

        Lesson nonConflictingLesson_7 = new Lesson(102L, math7, class7A);
        nonConflictingLesson_7.setTimeSlot(slot_Mon_1200);
        nonConflictingLesson_7.setRoom(room101);
        nonConflictingLesson_7.setTeacher(anna);

        Lesson conflictingLesson_6 = new Lesson(103L, math6, class6A);
        conflictingLesson_6.setTimeSlot(slot_Mon_1200);
        conflictingLesson_6.setRoom(room102);
        conflictingLesson_6.setTeacher(janis);

        Lesson nonConflictingLesson_6 = new Lesson(104L, math6, class6A);
        nonConflictingLesson_6.setTimeSlot(slot_Mon_1300);
        nonConflictingLesson_6.setRoom(room102);
        nonConflictingLesson_6.setTeacher(janis);

        //7. klasei stunda 13:00 (pusdienu laiks 7-12)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentLunchBreak)
                .given(lunch1_6, lunch7_12, conflictingLesson_7)
                .penalizesBy(1);

        //7. klasei stunda 12:00 (pusdienu laiks 1-6)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentLunchBreak)
                .given(lunch1_6, lunch7_12, nonConflictingLesson_7)
                .penalizesBy(0);

        //6. klasei stunda 12:00 (pusdienu laiks 1-6)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentLunchBreak)
                .given(lunch1_6, lunch7_12, conflictingLesson_6)
                .penalizesBy(1);

        //6. klasei stunda 13:00 (pusdienu laiks 7-12)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentLunchBreak)
                .given(lunch1_6, lunch7_12, nonConflictingLesson_6)
                .penalizesBy(0);

    }

    @Test
    void studentGroupConflict() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        anna.setHomeRoom(room101);

        janis.setId("T2");
        janis.setFirstName("Janis");
        janis.setLastName("Ozols");
        janis.setQualifiedUnits(Set.of(math7));
        janis.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        janis.setHomeRoom(room102);

        laura.setId("T3");
        laura.setFirstName("Laura");
        laura.setLastName("Zariņa");
        laura.setQualifiedUnits(Set.of(math7));
        laura.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        laura.setHomeRoom(room103);

        Lesson nonConflictingLesson = new Lesson(101L, math7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_0900);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, math7, class7A);
        conflictingLesson.setTimeSlot(slot_Mon_0900);
        conflictingLesson.setRoom(room102);
        conflictingLesson.setTeacher(janis);

        Lesson nonConflictingLesson2 = new Lesson(103L, math7, class7C);
        nonConflictingLesson2.setTimeSlot(slot_Mon_0900);
        nonConflictingLesson2.setRoom(room103);
        nonConflictingLesson2.setTeacher(laura);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGroupConflict)
                .given(nonConflictingLesson, conflictingLesson)
                .penalizesBy(1);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGroupConflict)
                .given(nonConflictingLesson, nonConflictingLesson2)
                .penalizesBy(0);
    }

    @Test
    void roomTypeMatch() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, chem7, sport7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Mon_1100)));
        anna.setHomeRoom(room101);

        Lesson nonConflictingLesson = new Lesson(101L, sport7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_0900);
        nonConflictingLesson.setRoom(gym1);
        nonConflictingLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, chem7, class7B);
        conflictingLesson.setTimeSlot(slot_Mon_1000);
        conflictingLesson.setRoom(gym1);
        conflictingLesson.setTeacher(anna);


        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomTypeMatch)
                .given(nonConflictingLesson)
                .penalizesBy(0);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomTypeMatch)
                .given(conflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void qualifiedUnitMatch() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Mon_1100)));
        anna.setHomeRoom(room101);

        Lesson firstLesson = new Lesson(101L, math7, class7A);
        firstLesson.setTimeSlot(slot_Mon_0900);
        firstLesson.setRoom(room101);
        firstLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, sport7, class7B);
        conflictingLesson.setTimeSlot(slot_Mon_1000);
        conflictingLesson.setRoom(gym1);
        conflictingLesson.setTeacher(anna);

        Lesson conflictingLesson2 = new Lesson(103L, math6, class7B);
        conflictingLesson2.setTimeSlot(slot_Mon_1100);
        conflictingLesson2.setRoom(room101);
        conflictingLesson2.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::qualifiedUnitMatch)
                .given(firstLesson)
                .penalizesBy(0);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::qualifiedUnitMatch)
                .given(conflictingLesson)
                .penalizesBy(1);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::qualifiedUnitMatch)
                .given(conflictingLesson2)
                .penalizesBy(1);
    }

    @Test
    void teacherAvailability() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900)));
        anna.setHomeRoom(room101);

        Lesson firstLesson = new Lesson(101L, math7, class7A);
        firstLesson.setTimeSlot(slot_Mon_0900);
        firstLesson.setRoom(room101);
        firstLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, math7, class7B);
        conflictingLesson.setTimeSlot(slot_Mon_1100);
        conflictingLesson.setRoom(room101);
        conflictingLesson.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherAvailability)
                .given(firstLesson)
                .penalizesBy(0);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherAvailability)
                .given(conflictingLesson)
                .penalizesBy(1);
    }

    @Test
    void dailyLessonCountLimit() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, chem7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Mon_1100, slot_Mon_1200, slot_Mon_1300, slot_Mon_1400)));
        anna.setHomeRoom(room101);

        Lesson nonConflictingChem7 = new Lesson(101L, chem7, class7A);
        nonConflictingChem7.setTimeSlot(slot_Mon_0900);
        nonConflictingChem7.setRoom(chem1);
        nonConflictingChem7.setTeacher(anna);

        Lesson conflictingChem7 = new Lesson(102L, chem7, class7A);
        conflictingChem7.setTimeSlot(slot_Mon_1000);
        conflictingChem7.setRoom(chem1);
        conflictingChem7.setTeacher(anna);

        Lesson nonConflictingMath7 = new Lesson(103L, math7, class7A);
        nonConflictingMath7.setTimeSlot(slot_Mon_1100);
        nonConflictingMath7.setRoom(room101);
        nonConflictingMath7.setTeacher(anna);

        Lesson nonConflictingMath7_2 = new Lesson(104L, math7, class7A);
        nonConflictingMath7_2.setTimeSlot(slot_Mon_1200);
        nonConflictingMath7_2.setRoom(room101);
        nonConflictingMath7_2.setTeacher(anna);

        Lesson conflictingMath7 = new Lesson(105L, math7, class7A);
        conflictingMath7.setTimeSlot(slot_Mon_1300);
        conflictingMath7.setRoom(room101);
        conflictingMath7.setTeacher(anna);

        Lesson conflictingMath7_2 = new Lesson(106L, math7, class7A);
        conflictingMath7_2.setTimeSlot(slot_Mon_1400);
        conflictingMath7_2.setRoom(room101);
        conflictingMath7_2.setTeacher(anna);

        //CHEMISTRY can have up to 1 lesson per day
        constraintVerifier.verifyThat(TimeTableConstraintProvider::dailyLessonCountLimit)
                .given(nonConflictingChem7, conflictingChem7)
                .penalizesBy(1);

        //MATH can have up to 2 lessons per day
        constraintVerifier.verifyThat(TimeTableConstraintProvider::dailyLessonCountLimit)
                .given(nonConflictingMath7, nonConflictingMath7_2, conflictingMath7)
                .penalizesBy(1);

        //MATH max=2, penalty should scale with violation count
        constraintVerifier.verifyThat(TimeTableConstraintProvider::dailyLessonCountLimit)
                .given(nonConflictingMath7, nonConflictingMath7_2, conflictingMath7, conflictingMath7_2)
                .penalizesBy(2);

        // 1 CHEMISTRY and 2 MATH (both at max lesson count per day)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::dailyLessonCountLimit)
                .given(nonConflictingChem7, nonConflictingMath7, nonConflictingMath7_2)
                .penalizesBy(0);
    }

    @Test
    void subjectMustBeConsecutive() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, art7, sport7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Mon_1100, slot_Mon_1200, slot_Mon_1300, slot_Mon_1400, slot_Mon_1500)));
        anna.setHomeRoom(room101);

        Lesson nonConflictingMath7 = new Lesson(101L, math7, class7A);
        nonConflictingMath7.setTimeSlot(slot_Mon_0900);
        nonConflictingMath7.setRoom(room101);
        nonConflictingMath7.setTeacher(anna);

        Lesson nonConflictingMath7_2 = new Lesson(102L, math7, class7A);
        nonConflictingMath7_2.setTimeSlot(slot_Mon_1000);
        nonConflictingMath7_2.setRoom(room101);
        nonConflictingMath7_2.setTeacher(anna);

        Lesson nonConflictingMath7_3 = new Lesson(103L, math7, class7A);
        nonConflictingMath7_3.setTimeSlot(slot_Mon_1100);
        nonConflictingMath7_3.setRoom(room101);
        nonConflictingMath7_3.setTeacher(anna);

        Lesson nonConflictingArt7 = new Lesson(104L, art7, class7A);
        nonConflictingArt7.setTimeSlot(slot_Mon_1200);
        nonConflictingArt7.setRoom(room101);
        nonConflictingArt7.setTeacher(anna);

        Lesson nonConflictingArt7_2 = new Lesson(105L, art7, class7A);
        nonConflictingArt7_2.setTimeSlot(slot_Mon_1300);
        nonConflictingArt7_2.setRoom(room101);
        nonConflictingArt7_2.setTeacher(anna);

        Lesson nonConflictingLesson = new Lesson(105L, math7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_1300);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        Lesson conflictingArt7 = new Lesson(106L, art7, class7A);
        conflictingArt7.setTimeSlot(slot_Mon_1400);
        conflictingArt7.setRoom(room101);
        conflictingArt7.setTeacher(anna);

        Lesson nonConflictingLesson_2 = new Lesson(106L, sport7, class7A);
        nonConflictingLesson_2.setTimeSlot(slot_Mon_1400);
        nonConflictingLesson_2.setRoom(gym1);
        nonConflictingLesson_2.setTeacher(anna);

        Lesson conflictingArt7_2 = new Lesson(107L, art7, class7A);
        conflictingArt7_2.setTimeSlot(slot_Mon_1500);
        conflictingArt7_2.setRoom(room101);
        conflictingArt7_2.setTeacher(anna);

        // MATH can be consecutive
        constraintVerifier.verifyThat(TimeTableConstraintProvider::subjectMustBeConsecutive)
                .given(nonConflictingMath7, nonConflictingMath7_2)
                .penalizesBy(0);

        // MATH can be non-consecutive
        constraintVerifier.verifyThat(TimeTableConstraintProvider::subjectMustBeConsecutive)
                .given(nonConflictingMath7, nonConflictingMath7_3)
                .penalizesBy(0);

        // ART has to be consecutive (they are consecutive)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::subjectMustBeConsecutive)
                .given(nonConflictingArt7, nonConflictingArt7_2)
                .penalizesBy(0);

        // ART has to be consecutive (lesson in between)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::subjectMustBeConsecutive)
                .given(nonConflictingArt7, nonConflictingLesson, conflictingArt7)
                .penalizesBy(1);

        // ART has to be consecutive (two lessons in between)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::subjectMustBeConsecutive)
                .given(nonConflictingArt7, nonConflictingLesson, nonConflictingLesson_2, conflictingArt7_2)
                .penalizesBy(2);

    }

    @Test
    void maxOneTeacherPerSchoolClassPerUnit() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, chem7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Tue_0900)));
        anna.setHomeRoom(room101);

        janis.setId("T2");
        janis.setFirstName("Janis");
        janis.setLastName("Ozols");
        janis.setQualifiedUnits(Set.of(math7, chem7));
        janis.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        janis.setHomeRoom(room102);

        Lesson firstLesson = new Lesson(101L, chem7, class7A);
        firstLesson.setTimeSlot(slot_Mon_0900);
        firstLesson.setRoom(chem1);
        firstLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, chem7, class7A);
        conflictingLesson.setTimeSlot(slot_Tue_0900);
        conflictingLesson.setRoom(chem1);
        conflictingLesson.setTeacher(janis);

        Lesson nonConflictingLesson = new Lesson(103L, chem7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Tue_0900);
        nonConflictingLesson.setRoom(chem1);
        nonConflictingLesson.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::maxOneTeacherPerSchoolClassPerUnit)
                .given(firstLesson, conflictingLesson)
                .penalizesBy(1);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::maxOneTeacherPerSchoolClassPerUnit)
                .given(firstLesson, nonConflictingLesson)
                .penalizesBy(0);
    }

    @Test
    void schoolClassLessonRoomStability() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Mon_1100, slot_Mon_1200)));
        anna.setHomeRoom(room101);

        Lesson firstLesson = new Lesson(101L, math7, class7A);
        firstLesson.setTimeSlot(slot_Mon_0900);
        firstLesson.setRoom(room101);
        firstLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, math7, class7A);
        conflictingLesson.setTimeSlot(slot_Mon_1000);
        conflictingLesson.setRoom(room102);
        conflictingLesson.setTeacher(anna);

        Lesson conflictingLesson_2 = new Lesson(103L, math7, class7A);
        conflictingLesson_2.setTimeSlot(slot_Mon_1100);
        conflictingLesson_2.setRoom(room103);
        conflictingLesson_2.setTeacher(anna);

        Lesson nonConflictingLesson = new Lesson(104L, chem7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_1200);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        // Two different rooms for class7A MATH
        constraintVerifier.verifyThat(TimeTableConstraintProvider::schoolClassLessonRoomStability)
                .given(firstLesson, conflictingLesson)
                .penalizesBy(3);

        // Three different rooms for class7A MATH
        constraintVerifier.verifyThat(TimeTableConstraintProvider::schoolClassLessonRoomStability)
                .given(firstLesson, conflictingLesson, conflictingLesson_2)
                .penalizesBy(6);

        // Same rooms for class7A MATH, different times
        constraintVerifier.verifyThat(TimeTableConstraintProvider::schoolClassLessonRoomStability)
                .given(firstLesson, nonConflictingLesson)
                .penalizesBy(0);
    }

    @Test
    void teacherRoomStability() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, chem7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        anna.setHomeRoom(room101);

        Lesson firstLesson = new Lesson(101L, math7, class7A);
        firstLesson.setTimeSlot(slot_Mon_0900);
        firstLesson.setRoom(room101);
        firstLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, math7, class7B);
        conflictingLesson.setTimeSlot(slot_Mon_1000);
        conflictingLesson.setRoom(room102);
        conflictingLesson.setTeacher(anna);

        Lesson nonConflictingLesson = new Lesson(103L, math7, class7B);
        nonConflictingLesson.setTimeSlot(slot_Mon_1000);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherRoomStability)
                .given(firstLesson, conflictingLesson)
                .penalizesBy(1);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherRoomStability)
                .given(firstLesson, nonConflictingLesson)
                .penalizesBy(0);
    }

    @Test
    void studentGaps() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Mon_1200, slot_Mon_1300, slot_Mon_1400, slot_Mon_1500, slot_Mon_1600)));
        anna.setHomeRoom(room101);

        Lesson nonConflictingLesson = new Lesson(101L, math7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_1100);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        Lesson nonConflictingLesson_2 = new Lesson(102L, math7, class7A);
        nonConflictingLesson_2.setTimeSlot(slot_Mon_1200);
        nonConflictingLesson_2.setRoom(room101);
        nonConflictingLesson_2.setTeacher(anna);

        Lesson nonConflictingLesson_3 = new Lesson(103L, math7, class7A);
        nonConflictingLesson_3.setTimeSlot(slot_Mon_1400);
        nonConflictingLesson_3.setRoom(room101);
        nonConflictingLesson_3.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(104L, math7, class7A);
        conflictingLesson.setTimeSlot(slot_Mon_1500);
        conflictingLesson.setRoom(room101);
        conflictingLesson.setTeacher(anna);

        Lesson conflictingLesson_2 = new Lesson(105L, math7, class7A);
        conflictingLesson_2.setTimeSlot(slot_Mon_1600);
        conflictingLesson_2.setRoom(room101);
        conflictingLesson_2.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGaps)
                .given(lunch1_6, lunch7_12, nonConflictingLesson, nonConflictingLesson_2, nonConflictingLesson_3, conflictingLesson)
                .penalizesBy(0);

        // Missing lesson after lunch
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGaps)
                .given(lunch1_6, lunch7_12, nonConflictingLesson, nonConflictingLesson_2, conflictingLesson)
                .penalizesBy(3);

        // Missing 2 lessons after lunch
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGaps)
                .given(lunch1_6, lunch7_12, nonConflictingLesson, nonConflictingLesson_2, conflictingLesson_2)
                .penalizesBy(6);

        // Missing lesson before and after lunch
        constraintVerifier.verifyThat(TimeTableConstraintProvider::studentGaps)
                .given(lunch1_6, lunch7_12, nonConflictingLesson, conflictingLesson, conflictingLesson_2)
                .penalizesBy(6);
    }

    @Test
    void teacherGaps() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, chem7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000)));
        anna.setHomeRoom(room101);

        Lesson firstLesson = new Lesson(101L, math7, class7A);
        firstLesson.setTimeSlot(slot_Mon_0900);
        firstLesson.setRoom(room101);
        firstLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(103L, math7, class7A);
        conflictingLesson.setTimeSlot(slot_Mon_1100);
        conflictingLesson.setRoom(room101);
        conflictingLesson.setTeacher(anna);

        Lesson conflictingLesson_2 = new Lesson(104L, math7, class7A);
        conflictingLesson_2.setTimeSlot(slot_Mon_1200);
        conflictingLesson_2.setRoom(room101);
        conflictingLesson_2.setTeacher(anna);

        Lesson nonConflictingLesson = new Lesson(102L, math7, class7A);
        nonConflictingLesson.setTimeSlot(slot_Mon_1000);
        nonConflictingLesson.setRoom(room101);
        nonConflictingLesson.setTeacher(anna);

        // 1 lesson gap
        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherGaps)
                .given(firstLesson, conflictingLesson)
                .penalizesBy(1);

        //2 lesson gap
        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherGaps)
                .given(firstLesson, conflictingLesson_2)
                .penalizesBy(2);

        //No gap
        constraintVerifier.verifyThat(TimeTableConstraintProvider::teacherGaps)
                .given(firstLesson, nonConflictingLesson)
                .penalizesBy(0);
    }

    @Test
    void lessLessonsBefore() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Mon_1100)));
        anna.setHomeRoom(room101);

        Lesson firstLesson = new Lesson(101L, math7, class7A);
        firstLesson.setTimeSlot(slot_Mon_0900);
        firstLesson.setRoom(room101);
        firstLesson.setTeacher(anna);

        Lesson secondLesson = new Lesson(102L, math7, class7A);
        secondLesson.setTimeSlot(slot_Mon_1000);
        secondLesson.setRoom(room101);
        secondLesson.setTeacher(anna);

        Lesson thirdLesson = new Lesson(103L, math7, class7A);
        thirdLesson.setTimeSlot(slot_Mon_1100);
        thirdLesson.setRoom(room101);
        thirdLesson.setTeacher(anna);

        //no day start gap
        constraintVerifier.verifyThat(TimeTableConstraintProvider::lessLessonsBefore)
                .given(firstLesson)
                .penalizesBy(0);

        // day starts with second lesson
        constraintVerifier.verifyThat(TimeTableConstraintProvider::lessLessonsBefore)
                .given(secondLesson)
                .penalizesBy(1);

        // day starts with third lesson
        constraintVerifier.verifyThat(TimeTableConstraintProvider::lessLessonsBefore)
                .given(thirdLesson)
                .penalizesBy(4);
    }

    @Test
    void balancedLessonsPerDay() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot_Mon_0900, slot_Mon_1000, slot_Tue_0900, slot_Tue_1000, slot_Wed_0900, slot_Wed_1000, slot_Thu_0900, slot_Thu_1000, slot_Fri_0900, slot_Fri_1000, slot_Fri_1100, slot_Fri_1200)));
        anna.setHomeRoom(room101);

        Lesson lesson_11 = new Lesson(101L, math7, class7A);
        lesson_11.setTimeSlot(slot_Mon_0900);
        lesson_11.setRoom(room101);
        lesson_11.setTeacher(anna);

        Lesson lesson_12 = new Lesson(102L, math7, class7A);
        lesson_12.setTimeSlot(slot_Mon_1000);
        lesson_12.setRoom(room101);
        lesson_12.setTeacher(anna);

        Lesson lesson_21 = new Lesson(103L, math7, class7A);
        lesson_21.setTimeSlot(slot_Tue_0900);
        lesson_21.setRoom(room101);
        lesson_21.setTeacher(anna);

        Lesson lesson_22 = new Lesson(104L, math7, class7A);
        lesson_22.setTimeSlot(slot_Tue_1000);
        lesson_22.setRoom(room101);
        lesson_22.setTeacher(anna);

        Lesson lesson_31 = new Lesson(105L, math7, class7A);
        lesson_31.setTimeSlot(slot_Wed_0900);
        lesson_31.setRoom(room101);
        lesson_31.setTeacher(anna);

        Lesson lesson_32 = new Lesson(106L, math7, class7A);
        lesson_32.setTimeSlot(slot_Wed_1000);
        lesson_32.setRoom(room101);
        lesson_32.setTeacher(anna);

        Lesson lesson_41 = new Lesson(107L, math7, class7A);
        lesson_41.setTimeSlot(slot_Thu_0900);
        lesson_41.setRoom(room101);
        lesson_41.setTeacher(anna);

        Lesson lesson_42 = new Lesson(108L, math7, class7A);
        lesson_42.setTimeSlot(slot_Thu_1000);
        lesson_42.setRoom(room101);
        lesson_42.setTeacher(anna);

        Lesson lesson_51 = new Lesson(109L, math7, class7A);
        lesson_51.setTimeSlot(slot_Fri_0900);
        lesson_51.setRoom(room101);
        lesson_51.setTeacher(anna);

        Lesson lesson_52 = new Lesson(110L, math7, class7A);
        lesson_52.setTimeSlot(slot_Fri_1000);
        lesson_52.setRoom(room101);
        lesson_52.setTeacher(anna);

        Lesson lesson_53 = new Lesson(111L, math7, class7A);
        lesson_53.setTimeSlot(slot_Fri_1100);
        lesson_53.setRoom(room101);
        lesson_53.setTeacher(anna);

        Lesson lesson_54 = new Lesson(111L, math7, class7A);
        lesson_54.setTimeSlot(slot_Fri_1200);
        lesson_54.setRoom(room101);
        lesson_54.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::balancedLessonsPerDay)
                .given(lesson_11, lesson_12, lesson_21, lesson_22, lesson_31, lesson_32, lesson_41, lesson_42, lesson_51, lesson_52)
                .penalizesBy(0);

        // AVG = 2, 4 point penalty for each mistake (1 less on tuesday, 1 more on friday)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::balancedLessonsPerDay)
                .given(lesson_11, lesson_12, lesson_21, lesson_31, lesson_32, lesson_41, lesson_42, lesson_51, lesson_52, lesson_53)
                .penalizesBy(8);

        // AVG = 2, 16 point penalty for each mistake (2 less on tuesday, 2 more on friday)
        constraintVerifier.verifyThat(TimeTableConstraintProvider::balancedLessonsPerDay)
                .given(lesson_11, lesson_12, lesson_31, lesson_32, lesson_41, lesson_42, lesson_51, lesson_52, lesson_53, lesson_54)
                .penalizesBy(32);

    }
}