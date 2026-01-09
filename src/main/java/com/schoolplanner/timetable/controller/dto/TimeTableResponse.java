package com.schoolplanner.timetable.controller.dto;

import com.schoolplanner.timetable.domain.Lesson;
import com.schoolplanner.timetable.domain.LunchGroup;
import com.schoolplanner.timetable.domain.TimeTable;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import ai.timefold.solver.core.api.score.ScoreExplanation;
import lombok.Getter;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class TimeTableResponse {

    private HardSoftScore score;
    private List<Lesson> lessons;
    private List<LunchGroup> lunchGroups;
    private Map<String, String> constraintMatches;

    public TimeTableResponse(TimeTable solution, ScoreExplanation<TimeTable, HardSoftScore> explanation) {
        this.score = solution.getScore();
        this.lessons = solution.getLessons();
        this.lunchGroups = solution.getLunchGroups();
        this.constraintMatches = explanation.getConstraintMatchTotalMap()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getScore().toString()));
    }

}
