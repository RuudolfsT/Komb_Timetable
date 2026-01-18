# Skolas stundu saraksta optimizators

Spring Boot lietotne optimizētu skolas stundu sarakstu ģenerēšanai, izmantojot Timefold Solver.

## Prasības

- Uzstādīts Docker un Docker Compose
- Lokālai izstrādei bez Docker: Java 17 un Maven 3.9+

## Palaišana ar Docker

1. **Uzbūvē un palaiž lietotni:**
   ```bash
   docker-compose up --build
   ```

2. **Piekļūst lietotnei:**
   - Pēc palaišanas tā būs pieejama: `http://localhost:8080`

3. **Aptur konteinerus:**
   ```bash
   docker-compose down
   ```

## API galapunkti

REST galapunkti stundu saraksta ģenerēšanai. Visi atrodas zem `/api/timetable`:

### Saknes galapunkts
- **GET /api** - Atgriež API informāciju un pieejamos galapunktus

### Timetable galapunkti

1. **Iesniegt problēmu (JSON)**
   - `POST /api/timetable/jobs`
   - Iesniedz problēmu JSON formātā pieprasījuma ķermenī
   - Atgriež: `{ "jobId": "..." }`

2. **Iesniegt problēmu (no visiem CSV failiem)**
   - `POST /api/timetable/jobs/from-all-csv`
   - Ielādē problēmu no visiem CSV failiem (rooms.csv, teachers.csv, lunch_groups.csv, lesson_list.csv)
   - Atgriež: `{ "jobId": "..." }`

3. **Saņemt darba statusu**
   - `GET /api/timetable/jobs/{jobId}`
   - Saņem konkrēta darba statusu
   - Atgriež: `{ "jobId": "...", "status": "..." }`

4. **Saņemt risinājumu**
   - `GET /api/timetable/jobs/{jobId}/solution`
   - Saņem pabeigta darba risinājumu
   - Atgriež: Stundu sarakstu JSON formātā ar skaidrojumu

5. **Saņemt visus darbus**
   - `GET /api/timetable/alljobs`
   - Saņem visu darbu statusus
   - Atgriež: karti ar darba ID un statusiem

### Piemēru izmantošana

1. **Sāk risināšanas darbu no visiem CSV failiem:**
   ```bash
   curl -X POST http://localhost:8080/api/timetable/jobs/from-all-csv
   ```
   Atbilde: `{"jobId": "abc123"}`

2. **Sāk risināšanas darbu no stundu saraksta CSV:**
   ```bash
   curl -X POST http://localhost:8080/api/timetable/jobs/from-csv
   ```
   Atbilde: `{"jobId": "def456"}`

3. **Sāk mazu demonstrācijas problēmu:**
   ```bash
   curl -X POST http://localhost:8080/api/timetable/jobs/smalldemo
   ```
   Atbilde: `{"jobId": "ghi789"}`

4. **Pārbauda darba statusu:**
   ```bash
   curl http://localhost:8080/api/timetable/jobs/abc123
   ```
   Atbilde: `{"jobId": "abc123", "status": "SOLVING"}` vai `{"jobId": "abc123", "status": "COMPLETED"}`

5. **Saņem risinājumu (kad pabeigts):**
   ```bash
   curl http://localhost:8080/api/timetable/jobs/abc123/solution
   ```

6. **Saņem visu darbu statusus:**
   ```bash
   curl http://localhost:8080/api/timetable/alljobs
   ```

### Sagaidāmo datu formāta piemērs ievadei no CSV failiem

#### rooms.csv (telpas)
```csv
id,roomType
101,NORMAL
102,NORMAL
```

#### room_types.csv (telpu tipi)
```csv
roomType,description
NORMAL,Standard classroom
GYM,Gymnasium/Sports hall
```

**Pieejamās vērtības:**
- `NORMAL`
- `GYM`
- `MUSIC`
- `PHYSICS_LAB`
- `CHEMISTRY_LAB`
- `COMPUTER_LAB`

#### teachers.csv (definē informāciju par skolotājiem)
```csv
id,firstName,lastName,homeRoomId,qualifiedUnits,workDays,workStartTime,workEndTime
T1,Anna,Ozola,101,MATH:1-6;LATVIAN:1-6,MONDAY;TUESDAY;WEDNESDAY;THURSDAY;FRIDAY,08:30,16:00
T2,Janis,Berzins,102,MATH:7-12;PHYSICS:7-12,MONDAY;TUESDAY;WEDNESDAY;THURSDAY;FRIDAY,08:30,16:00
```

#### lunchGroups.csv (definē pusdienu laikus klasēm)
```csv
name,minGrade,maxGrade,lunchStartTime,lunchEndTime
Grades 1-6 lunch,1,6,11:00,11:50
```

#### lessons.csv (definē stundu skaitu katrā priekšmetā katrai klašu grupai)
```csv
Grade,MATH,LATVIAN,LITERATURE,FOREIGN_LANG_1,FOREIGN_LANG_2,SPORT,NATURAL_SCIENCES,BIOLOGY,PHYSICS,CHEMISTRY,GEOGRAPHY,MUSIC,ART,HISTORY,SOCIAL_SCIENCES,COMPUTER,ENGINEERING,DESIGN_AND_TECHNOLOGY
1,4,3,1,1,,2,1,,,,,2,2,,2,,,2
```

**Pieejamās priekšmetu vērtības:**
- `MATH`
- `LATVIAN`
- `LITERATURE`
- `FOREIGN_LANG_1`
- `FOREIGN_LANG_2`
- `SPORT`
- `NATURAL_SCIENCES`
- `BIOLOGY`
- `PHYSICS`
- `CHEMISTRY`
- `GEOGRAPHY`
- `MUSIC`
- `ART`
- `HISTORY`
- `SOCIAL_SCIENCES`
- `COMPUTER`
- `ENGINEERING`
- `DESIGN_AND_TECHNOLOGY`
