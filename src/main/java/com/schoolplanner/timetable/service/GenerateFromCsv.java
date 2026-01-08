package com.schoolplanner.timetable.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.schoolplanner.timetable.domain.*;

import java.io.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class GenerateFromCsv {

    public static TimeTable generateFromCsv(String resourcePath) {
        List<TimeSlot> timeSlots = generateTimeSlots();
        List<Room> rooms = generateRooms();
        List<LunchGroup> lunchGroups = generateLunchGroups(timeSlots);
        List<SchoolClass> schoolClasses = new ArrayList<>();
        List<Lesson> lessons = new ArrayList<>();

        List<TeachingUnit> allTeachingUnits = new ArrayList<>();

        long lessonIdCounter = 0;
        long classIdCounter = 0;
        InputStream is = GenerateFromCsv.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IllegalArgumentException(
                    "CSV not found on classpath: " + resourcePath
            );
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {

            String headerLine = br.readLine();
            if (headerLine == null) return null;

            String[] headers = headerLine.split(",");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split(",");

                String gradeStr = columns[0].trim();
                if (gradeStr.isEmpty() || gradeStr.equalsIgnoreCase("nan")) continue;

                int grade = (int) Double.parseDouble(gradeStr);

//                LocalTime lunchStart = (grade <= 6) ? LocalTime.of(11, 0) : LocalTime.of(12, 0);
//                LocalTime lunchEnd = lunchStart.plusMinutes(40);
//                TimeRange lunchTime = new TimeRange(lunchStart, lunchEnd);


                SchoolClass schoolClassA = new SchoolClass(
                        ++classIdCounter,
                        grade + "A",
                        grade
                );
//                SchoolClass schoolClassB = new SchoolClass(
//                        ++classIdCounter,
//                        grade + "B",
//                        grade,
//                        lunchTime
//                );

                schoolClasses.add(schoolClassA);
//                schoolClasses.add(schoolClassB);

                for (int i = 1; i < columns.length; i++) {
                    if (i >= headers.length) break;

                    String subjectName = headers[i].trim();
                    String countStr = columns[i].trim();

                    if (countStr.isEmpty() || countStr.equalsIgnoreCase("nan")) continue;

                    int count = (int) Double.parseDouble(countStr);
                    if (count == 0) continue;

                    Subject subject;
                    try {
                        subject = Subject.valueOf(subjectName.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        System.out.println("Warning: Subject '" + subjectName + "' not found in Enum. Skipping.");
                        continue;
                    }

                    RoomType requiredRoom = getRoomTypeForSubject(subject);

                    TeachingUnit unit = new TeachingUnit(subject, grade, requiredRoom);

                    if (!allTeachingUnits.contains(unit)) {
                        allTeachingUnits.add(unit);
                    }

                    for (int k = 0; k < count; k++) {
                        lessons.add(new Lesson(++lessonIdCounter, unit, schoolClassA));
//                        lessons.add(new Lesson(++lessonIdCounter, unit, schoolClassB));
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read CSV file", e);
        }

        List<Teacher> teachers = generateGenericTeachers(timeSlots, rooms, allTeachingUnits);

        return new TimeTable(
                timeSlots,
                rooms,
                teachers,
                lunchGroups,
                schoolClasses,
                lessons,
                HardSoftScore.ZERO
        );
    }

    private static RoomType getRoomTypeForSubject(Subject s) {
        return switch (s) {
            case SPORT -> RoomType.GYM;
            case CHEMISTRY, BIOLOGY -> RoomType.CHEMISTRY_LAB;
            case PHYSICS -> RoomType.PHYSICS_LAB;
            case MUSIC -> RoomType.MUSIC;
            case COMPUTER -> RoomType.COMPUTER_LAB;
            default -> RoomType.NORMAL;
        };
    }

    private static List<TimeSlot> generateTimeSlots() {
        List<TimeSlot> slots = new ArrayList<>();
        long id = 0;
        SchoolDay[] days = SchoolDay.values();
        LocalTime[] startTimes = {
                LocalTime.of(8, 30),
                LocalTime.of(9, 20),
                LocalTime.of(10, 10),
                LocalTime.of(11, 0),
                LocalTime.of(11, 50),
                LocalTime.of(12, 40),
                LocalTime.of(13, 30),
                LocalTime.of(14, 20),
                LocalTime.of(15, 10),
                LocalTime.of(16, 0),
        };

        for (SchoolDay day : days) {
            for (LocalTime start : startTimes) {
                slots.add(new TimeSlot(++id, day, start, start.plusMinutes(40)));
            }
        }
        return slots;
    }

    private static List<LunchGroup> generateLunchGroups(List<TimeSlot> timeSlots) {
        List<TimeSlot> lunchSlots1to6 = new ArrayList<>();
        List<TimeSlot> lunchSlots7to12 = new ArrayList<>();

        for (TimeSlot slot : timeSlots) {
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

        return List.of(
                new LunchGroup("Grades 1–6 lunch", 1, 6, lunchSlots1to6),
                new LunchGroup("Grades 7–12 lunch", 7, 12, lunchSlots7to12)
        );
    }

    private static List<Room> generateRooms() {
        return List.of(
                new Room("101", RoomType.NORMAL, 30),
                new Room("102", RoomType.NORMAL, 30),
                new Room("103", RoomType.NORMAL, 30),
                new Room("104", RoomType.NORMAL, 30),
                new Room("105", RoomType.NORMAL, 30),
                new Room("106", RoomType.NORMAL, 30),
                new Room("107", RoomType.NORMAL, 30),
                new Room("108", RoomType.NORMAL, 30),
                new Room("109", RoomType.NORMAL, 30),
                new Room("110", RoomType.NORMAL, 30),
                new Room("111", RoomType.NORMAL, 30),
                new Room("112", RoomType.NORMAL, 30),
                new Room("113", RoomType.NORMAL, 30),
                new Room("114", RoomType.NORMAL, 30),
                new Room("115", RoomType.NORMAL, 30),
                new Room("116", RoomType.NORMAL, 30),
                new Room("117", RoomType.NORMAL, 30),
                new Room("118", RoomType.NORMAL, 30),
                new Room("119", RoomType.NORMAL, 30),
                new Room("120", RoomType.NORMAL, 30),
                new Room("121", RoomType.NORMAL, 30),
                new Room("SCI_1", RoomType.CHEMISTRY_LAB, 30),
                new Room("SCI_2", RoomType.CHEMISTRY_LAB, 30),
                new Room("SCI_3", RoomType.PHYSICS_LAB, 30),
                new Room("SCI_4", RoomType.PHYSICS_LAB, 30),
                new Room("GYM_1", RoomType.GYM, 100),
                new Room("GYM_2", RoomType.GYM, 100),
                new Room("COMP_1", RoomType.COMPUTER_LAB, 30),
                new Room("COMP_2", RoomType.COMPUTER_LAB, 30),
                new Room("MUS_1", RoomType.MUSIC, 30),
                new Room("MUS_2", RoomType.MUSIC, 30)
        );
    }

    private static List<Teacher> generateGenericTeachers(List<TimeSlot> allSlots, List<Room> allRooms, List<TeachingUnit> allUnits) {
        List<Teacher> teachers = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            Teacher t = new Teacher();
            t.setId("T" + i);
            t.setFirstName("Teacher");
            t.setLastName(String.valueOf(i));
            t.setWorkTimeSlots(new HashSet<>(allSlots));
            t.setQualifiedUnits(new HashSet<>(allUnits));
            t.setHomeRoom(allRooms.get(0));

            teachers.add(t);
        }
        return teachers;
    }
}