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
    TimeSlot slot1_Mon_0900 = new TimeSlot(1L, SchoolDay.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 45));
    TimeSlot slot2_Mon_1000 = new TimeSlot(2L, SchoolDay.MONDAY, LocalTime.of(10, 0), LocalTime.of(10, 45));
    TeachingUnit math7 = new TeachingUnit(1L, Subject.MATH, 7, RoomType.NORMAL);
    TeachingUnit chem8 = new TeachingUnit(2L, Subject.CHEMISTRY, 7, RoomType.NORMAL);
    TeachingUnit sport7 = new TeachingUnit(3L, Subject.SPORT, 7, RoomType.GYM);
    SchoolClass class7A = new SchoolClass(1L, "7A", 7);
    SchoolClass class7B = new SchoolClass(2L, "7B", 7);
    SchoolClass class7C = new SchoolClass(3L, "7C", 7);
    Teacher anna = new Teacher();



    ConstraintVerifier<TimeTableConstraintProvider, TimeTable> constraintVerifier = ConstraintVerifier.build(
            new TimeTableConstraintProvider(),
            TimeTable.class,
            Lesson.class
    );

    @Test
    void roomConflict() {
        anna.setId("T1");
        anna.setFirstName("Anna");
        anna.setLastName("Ozola");
        anna.setQualifiedUnits(Set.of(math7, chem8));
        anna.setWorkTimeSlots(new HashSet<>(List.of(slot1_Mon_0900, slot2_Mon_1000)));
        anna.setHomeRoom(room101);

        Lesson firstLesson = new Lesson(101L, sport7, class7A);
        firstLesson.setTimeSlot(slot1_Mon_0900);
        firstLesson.setRoom(room101);
        firstLesson.setTeacher(anna);

        Lesson conflictingLesson = new Lesson(102L, chem8, class7B);
        conflictingLesson.setTimeSlot(slot1_Mon_0900);
        conflictingLesson.setRoom(room101);
        conflictingLesson.setTeacher(anna);

        Lesson nonConflictingLesson = new Lesson(103L, math7, class7C);
        nonConflictingLesson.setTimeSlot(slot2_Mon_1000);
        nonConflictingLesson.setRoom(room102);
        nonConflictingLesson.setTeacher(anna);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomConflict)
                .given(firstLesson, conflictingLesson, nonConflictingLesson)
                .penalizesBy(1);

        constraintVerifier.verifyThat(TimeTableConstraintProvider::roomConflict)
                .given(firstLesson, nonConflictingLesson)
                .penalizesBy(0);
    }
}