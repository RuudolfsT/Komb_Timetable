package com.schoolplanner.timetable.domain;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

// izmanto tikai priekš skolotāja darba laika ievades
public record TimeRange(LocalTime start, LocalTime end) {}
