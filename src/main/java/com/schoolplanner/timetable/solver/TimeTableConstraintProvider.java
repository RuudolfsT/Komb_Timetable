package com.schoolplanner.timetable.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import com.schoolplanner.timetable.domain.Lesson;
import com.schoolplanner.timetable.domain.SchoolClass;

import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // Hard Constraints
                teacherConflict(constraintFactory),
                roomConflict(constraintFactory),
                studentGroupConflict(constraintFactory),
                roomTypeMatch(constraintFactory),
                qualifiedUnitMatch(constraintFactory),
                teacherAvailability(constraintFactory),
                schoolGroupLunchConflict(constraintFactory),
                maxTwoLessonsPerDay(constraintFactory),
                maxOneTeacherPerSchoolClassPerUnit(constraintFactory),

                // Soft Constraints
                teacherRoomStability(constraintFactory)
        };
    }

    // Skolotājs nevar vienlaicīgi pasniegt divas stundas
    private Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
                        Joiners.equal(Lesson::getTimeSlot)
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher cannot teach two lessons at the same time");
    }

    // Telpā nevar notikt divas stundas vienlaicīgi
    private Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getRoom),
                        Joiners.equal(Lesson::getTimeSlot)
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("A room cannot host two lessons at the same time.");
    }

    // Stundentu grupa nevar apmeklēt divas stundas vienlaicīgi
    private Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getSchoolClass),
                        Joiners.equal(Lesson::getTimeSlot)
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("A student group cannot attend two lessons at the same time");
    }

    // Telpai jāatbilst nepieciešamajam telpas tipam
    private Constraint roomTypeMatch(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null)
                .filter(lesson -> lesson.getTeachingUnit().getRoomType() != lesson.getRoom().getRoomType())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("The assigned room must match the required RoomType");
    }

    // Skolotājs drīkst pasniegt noteikto stundu
    private Constraint qualifiedUnitMatch(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeachingUnit() != null)
                .filter(lesson -> lesson.getTeacher() != null)
                .filter(lesson -> !lesson.getTeacher().getQualifiedUnits().contains(lesson.getTeachingUnit()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher must be qualified to teach the subject");
    }

    // Skolotājam jābūt pieejamam
    private Constraint teacherAvailability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .filter(lesson -> lesson.getTeacher() != null)
                .filter(lesson -> !lesson.getTeacher().getWorkTimeSlots().contains(lesson.getTimeSlot()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher must be available at the assigned time slot");
    }

    // Katrai klasei ir savs pusdienu laiks, kurā nedrīkst būt stundas
    private Constraint schoolGroupLunchConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .join(SchoolClass.class, Joiners.equal(Lesson::getSchoolClass, Function.identity()))
                .filter((lesson, schoolClass) -> {
                    LocalTime lessonStart = lesson.getTimeSlot().getStartTime();
                    LocalTime lunchStart = schoolClass.getLunchTime().start();
                    return lessonStart.equals(lunchStart);
                })
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Student group cannot have lesson during their lunch");
    }
    // Ne vairāk kā 2 viena veida stundas dienā
    private Constraint maxTwoLessonsPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .groupBy(
                        Lesson::getSchoolClass,
                        Lesson::getTeachingUnit,
                        lesson -> lesson.getTimeSlot().getSchoolDay(),
                        ConstraintCollectors.count()
                )
                .filter((schoolClass, teachingUnit, day, count) -> count > 2)
                .penalize(HardSoftScore.ONE_HARD, (schoolClass, teachingUnit, day, count) -> count - 2)
                .asConstraint("Max 2 lessons of same unit per day");
    }

    // Vienu priekšmetu vienmēr pasniedz viens un tas pats skolotājs
    private Constraint maxOneTeacherPerSchoolClassPerUnit(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeacher() != null)
                .groupBy(
                        Lesson::getSchoolClass,
                        Lesson::getTeachingUnit,
                        ConstraintCollectors.countDistinct(Lesson::getTeacher)
                )
                .filter((schoolClass, teachingUnit, distinctTeacherCount) -> distinctTeacherCount > 1)
                .penalize(HardSoftScore.ONE_HARD, (schoolClass, teachingUnit, distinctTeacherCount) -> distinctTeacherCount - 1)
                .asConstraint("One teacher per unit per class");
    }

    // Skolotājam vēlas pasniegt stundu savā klasē
    private Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null)
                .filter(lesson -> lesson.getTeacher().getHomeRoom() != null)
                .filter(lesson -> !lesson.getRoom().getId().equals(lesson.getTeacher().getHomeRoom().getId()))
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Teachers prefer to teach in their Home Room");
    }

    // Skolēni nevēlas brīvas starpstundas
    Constraint studentGaps(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .groupBy(Lesson::getSchoolClass,
                        lesson -> lesson.getTimeSlot().getSchoolDay(),
                        ConstraintCollectors.toList(lesson -> lesson.getTimeSlot().getId()))
                .penalize(HardSoftScore.ONE_SOFT,
                        (schoolClass, day, slotIds) -> {
                            return calculateGaps(slotIds);
                        })
                .asConstraint("Student gaps per day");
    }

    // Skolotāji nevēlas brīvas starpstundas
    Constraint teacherGaps(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .groupBy(Lesson::getTeacher,
                        lesson -> lesson.getTimeSlot().getSchoolDay(),
                        ConstraintCollectors.toList(lesson -> lesson.getTimeSlot().getId()))
                .penalize(HardSoftScore.ONE_SOFT,
                        (teacher, day, slotIds) -> {
                            return calculateGaps(slotIds);
                        })
                .asConstraint("Teacher gaps per day");
    }

    //---------
    // helper functions
    //---------
    private int calculateGaps(List<Long> slotIds) {
        if (slotIds.isEmpty()) return 0;
        long firstSlot = Collections.min(slotIds);
        long lastSlot = Collections.max(slotIds);
        long span = lastSlot - firstSlot + 1;
        return (int) (span - slotIds.size());
    }
}