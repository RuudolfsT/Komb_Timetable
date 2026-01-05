package com.schoolplanner.timetable.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import com.schoolplanner.timetable.domain.Lesson;

import static ai.timefold.solver.core.api.score.stream.Joiners.*;
import static ai.timefold.solver.core.api.score.stream.ConstraintCollectors.*;


public class TimeTableConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
            teacherConflict(constraintFactory),
        };
    }

    // Skolotājs nevar vienlaicīgi pasniegt divas stundas
    private Constraint teacherConflict(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachUniquePair(
                        Lesson.class,
                        equal(Lesson::getTeacher),
                        equal(Lesson::getTimeSlot)
                )
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Teacher cannot teach two lessons at the same time");
    }
}
