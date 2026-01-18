package com.schoolplanner.timetable.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.*;
import com.schoolplanner.timetable.domain.*;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.count;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.countBi;

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
                dailyLessonCountLimit(constraintFactory),
                maxOneTeacherPerSchoolClassPerUnit(constraintFactory),
                subjectMustBeConsecutive(constraintFactory),
                studentLunchBreak(constraintFactory),

                // Soft Constraints
                schoolClassLessonRoomStability(constraintFactory),
                teacherRoomStability(constraintFactory),
                studentGaps(constraintFactory),
                teacherGaps(constraintFactory),
                lessLessonsBefore(constraintFactory),
                balancedLessonsPerDay(constraintFactory)
        };
    }

    // Skolotājs nevar vienlaicīgi pasniegt divas stundas
    Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getTeacher),
                        Joiners.equal(Lesson::getTimeSlot)
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher cannot teach two lessons at the same time");
    }

    // Telpā nevar notikt divas stundas vienlaicīgi
    Constraint roomConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getRoom),
                        Joiners.equal(Lesson::getTimeSlot)
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("A room cannot host two lessons at the same time.");
    }

    //Pusdienu laikā nedrīskt notikt stundas
    Constraint studentLunchBreak(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .join(LunchGroup.class,
                        Joiners.filtering((lesson, lunchGroup) ->
                                lesson.getSchoolClass().getGrade() >= lunchGroup.getMinGrade() &&
                                        lesson.getSchoolClass().getGrade() <= lunchGroup.getMaxGrade()))
                .filter((lesson, lunchGroup) ->
                        lunchGroup.getLunchTimeSlots().contains(lesson.getTimeSlot()))
                .penalize(HardSoftScore.ofHard(100))
                .asConstraint("No lessons during lunch break");
    }

    // Stundentu grupa nevar apmeklēt divas stundas vienlaicīgi
    Constraint studentGroupConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(
                        Lesson.class,
                        Joiners.equal(Lesson::getSchoolClass),
                        Joiners.equal(Lesson::getTimeSlot)
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("A student group cannot attend two lessons at the same time");
    }

    // Telpai jāatbilst nepieciešamajam telpas tipam
    Constraint roomTypeMatch(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null)
                .filter(lesson -> lesson.getTeachingUnit().getRoomType() != lesson.getRoom().getRoomType())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("The assigned room must match the required RoomType");
    }

    // Skolotājs drīkst pasniegt noteikto stundu
    Constraint qualifiedUnitMatch(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTeachingUnit() != null)
                .filter(lesson -> lesson.getTeacher() != null)
                .filter(lesson -> !lesson.getTeacher().getQualifiedUnits().contains(lesson.getTeachingUnit()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher must be qualified to teach the subject");
    }

    // Skolotājam jābūt pieejamam
    Constraint teacherAvailability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .filter(lesson -> lesson.getTeacher() != null)
                .filter(lesson -> !lesson.getTeacher().getWorkTimeSlots().contains(lesson.getTimeSlot()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher must be available at the assigned time slot");
    }

    // Priekšmeta stundu skaits nedrīkst pārsniegt dienas limitu
    Constraint dailyLessonCountLimit(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .groupBy(
                        Lesson::getSchoolClass,
                        lesson -> lesson.getTeachingUnit().getSubject(),
                        lesson -> lesson.getTimeSlot().getSchoolDay(),
                        ConstraintCollectors.count()
                )
                .filter((schoolClass, subject, day, count) -> {
                    int limit = subject.isAllowMultiplePerDay() ? 2 : 1;
                    return count > limit;
                })
                .penalize(HardSoftScore.ONE_HARD,
                        (schoolClass, subject, day, count) -> {
                            int limit = subject.isAllowMultiplePerDay() ? 2 : 1;
                            return count - limit;
                        })
                .asConstraint("Daily lesson count limit for lesson exceeded");
    }

    // Noteiktiem priekšmetiem jānotiek 2 stundām pēc kārtas
    Constraint subjectMustBeConsecutive(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .filter(lesson -> lesson.getTeachingUnit().getSubject().isMustBeConsecutive())
                .groupBy(Lesson::getSchoolClass,
                        lesson -> lesson.getTeachingUnit().getSubject(),
                        // CHANGE: Collect the actual TimeSlot objects, not just IDs
                        ConstraintCollectors.toList(Lesson::getTimeSlot))
                .penalize(HardSoftScore.ONE_HARD,
                        (schoolClass, subject, slots) -> {
                            return calculateNonConsecutivePenalty((List<TimeSlot>) slots);
                        })
                .asConstraint("Subject must be consecutive");
    }

    // Vienu priekšmetu vienmēr pasniedz viens un tas pats skolotājs
    Constraint maxOneTeacherPerSchoolClassPerUnit(ConstraintFactory constraintFactory) {
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

    // Priekšmetu vajadzētu pasniegt tajā pašā telpā
    Constraint schoolClassLessonRoomStability(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getRoom() != null)
                .groupBy(
                        Lesson::getSchoolClass,
                        Lesson::getTeachingUnit,
                        ConstraintCollectors.countDistinct(Lesson::getRoom)
                )
                .filter((schoolClass, teachingUnit, distinctRoomCount) -> distinctRoomCount > 1)
                .penalize(HardSoftScore.ONE_SOFT, (schoolClass, teachingUnit, distinctRoomCount) -> (distinctRoomCount - 1) * 3)
                .asConstraint("One room for teachingUnit preference");
    }

    // Skolotājam vēlas pasniegt stundu savā klasē
    Constraint teacherRoomStability(ConstraintFactory constraintFactory) {
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
                .join(LunchGroup.class,
                        Joiners.filtering((lesson, lunchGroup) ->
                                lesson.getSchoolClass().getGrade() >= lunchGroup.getMinGrade() &&
                                        lesson.getSchoolClass().getGrade() <= lunchGroup.getMaxGrade()))
                .groupBy(
                        (lesson, lunchGroup) -> lesson.getSchoolClass(),
                        (lesson, lunchGroup) -> lesson.getTimeSlot().getSchoolDay(),
                        (lesson, lunchGroup) -> lunchGroup,
                        ConstraintCollectors.toList((lesson, lunchGroup) -> lesson.getTimeSlot().getId())
                )
                .penalize(HardSoftScore.ONE_SOFT,
                        (schoolClass, day, lunchGroup, slotIds) -> {
                            return calculateGapsIgnoringLunch(slotIds, lunchGroup) * 3;
                        })
                .asConstraint("Student gaps per day (ignoring lunch)");
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

    // Skolēni vēlas pēc iespējas īsāku dienu
    Constraint lessLessonsBefore(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .groupBy(Lesson::getSchoolClass,
                        lesson -> lesson.getTimeSlot().getSchoolDay(),
                        ConstraintCollectors.toList(lesson -> lesson.getTimeSlot().getId()))
                .penalize(HardSoftScore.ONE_SOFT,
                        (schoolClass, day, slotIds) -> {
                            int startPenalty = dayStartPenalty(slotIds);
                            return startPenalty * startPenalty;
                        })
                .asConstraint("Evenly spread lessons per day");
    }

    //Skolēni vēlas apmēram vienādu stundu skaitu dienā
    Constraint balancedLessonsPerDay(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Lesson.class)
                .filter(lesson -> lesson.getTimeSlot() != null)
                .groupBy(Lesson::getSchoolClass,
                        ConstraintCollectors.toList())
                .penalize(HardSoftScore.ONE_SOFT,
                        (schoolClass, lessons) -> {
                            return calculateWeeklyVariancePenalty(lessons);
                        })
                .asConstraint("Balanced lessons per day");
    }

    // helper functions
    private int calculateGaps(List<Long> slotIds) {
        if (slotIds.isEmpty()) return 0;
        long firstSlot = Collections.min(slotIds);
        long lastSlot = Collections.max(slotIds);
        long span = lastSlot - firstSlot + 1;
        long distinctSlotCount = slotIds.stream().distinct().count();
        return Math.max(0, (int) (span - distinctSlotCount));
    }

    private int calculateNonConsecutivePenalty(List<TimeSlot> slots) {
        if (slots.size() < 2) return 0;

        Map<SchoolDay, List<TimeSlot>> slotsByDay = slots.stream()
                .collect(Collectors.groupingBy(TimeSlot::getSchoolDay));

        int penalty = 0;

        if (slotsByDay.size() > 1) {
            penalty += 10 * (slotsByDay.size() - 1);
        }

        for (List<TimeSlot> daySlots : slotsByDay.values()) {

            daySlots.sort(Comparator.comparingLong(TimeSlot::getId));

            for (int i = 0; i < daySlots.size() - 1; i++) {
                long currentId = daySlots.get(i).getId();
                long nextId = daySlots.get(i + 1).getId();

                long diff = nextId - currentId;

                if (diff > 1) {
                    penalty += (int) (diff - 1);
                }
            }
        }
        return penalty;
    }

    private int dayStartPenalty(List<Long> slotIds) {
        if (slotIds.isEmpty()) return 0;
        Long minAbsoluteId = Collections.min(slotIds);
        long normalizedId = ((minAbsoluteId - 1) % 10);
        if (normalizedId > 0) {
            return (int) normalizedId;
        } else {
            return 0;
        }
    }

    private int calculateWeeklyVariancePenalty(List<Lesson> lessons) {
        double totalLessons = lessons.size();

        double averagePerDay = totalLessons / 5.0;

        Map<SchoolDay, Long> countPerDayMap = lessons.stream()
                .collect(Collectors.groupingBy(
                        lesson -> lesson.getTimeSlot().getSchoolDay(),
                        Collectors.counting()
                ));

        double totalPenalty = 0;

        for (SchoolDay day : SchoolDay.values()) {

            long actualCount = countPerDayMap.getOrDefault(day, 0L);

            double diff = Math.abs(averagePerDay - actualCount);
            double term = diff * 2;
            totalPenalty += (term * term);
        }

        return (int) totalPenalty;
    }

    private int calculateGapsIgnoringLunch(List<Long> occupiedSlotIds, LunchGroup lunchGroup) {
        if (occupiedSlotIds.isEmpty() || occupiedSlotIds.size() < 2) return 0;

        occupiedSlotIds.sort(Long::compareTo);
        long firstSlotId = occupiedSlotIds.get(0);
        long lastSlotId = occupiedSlotIds.get(occupiedSlotIds.size() - 1);

        Set<Long> lunchSlotIds = lunchGroup.getLunchTimeSlots().stream()
                .map(TimeSlot::getId)
                .collect(Collectors.toSet());

        int penalty = 0;

        for (long slotId = firstSlotId + 1; slotId < lastSlotId; slotId++) {
            if (!occupiedSlotIds.contains(slotId)) {
                if (!lunchSlotIds.contains(slotId)) {
                    penalty++;
                }
            }
        }

        return penalty;
    }
}