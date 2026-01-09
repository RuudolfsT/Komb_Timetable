package com.schoolplanner.timetable.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.schoolplanner.timetable.domain.*;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JsonProblemGenerator {

    private static final Random random = new Random();
    private static final String[] FIRST_NAMES = {"Anna", "Jānis", "Pēteris", "Māra", "Andris", "Līga", "Juris", "Inese", "Guntis", "Dace"};
    private static final String[] LAST_NAMES = {"Ozola", "Bērziņš", "Kļaviņš", "Liepiņa", "Kalniņš", "Zariņa", "Pētersons", "Jansone", "Balodis", "Krūmiņa"};

    /**
     * Generates a random timetable problem with configurable parameters
     *
     * @param numClasses Number of school classes to generate
     * @param numTeachers Number of teachers to generate
     * @param numRooms Number of rooms to generate
     * @param lessonsPerClass Average number of lessons per class
     * @param minGrade Minimum grade level (1-12)
     * @param maxGrade Maximum grade level (1-12)
     * @return A randomly generated TimeTable problem
     */
    public static TimeTable generateRandomProblem(
            int numClasses,
            int numTeachers,
            int numRooms,
            int lessonsPerClass,
            int minGrade,
            int maxGrade
    ) {
        List<TimeSlot> timeSlots = generateTimeSlots();
        
        List<Room> rooms = generateRooms(numRooms);
        
        List<SchoolClass> schoolClasses = generateSchoolClasses(numClasses, minGrade, maxGrade);
        
        List<TeachingUnit> teachingUnits = generateTeachingUnits(schoolClasses);
        
        List<Teacher> teachers = generateTeachers(numTeachers, teachingUnits, rooms, timeSlots);
        
        // Ensure all teaching units used in lessons have at least one qualified teacher
        Set<TeachingUnit> teachableUnits = getTeachableUnits(teachers);
        if (teachableUnits.isEmpty()) {
            throw new IllegalStateException("No teachers are qualified for any teaching units. This would create an unsolvable problem.");
        }
        
        List<LunchGroup> lunchGroups = generateLunchGroups(minGrade, maxGrade, timeSlots);
        
        // Only generate lessons for teaching units that have qualified teachers
        List<TeachingUnit> availableUnits = teachingUnits.stream()
                .filter(teachableUnits::contains)
                .toList();
        
        List<Lesson> lessons = generateLessons(schoolClasses, availableUnits, lessonsPerClass);
        
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
     * Generates a default random problem with reasonable defaults
     */
    public static TimeTable generateRandomProblem() {
        return generateRandomProblem(5, 8, 10, 6, 1, 12);
    }

    /**
     * Writes a TimeTable problem to a JSON file
     *
     * @param timeTable The TimeTable to serialize
     * @param filePath The path where to write the JSON file
     * @throws IOException If file writing fails
     */
    public static void writeToJsonFile(TimeTable timeTable, String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        objectMapper.writeValue(new File(filePath), timeTable);
    }

    /**
     * Generates time slots for all school days
     */
    private static List<TimeSlot> generateTimeSlots() {
        List<TimeSlot> timeSlots = new ArrayList<>();
        long slotId = 1L;
        
        // Generate slots for each day: 8:00 to 15:00, 45-minute lessons with 15-minute breaks
        LocalTime[] startTimes = {
                LocalTime.of(8, 0),   // 8:00
                LocalTime.of(9, 0),   // 9:00
                LocalTime.of(10, 0),  // 10:00
                LocalTime.of(11, 0),  // 11:00
                LocalTime.of(12, 0),  // 12:00
                LocalTime.of(13, 0),  // 13:00
                LocalTime.of(14, 0)   // 14:00
        };
        
        for (SchoolDay day : SchoolDay.values()) {
            for (LocalTime startTime : startTimes) {
                LocalTime endTime = startTime.plusMinutes(45);
                timeSlots.add(new TimeSlot(slotId++, day, startTime, endTime));
            }
        }
        
        return timeSlots;
    }

    /**
     * Generates rooms with various types
     */
    private static List<Room> generateRooms(int numRooms) {
        List<Room> rooms = new ArrayList<>();
        RoomType[] roomTypes = RoomType.values();
        
        // Ensure at least one room of each type
        int roomNumber = 101;
        for (RoomType roomType : roomTypes) {
            rooms.add(new Room(String.valueOf(roomNumber++), roomType));
        }
        
        // Fill remaining rooms with random types
        while (rooms.size() < numRooms) {
            RoomType randomType = roomTypes[random.nextInt(roomTypes.length)];
            rooms.add(new Room(String.valueOf(roomNumber++), randomType));
        }
        
        return rooms;
    }

    /**
     * Generates school classes
     */
    private static List<SchoolClass> generateSchoolClasses(int numClasses, int minGrade, int maxGrade) {
        List<SchoolClass> classes = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        long classId = 1L;
        
        for (int i = 0; i < numClasses; i++) {
            int grade = minGrade + random.nextInt(maxGrade - minGrade + 1);
            String className;
            do {
                char letter = (char) ('A' + random.nextInt(6)); // A-F
                className = grade + String.valueOf(letter);
            } while (usedNames.contains(className));
            
            usedNames.add(className);
            classes.add(new SchoolClass(classId++, className, grade));
        }
        
        return classes;
    }

    /**
     * Generates teaching units based on school classes
     */
    private static List<TeachingUnit> generateTeachingUnits(List<SchoolClass> schoolClasses) {
        List<TeachingUnit> teachingUnits = new ArrayList<>();
        Set<String> usedCombinations = new HashSet<>();
        long unitId = 1L;
        
        // Get unique grades
        Set<Integer> grades = schoolClasses.stream()
                .map(SchoolClass::getGrade)
                .collect(Collectors.toSet());
        
        for (Integer grade : grades) {
            // Generate teaching units for common subjects
            Subject[] commonSubjects = {
                    Subject.MATH, Subject.LATVIAN, Subject.LITERATURE,
                    Subject.FOREIGN_LANG_1, Subject.SPORT, Subject.HISTORY
            };
            
            for (Subject subject : commonSubjects) {
                RoomType requiredRoomType = getRoomTypeForSubject(subject);
                String key = grade + "-" + subject;
                if (!usedCombinations.contains(key)) {
                    usedCombinations.add(key);
                    teachingUnits.add(new TeachingUnit(unitId++, subject, grade, requiredRoomType));
                }
            }
            
            // Add some specialized subjects for higher grades
            if (grade >= 7) {
                Subject[] specializedSubjects = {
                        Subject.CHEMISTRY, Subject.PHYSICS, Subject.BIOLOGY,
                        Subject.GEOGRAPHY, Subject.COMPUTER
                };
                
                for (Subject subject : specializedSubjects) {
                    if (random.nextDouble() < 0.6) { // 60% chance
                        RoomType requiredRoomType = getRoomTypeForSubject(subject);
                        String key = grade + "-" + subject;
                        if (!usedCombinations.contains(key)) {
                            usedCombinations.add(key);
                            teachingUnits.add(new TeachingUnit(unitId++, subject, grade, requiredRoomType));
                        }
                    }
                }
            }
        }
        
        return teachingUnits;
    }

    /**
     * Gets the required room type for a subject
     */
    private static RoomType getRoomTypeForSubject(Subject subject) {
        return switch (subject) {
            case SPORT -> RoomType.GYM;
            case MUSIC -> RoomType.MUSIC;
            case PHYSICS -> RoomType.PHYSICS_LAB;
            case CHEMISTRY -> RoomType.CHEMISTRY_LAB;
            case COMPUTER -> RoomType.COMPUTER_LAB;
            default -> RoomType.NORMAL;
        };
    }

    /**
     * Generates teachers with qualified units
     */
    private static List<Teacher> generateTeachers(
            int numTeachers,
            List<TeachingUnit> teachingUnits,
            List<Room> rooms,
            List<TimeSlot> timeSlots
    ) {
        List<Teacher> teachers = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();
        
        for (int i = 0; i < numTeachers; i++) {
            Teacher teacher = new Teacher();
            teacher.setId("T" + (i + 1));
            
            // Generate unique name
            String fullName;
            do {
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                fullName = firstName + " " + lastName;
            } while (usedNames.contains(fullName));
            usedNames.add(fullName);
            
            String[] nameParts = fullName.split(" ");
            teacher.setFirstName(nameParts[0]);
            teacher.setLastName(nameParts[1]);
            
            // Assign home room (prefer normal rooms)
            List<Room> normalRooms = rooms.stream()
                    .filter(r -> r.getRoomType() == RoomType.NORMAL)
                    .toList();
            if (!normalRooms.isEmpty()) {
                teacher.setHomeRoom(normalRooms.get(random.nextInt(normalRooms.size())));
            } else {
                teacher.setHomeRoom(rooms.get(random.nextInt(rooms.size())));
            }
            
            // Assign qualified units (2-4 units per teacher)
            int numQualifiedUnits = 2 + random.nextInt(3);
            Set<TeachingUnit> qualifiedUnits = new HashSet<>();
            for (int j = 0; j < numQualifiedUnits && j < teachingUnits.size(); j++) {
                TeachingUnit unit = teachingUnits.get(random.nextInt(teachingUnits.size()));
                qualifiedUnits.add(unit);
            }
            teacher.setQualifiedUnits(qualifiedUnits);
            
            // Generate working hours (available on most days, but not necessarily all)
            Map<SchoolDay, List<TimeRange>> workingHours = new HashMap<>();
            Set<TimeSlot> workTimeSlots = new HashSet<>();
            
            for (SchoolDay day : SchoolDay.values()) {
                // 80% chance teacher works on this day
                if (random.nextDouble() < 0.8) {
                    // Generate working time range (e.g., 8:00-14:00)
                    LocalTime workStart = LocalTime.of(8, 0);
                    LocalTime workEnd = LocalTime.of(13, 0).plusMinutes(45);
                    workingHours.put(day, List.of(new TimeRange(workStart, workEnd)));
                    
                    // Add corresponding time slots
                    timeSlots.stream()
                            .filter(ts -> ts.getSchoolDay() == day)
                            .filter(ts -> !ts.getStartTime().isBefore(workStart) && !ts.getStartTime().isAfter(workEnd.minusMinutes(45)))
                            .forEach(workTimeSlots::add);
                }
            }
            
            teacher.setWorkingHours(workingHours);
            teacher.setWorkTimeSlots(workTimeSlots);
            
            teachers.add(teacher);
        }
        
        return teachers;
    }

    /**
     * Gets the set of teaching units that have at least one qualified teacher
     */
    private static Set<TeachingUnit> getTeachableUnits(List<Teacher> teachers) {
        Set<TeachingUnit> teachableUnits = new HashSet<>();
        for (Teacher teacher : teachers) {
            if (teacher.getQualifiedUnits() != null) {
                teachableUnits.addAll(teacher.getQualifiedUnits());
            }
        }
        return teachableUnits;
    }

    /**
     * Generates lunch groups
     */
    private static List<LunchGroup> generateLunchGroups(int minGrade, int maxGrade, List<TimeSlot> timeSlots) {
        List<LunchGroup> lunchGroups = new ArrayList<>();
        
        // Find a single lunch time slot (preferably Monday at 12:00, or first available around noon)
        TimeSlot lunchTimeSlot = timeSlots.stream()
                .filter(ts -> ts.getSchoolDay() == SchoolDay.MONDAY)
                .filter(ts -> ts.getStartTime().equals(LocalTime.of(12, 0)) || ts.getStartTime().equals(LocalTime.of(11, 0)))
                .findFirst()
                .orElse(null);
        
        if (lunchTimeSlot == null) {
            // Fallback: use first available slot around noon (any day)
            lunchTimeSlot = timeSlots.stream()
                    .filter(ts -> ts.getStartTime().getHour() >= 11 && ts.getStartTime().getHour() <= 13)
                    .findFirst()
                    .orElse(null);
        }
        
        if (lunchTimeSlot != null) {
            // Use only a single timeslot for each lunch group
            List<TimeSlot> singleLunchSlot = List.of(lunchTimeSlot);
            
            // Create lunch groups for different grade ranges
            if (maxGrade - minGrade >= 6) {
                // Split into two groups
                int midGrade = (minGrade + maxGrade) / 2;
                lunchGroups.add(new LunchGroup(
                        "Grades " + minGrade + "-" + midGrade + " lunch",
                        minGrade,
                        midGrade,
                        singleLunchSlot
                ));
                lunchGroups.add(new LunchGroup(
                        "Grades " + (midGrade + 1) + "-" + maxGrade + " lunch",
                        midGrade + 1,
                        maxGrade,
                        singleLunchSlot
                ));
            } else {
                // Single lunch group
                lunchGroups.add(new LunchGroup(
                        "Grades " + minGrade + "-" + maxGrade + " lunch",
                        minGrade,
                        maxGrade,
                        singleLunchSlot
                ));
            }
        }
        
        return lunchGroups;
    }

    /**
     * Generates lessons for school classes
     */
    private static List<Lesson> generateLessons(
            List<SchoolClass> schoolClasses,
            List<TeachingUnit> teachingUnits,
            int lessonsPerClass
    ) {
        List<Lesson> lessons = new ArrayList<>();
        long lessonId = 1L;
        
        // Group teaching units by grade
        Map<Integer, List<TeachingUnit>> unitsByGrade = teachingUnits.stream()
                .collect(Collectors.groupingBy(TeachingUnit::getGrade));
        
        for (SchoolClass schoolClass : schoolClasses) {
            int grade = schoolClass.getGrade();
            List<TeachingUnit> availableUnits = unitsByGrade.getOrDefault(grade, new ArrayList<>());
            
            if (availableUnits.isEmpty()) {
                continue;
            }
            
            // Generate lessons for this class
            int numLessons = lessonsPerClass + random.nextInt(3) - 1; // lessonsPerClass ± 1
            for (int i = 0; i < numLessons; i++) {
                TeachingUnit unit = availableUnits.get(random.nextInt(availableUnits.size()));
                lessons.add(new Lesson(lessonId++, unit, schoolClass));
            }
        }
        
        return lessons;
    }

    /**
     * Main method for testing - generates a problem and writes it to a file
     */
    public static void main(String[] args) {
        try {
            // Generate a random problem
            TimeTable problem = generateRandomProblem(6, 10, 12, 7, 1, 9);
            
            // Write to JSON file
            String outputPath = "generated_problem.json";
            writeToJsonFile(problem, outputPath);
            
            System.out.println("Random timetable problem generated successfully!");
            System.out.println("Output file: " + outputPath);
            System.out.println("Classes: " + problem.getSchoolClasses().size());
            System.out.println("Teachers: " + problem.getTeachers().size());
            System.out.println("Rooms: " + problem.getRooms().size());
            System.out.println("Lessons: " + problem.getLessons().size());
            System.out.println("Time slots: " + problem.getTimeSlots().size());
        } catch (IOException e) {
            System.err.println("Error generating problem: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
