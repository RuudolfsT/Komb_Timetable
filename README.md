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

3. **Saņemt darba statusu**
   - `GET /api/timetable/jobs/{jobId}`
   - Saņem konkrēta darba statusu
   - Atgriež: `{ "jobId": "...", "status": "..." }`

4. **Saņemt risinājumu**
   - `GET /api/timetable/jobs/{jobId}/solution`
   - Saņem pabeigta darba risinājumu
   - Atgriež: Stundu sarakstu ar skaidrojumu

5. **Saņemt visus darbus**
   - `GET /api/timetable/alljobs`
   - Saņem visu darbu statusus
   - Atgriež: karti ar darba ID un statusiem

### Piemēru izmantošana

1. **Sāk risināšanas darbu no CSV:**
   ```bash
   curl -X POST http://localhost:8080/api/timetable/jobs/from-csv
   ```
   Atbilde: `{"jobId": "abc123"}`

2. **Pārbauda darba statusu:**
   ```bash
   curl http://localhost:8080/api/timetable/jobs/abc123
   ```

3. **Saņem risinājumu (kad pabeigts):**
   ```bash
   curl http://localhost:8080/api/timetable/jobs/abc123/solution
   ```
