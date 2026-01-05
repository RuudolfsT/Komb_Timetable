package com.schoolplanner.timetable.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    @PlanningId
    private String id;
    private RoomType roomType;
    private int capacity;

    @Override
    public String toString() {
        return id;
    }
}