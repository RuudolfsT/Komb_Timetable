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
- **GET /** - Atgriež API informāciju un pieejamos galapunktus

### Timetable galapunkti

1. **Iesniegt problēmu (JSON)**
   - `POST /api/timetable/jobs`
   - Iesniedz problēmu JSON formātā pieprasījuma ķermenī
   - Atgriež: `{ "jobId": "..." }`

2. **Iesniegt problēmu (CSV)**
   - `POST /api/timetable/jobs/from-csv`
   - Iesniedz problēmu, izmantojot CSV failu
   - Atgriež: `{ "jobId": "..." }`

3. **Ģenerēt un risināt nejaušu problēmu**
   - `POST /api/timetable/jobs/create-problem`
   - Ģenerē nejaušu stundu saraksta problēmu un automātiski sāk to risināt
   - Opcionālie parametri (query parameters):
     - `numClasses` (noklusējums: 5) - Skolu klašu skaits
     - `numTeachers` (noklusējums: 8) - Skolotāju skaits
     - `numRooms` (noklusējums: 10) - Telpu skaits
     - `lessonsPerClass` (noklusējums: 6) - Vidējais stundu skaits uz klasi
     - `minGrade` (noklusējums: 1) - Minimālais klases līmenis
     - `maxGrade` (noklusējums: 12) - Maksimālais klases līmenis
   - Atgriež: `{ "jobId": "..." }`

4. **Saņemt darba statusu**
   - `GET /api/timetable/jobs/{jobId}`
   - Saņem konkrēta darba statusu
   - Atgriež: `{ "jobId": "...", "status": "..." }`

5. **Saņemt risinājumu**
   - `GET /api/timetable/jobs/{jobId}/solution`
   - Saņem pabeigta darba risinājumu
   - Atgriež: Stundu sarakstu ar skaidrojumu

6. **Saņemt visus darbus**
   - `GET /api/timetable/alljobs`
   - Saņem visu darbu statusus
   - Atgriež: karti ar darba ID un statusiem

### Piemēru izmantošana

1. **Sāk risināšanas darbu no CSV:**
   ```bash
   curl -X POST http://localhost:8080/api/timetable/jobs/from-csv
   ```
   Atbilde: `{"jobId": "abc123"}`

2. **Ģenerē un risina nejaušu problēmu (ar noklusējuma parametriem):**
   ```bash
   curl -X POST http://localhost:8080/api/timetable/jobs/create-problem
   ```
   Atbilde: `{"jobId": "xyz789"}`

3. **Ģenerē un risina nejaušu problēmu (ar pielāgotiem parametriem):**
   ```bash
   curl -X POST "http://localhost:8080/api/timetable/jobs/create-problem?numClasses=6&numTeachers=10&numRooms=12&lessonsPerClass=7&minGrade=1&maxGrade=9"
   ```
   Atbilde: `{"jobId": "def456"}`

4. **Pārbauda darba statusu:**
   ```bash
   curl http://localhost:8080/api/timetable/jobs/abc123
   ```

5. **Saņem risinājumu (kad pabeigts):**
   ```bash
   curl http://localhost:8080/api/timetable/jobs/abc123/solution
   ```
