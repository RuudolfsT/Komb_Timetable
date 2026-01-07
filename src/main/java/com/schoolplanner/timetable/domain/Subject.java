package com.schoolplanner.timetable.domain;

import lombok.*;

@Getter
public enum Subject {
    // Enum(allowMultiplePerDay, mustBeConsecutive)
    MATH(true, false),
    LATVIAN(true, false),
    LITERATURE(false, false),
    FOREIGN_LANG_1(true, false),
    FOREIGN_LANG_2(true, false),
    SPORT(false, false),
    NATURAL_SCIENCES(false, false),
    BIOLOGY(false, false),
    PHYSICS(false, false),
    CHEMISTRY(false, false),
    GEOGRAPHY(false, false),
    MUSIC(false, false),
    ART(true, true),
    HISTORY(false, false),
    SOCIAL_SCIENCES(false, false),
    COMPUTER(true, false),
    ENGINEERING(false, false),
    DESIGN_AND_TECHNOLOGY(true, true);

    private final boolean allowMultiplePerDay;
    private final boolean mustBeConsecutive;

    Subject(boolean allowMultiplePerDay, boolean mustBeConsecutive) {
        this.allowMultiplePerDay = allowMultiplePerDay;
        this.mustBeConsecutive = mustBeConsecutive;
    }
}