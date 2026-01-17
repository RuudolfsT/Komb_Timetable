package com.schoolplanner.timetable.service;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import com.schoolplanner.timetable.domain.*;

import java.io.*;
import java.time.LocalTime;
import java.util.*;

/**
 * Service for generating a TimeTable problem from multiple CSV files.
 * CSV files include: rooms.csv, teachers.csv, lunch_groups.csv, lesson_list.csv
 */
public class CsvDataLoader {

    private static final String DEFAULT_ROOMS_PATH = "data/rooms.csv";
    private static final String DEFAULT_TEACHERS_PATH = "data/teachers.csv";
    private static final String DEFAULT_LUNCH_GROUPS_PATH = "data/lunch_groups.csv";
    private static final String DEFAULT_LESSONS_PATH = "data/lesson_list.csv";

    /**
     * Generates a TimeTable from all CSV files using default paths.
     */
    public static TimeTable generateFromAllCsvFiles() {
        return generateFromAllCsvFiles(
                DEFAULT_ROOMS_PATH,
                DEFAULT_TEACHERS_PATH,
                DEFAULT_LUNCH_GROUPS_PATH,
                DEFAULT_LESSONS_PATH,
                1
        );
    }

    /**
     * Generates a TimeTable from all CSV files with custom paths.
     */
    public static TimeTable generateFromAllCsvFiles(
            String roomsCsvPath,
            String teachersCsvPath,
            String lunchGroupsCsvPath,
            String lessonsCsvPath,
            int classCount
    ) {
        // Generate time slots first (needed for teachers and lunch groups)
        List<TimeSlot> timeSlots = generateTimeSlots();

        // Load rooms from CSV
        List<Room> rooms = loadRoomsFromCsv(roomsCsvPath);
        Map<String, Room> roomMap = new HashMap<>();
        for (Room room : rooms) {
            roomMap.put(room.getId(), room);
        }

        // Parse lessons CSV to create teaching units and lessons
        LessonParseResult lessonResult = parseLessonsCsv(lessonsCsvPath, classCount);
        List<TeachingUnit> allTeachingUnits = lessonResult.teachingUnits;
        List<SchoolClass> schoolClasses = lessonResult.schoolClasses;
        List<Lesson> lessons = lessonResult.lessons;

        // Load teachers from CSV (needs rooms and teaching units)
        List<Teacher> teachers = loadTeachersFromCsv(
                teachersCsvPath,
                roomMap,
                timeSlots,
                allTeachingUnits
        );

        // Load lunch groups from CSV (needs time slots)
        List<LunchGroup> lunchGroups = loadLunchGroupsFromCsv(lunchGroupsCsvPath, timeSlots);

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

    /**
     * Loads rooms from CSV file.
     * Format: id,roomType
     */
    public static List<Room> loadRoomsFromCsv(String resourcePath) {
        List<Room> rooms = new ArrayList<>();

        try (BufferedReader br = getBufferedReader(resourcePath)) {
            String headerLine = br.readLine();
            if (headerLine == null) return rooms;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split(",");
                if (columns.length < 2) continue;

                String id = columns[0].trim();
                String roomTypeStr = columns[1].trim();

                RoomType roomType;
                try {
                    roomType = RoomType.valueOf(roomTypeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    System.out.println("Warning: RoomType '" + roomTypeStr + "' not found. Defaulting to NORMAL.");
                    roomType = RoomType.NORMAL;
                }

                rooms.add(new Room(id, roomType));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read rooms CSV: " + resourcePath, e);
        }

        return rooms;
    }

    /**
     * Loads teachers from CSV file.
     * Format: id,firstName,lastName,homeRoomId,qualifiedUnits,workDays,workStartTime,workEndTime
     * qualifiedUnits format: SUBJECT:minGrade-maxGrade;SUBJECT:minGrade-maxGrade
     * workDays format: MONDAY;TUESDAY;WEDNESDAY
     */
    public static List<Teacher> loadTeachersFromCsv(
            String resourcePath,
            Map<String, Room> roomMap,
            List<TimeSlot> allTimeSlots,
            List<TeachingUnit> allTeachingUnits
    ) {
        List<Teacher> teachers = new ArrayList<>();

        try (BufferedReader br = getBufferedReader(resourcePath)) {
            String headerLine = br.readLine();
            if (headerLine == null) return teachers;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split(",");
                if (columns.length < 8) {
                    System.out.println("Warning: Skipping malformed teacher line: " + line);
                    continue;
                }

                String id = columns[0].trim();
                String firstName = columns[1].trim();
                String lastName = columns[2].trim();
                String homeRoomId = columns[3].trim();
                String qualifiedUnitsStr = columns[4].trim();
                String workDaysStr = columns[5].trim();
                String workStartTimeStr = columns[6].trim();
                String workEndTimeStr = columns[7].trim();

                Teacher teacher = new Teacher();
                teacher.setId(id);
                teacher.setFirstName(firstName);
                teacher.setLastName(lastName);

                // Set home room
                Room homeRoom = roomMap.get(homeRoomId);
                if (homeRoom != null) {
                    teacher.setHomeRoom(homeRoom);
                }

                // Parse qualified units
                Set<TeachingUnit> qualifiedUnits = parseQualifiedUnits(qualifiedUnitsStr, allTeachingUnits);
                teacher.setQualifiedUnits(qualifiedUnits);

                // Parse work time slots
                Set<TimeSlot> workTimeSlots = parseWorkTimeSlots(
                        workDaysStr,
                        workStartTimeStr,
                        workEndTimeStr,
                        allTimeSlots
                );
                teacher.setWorkTimeSlots(workTimeSlots);

                teachers.add(teacher);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read teachers CSV: " + resourcePath, e);
        }

        return teachers;
    }

    /**
     * Loads lunch groups from CSV file.
     * Format: name,minGrade,maxGrade,lunchStartTime,lunchEndTime
     */
    public static List<LunchGroup> loadLunchGroupsFromCsv(String resourcePath, List<TimeSlot> allTimeSlots) {
        List<LunchGroup> lunchGroups = new ArrayList<>();

        try (BufferedReader br = getBufferedReader(resourcePath)) {
            String headerLine = br.readLine();
            if (headerLine == null) return lunchGroups;

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split(",");
                if (columns.length < 5) {
                    System.out.println("Warning: Skipping malformed lunch group line: " + line);
                    continue;
                }

                String name = columns[0].trim();
                int minGrade = Integer.parseInt(columns[1].trim());
                int maxGrade = Integer.parseInt(columns[2].trim());
                LocalTime lunchStartTime = LocalTime.parse(columns[3].trim());
                LocalTime lunchEndTime = LocalTime.parse(columns[4].trim());

                // Find matching time slots across all days
                List<TimeSlot> lunchTimeSlots = new ArrayList<>();
                for (TimeSlot slot : allTimeSlots) {
                    // Slot overlaps with lunch time if:
                    // slot starts before lunch ends AND slot ends after lunch starts
                    if (slot.getStartTime().isBefore(lunchEndTime) &&
                            slot.getEndTime().isAfter(lunchStartTime)) {
                        lunchTimeSlots.add(slot);
                    }
                }

                lunchGroups.add(new LunchGroup(name, minGrade, maxGrade, lunchTimeSlots));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read lunch groups CSV: " + resourcePath, e);
        }

        return lunchGroups;
    }

    /**
     * Result holder for lesson parsing.
     */
    private static class LessonParseResult {
        List<TeachingUnit> teachingUnits;
        List<SchoolClass> schoolClasses;
        List<Lesson> lessons;

        LessonParseResult(List<TeachingUnit> teachingUnits, List<SchoolClass> schoolClasses, List<Lesson> lessons) {
            this.teachingUnits = teachingUnits;
            this.schoolClasses = schoolClasses;
            this.lessons = lessons;
        }
    }

    /**
     * Parses lessons CSV and creates teaching units, school classes, and lessons.
     */
    private static LessonParseResult parseLessonsCsv(String resourcePath, int classCount) {
        List<TeachingUnit> allTeachingUnits = new ArrayList<>();
        List<SchoolClass> schoolClasses = new ArrayList<>();
        List<Lesson> lessons = new ArrayList<>();

        long lessonIdCounter = 0;
        long teachingUnitCounter = 0;
        long classIdCounter = 0;

        try (BufferedReader br = getBufferedReader(resourcePath)) {
            String headerLine = br.readLine();
            if (headerLine == null) {
                return new LessonParseResult(allTeachingUnits, schoolClasses, lessons);
            }

            String[] headers = headerLine.split(",");

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] columns = line.split(",");

                String gradeStr = columns[0].trim();
                if (gradeStr.isEmpty() || gradeStr.equalsIgnoreCase("nan")) continue;

                int grade = (int) Double.parseDouble(gradeStr);

                List<SchoolClass> classesForThisRow = new ArrayList<>();

                for (int i = 0; i < classCount; i++) {
                    char classSuffix = (char) ('A' + i);

                    SchoolClass schoolClass = new SchoolClass(
                            ++classIdCounter,
                            grade + String.valueOf(classSuffix),
                            grade
                    );

                    schoolClasses.add(schoolClass);
                    classesForThisRow.add(schoolClass);
                }


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
                        System.out.println("Warning: Subject '" + subjectName + "' not found. Skipping.");
                        continue;
                    }

                    RoomType requiredRoom = getRoomTypeForSubject(subject);

                    TeachingUnit unit = new TeachingUnit(++teachingUnitCounter, subject, grade, requiredRoom);

                    // Check if unit already exists
                    TeachingUnit existingUnit = findExistingUnit(allTeachingUnits, unit);
                    if (existingUnit == null) {
                        allTeachingUnits.add(unit);
                        existingUnit = unit;
                    } else {
                        teachingUnitCounter--; // Revert counter since we didn't add a new unit
                    }

                    for (int k = 0; k < count; k++) {
                        for (SchoolClass sClass : classesForThisRow) {
                            lessons.add(new Lesson(++lessonIdCounter, existingUnit, sClass));
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read lessons CSV: " + resourcePath, e);
        }

        return new LessonParseResult(allTeachingUnits, schoolClasses, lessons);
    }

    private static TeachingUnit findExistingUnit(List<TeachingUnit> units, TeachingUnit target) {
        for (TeachingUnit unit : units) {
            if (unit.getSubject() == target.getSubject() && unit.getGrade() == target.getGrade()) {
                return unit;
            }
        }
        return null;
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

    /**
     * Parses qualified units string.
     * Format: SUBJECT:minGrade-maxGrade;SUBJECT:minGrade-maxGrade
     * Example: MATH:1-6;LATVIAN:1-6
     */
    private static Set<TeachingUnit> parseQualifiedUnits(String qualifiedUnitsStr, List<TeachingUnit> allTeachingUnits) {
        Set<TeachingUnit> result = new HashSet<>();

        if (qualifiedUnitsStr == null || qualifiedUnitsStr.isEmpty()) {
            return result;
        }

        String[] unitSpecs = qualifiedUnitsStr.split(";");
        for (String spec : unitSpecs) {
            spec = spec.trim();
            if (spec.isEmpty()) continue;

            String[] parts = spec.split(":");
            if (parts.length != 2) {
                System.out.println("Warning: Invalid qualified unit spec: " + spec);
                continue;
            }

            String subjectStr = parts[0].trim();
            String gradeRangeStr = parts[1].trim();

            Subject subject;
            try {
                subject = Subject.valueOf(subjectStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("Warning: Unknown subject in qualified units: " + subjectStr);
                continue;
            }

            String[] gradeRange = gradeRangeStr.split("-");
            int minGrade = Integer.parseInt(gradeRange[0].trim());
            int maxGrade = gradeRange.length > 1 ? Integer.parseInt(gradeRange[1].trim()) : minGrade;

            // Find matching teaching units
            for (TeachingUnit unit : allTeachingUnits) {
                if (unit.getSubject() == subject &&
                        unit.getGrade() >= minGrade &&
                        unit.getGrade() <= maxGrade) {
                    result.add(unit);
                }
            }
        }

        return result;
    }

    /**
     * Parses work time slots from days and time range.
     * workDaysStr format: MONDAY;TUESDAY;WEDNESDAY
     */
    private static Set<TimeSlot> parseWorkTimeSlots(
            String workDaysStr,
            String workStartTimeStr,
            String workEndTimeStr,
            List<TimeSlot> allTimeSlots
    ) {
        Set<TimeSlot> result = new HashSet<>();

        Set<SchoolDay> workDays = new HashSet<>();
        if (workDaysStr != null && !workDaysStr.isEmpty()) {
            String[] dayStrs = workDaysStr.split(";");
            for (String dayStr : dayStrs) {
                try {
                    workDays.add(SchoolDay.valueOf(dayStr.trim().toUpperCase()));
                } catch (IllegalArgumentException e) {
                    System.out.println("Warning: Unknown day: " + dayStr);
                }
            }
        }

        LocalTime workStartTime = LocalTime.parse(workStartTimeStr);
        LocalTime workEndTime = LocalTime.parse(workEndTimeStr);

        for (TimeSlot slot : allTimeSlots) {
            if (workDays.contains(slot.getSchoolDay()) &&
                    !slot.getStartTime().isBefore(workStartTime) &&
                    !slot.getEndTime().isAfter(workEndTime)) {
                result.add(slot);
            }
        }

        return result;
    }

    /**
     * Generates standard time slots for a school week.
     */
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

    private static BufferedReader getBufferedReader(String resourcePath) throws IOException {
        InputStream is = CsvDataLoader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (is == null) {
            throw new IllegalArgumentException("CSV not found on classpath: " + resourcePath);
        }

        return new BufferedReader(new InputStreamReader(is));
    }
}
