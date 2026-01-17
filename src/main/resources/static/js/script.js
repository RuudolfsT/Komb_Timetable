const API_BASE = '/api/timetable';
const DAY_ORDER = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'];
const DAY_NAMES = { MONDAY: 'Monday', TUESDAY: 'Tuesday', WEDNESDAY: 'Wednesday', THURSDAY: 'Thursday', FRIDAY: 'Friday' };

let solutionLessons = [];
let lessonsByClass = {};
let sortedTimeSlots = [];
let classInfoMap = new Map(); // className -> { name, grade }
let lunchGroups = []; // Store lunch groups from the solution
let autoRetryTimeout = null; // Track auto-retry timeout to cancel if needed

function showStatus(message, type) {
    const el = document.getElementById('status');
    el.textContent = message;
    el.className = `status ${type}`;
    el.style.display = 'block';
}
function hideStatus() { document.getElementById('status').style.display = 'none'; }

function showEndpointStatus(message, type) {
    const el = document.getElementById('endpointStatus');
    el.textContent = message;
    el.className = `endpoint-status ${type}`;
    el.style.display = 'block';
}
function hideEndpointStatus() { document.getElementById('endpointStatus').style.display = 'none'; }

async function submitEndpoint() {
    const endpoint = document.getElementById('endpointSelect').value;
    
    if (!endpoint) {
        showEndpointStatus('Please select an endpoint', 'error');
        return;
    }
    
    const solveBtn = document.getElementById('solveBtn');
    solveBtn.disabled = true;
    showEndpointStatus('Submitting problem...', 'info');
    toggleLoading(true);
    hideStatus();
    resetView();
    
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, { method: 'POST' });
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        const data = await response.json();
        const jobId = data.jobId;
        
        showEndpointStatus(`Problem submitted! Job ID: ${jobId}`, 'success');
        
        // Auto-fill the job ID input and clear endpoint select
        document.getElementById('jobIdInput').value = jobId;
        document.getElementById('endpointSelect').value = '';
        
        // Optional: auto-fetch the solution after a short delay
        setTimeout(() => {
            fetchSolution();
        }, 1500);
        
    } catch (error) {
        showEndpointStatus(`Error: ${error.message}`, 'error');
        console.error('Error:', error);
        toggleLoading(false);
    } finally {
        solveBtn.disabled = false;
    }
}

async function submitUploadedFiles() {
    const roomsCsv = document.getElementById('roomsCsvFile').files[0];
    const teachersCsv = document.getElementById('teachersCsvFile').files[0];
    const lunchGroupsCsv = document.getElementById('lunchGroupsCsvFile').files[0];
    const lessonsCsv = document.getElementById('lessonsCsvFile').files[0];
    const classCount = parseInt(document.getElementById('classCountInput').value) || 1;

    // Validate that all files are selected
    if (!roomsCsv || !teachersCsv || !lunchGroupsCsv || !lessonsCsv) {
        showUploadStatus('Please select all four CSV files', 'error');
        return;
    }

    const uploadBtn = document.getElementById('uploadBtn');
    uploadBtn.disabled = true;
    showUploadStatus('Uploading files and submitting problem...', 'info');
    toggleLoading(true);
    hideStatus();
    resetView();

    try {
        // Create FormData to send files
        const formData = new FormData();
        formData.append('roomsCsv', roomsCsv);
        formData.append('teachersCsv', teachersCsv);
        formData.append('lunchGroupsCsv', lunchGroupsCsv);
        formData.append('lessonsCsv', lessonsCsv);
        formData.append('classCount', classCount);

        const response = await fetch(`${API_BASE}/jobs/upload`, {
            method: 'POST',
            body: formData
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        const jobId = data.jobId;

        showUploadStatus(`Problem submitted! Job ID: ${jobId}`, 'success');

        // Auto-fill the job ID input
        document.getElementById('jobIdInput').value = jobId;

        // Clear file inputs
        document.getElementById('roomsCsvFile').value = '';
        document.getElementById('teachersCsvFile').value = '';
        document.getElementById('lunchGroupsCsvFile').value = '';
        document.getElementById('lessonsCsvFile').value = '';

        // Optional: auto-fetch the solution after a short delay
        setTimeout(() => {
            fetchSolution();
        }, 1500);

    } catch (error) {
        showUploadStatus(`Error: ${error.message}`, 'error');
        console.error('Error:', error);
        toggleLoading(false);
    } finally {
        uploadBtn.disabled = false;
    }
}

function showUploadStatus(message, type) {
    const el = document.getElementById('uploadStatus');
    el.textContent = message;
    el.className = `endpoint-status ${type}`;
    el.style.display = 'block';
}
function hideUploadStatus() { document.getElementById('uploadStatus').style.display = 'none'; }

function formatScore(score) {
    if (!score) return { hard: '0', soft: '0' };
    const [hardPart, softPart] = score.split('/');
    return {
        hard: hardPart ? hardPart.replace('hard', '') : '0',
        soft: softPart ? softPart.replace('soft', '') : '0'
    };
}

function parseConstraintScore(scoreStr) {
    if (!scoreStr) return { hard: 0, soft: 0 };
    const [hardPart, softPart] = scoreStr.split('/');
    return {
        hard: hardPart ? parseInt(hardPart.replace('hard', '') || '0') : 0,
        soft: softPart ? parseInt(softPart.replace('soft', '') || '0') : 0
    };
}

function stripPackagePrefix(constraintName) {
    if (!constraintName) return constraintName;
    
    // Timefold Solver constraint names are in format:
    // "com.schoolplanner.timetable.domain.ClassName/Constraint name"
    // We want to extract just the constraint name part after the slash
    
    // If there's a slash, take everything after it
    if (constraintName.includes('/')) {
        return constraintName.split('/').slice(1).join('/');
    }
    
    // If no slash, remove package prefix and class name
    // Remove "com.schoolplanner.timetable.domain." prefix
    let cleaned = constraintName.replace(/^com\.schoolplanner\.timetable\.domain\./, '');
    
    // Remove class name (everything up to the first dot or end of string)
    // This handles cases like "Lesson.Teacher cannot teach..." 
    const dotIndex = cleaned.indexOf('.');
    if (dotIndex > 0) {
        cleaned = cleaned.substring(dotIndex + 1);
    }
    
    return cleaned;
}

function displayConstraintMatches(constraintMatches) {
    const container = document.getElementById('constraintMatches');
    if (!constraintMatches || Object.keys(constraintMatches).length === 0) {
        container.innerHTML = '';
        return;
    }
    
    const hardConstraints = [];
    const softConstraints = [];
    
    Object.entries(constraintMatches).forEach(([name, scoreStr]) => {
        const score = parseConstraintScore(scoreStr);
        const cleanName = stripPackagePrefix(name);
        const constraint = { name: cleanName, scoreStr, score };
        
        // If constraint has a hard score component (non-zero), it's a hard constraint
        // Otherwise, it's a soft constraint
        if (score.hard !== 0) {
            hardConstraints.push(constraint);
        } else {
            softConstraints.push(constraint);
        }
    });
    
    let html = '';
    
    // Sort constraints: violated first, then by score (most negative first)
    hardConstraints.sort((a, b) => {
        if (a.score.hard < 0 && b.score.hard >= 0) return -1;
        if (a.score.hard >= 0 && b.score.hard < 0) return 1;
        return a.score.hard - b.score.hard;
    });
    
    softConstraints.sort((a, b) => {
        if (a.score.soft < 0 && b.score.soft >= 0) return -1;
        if (a.score.soft >= 0 && b.score.soft < 0) return 1;
        return a.score.soft - b.score.soft;
    });
    
    if (hardConstraints.length > 0) {
        html += '<div class="constraint-group">';
        html += '<div class="constraint-group-title">Hard Constraints</div>';
        html += '<div class="constraint-list">';
        hardConstraints.forEach(constraint => {
            const isViolated = constraint.score.hard < 0;
            html += `<div class="constraint-item ${isViolated ? 'violated' : 'satisfied'}">`;
            html += `<div class="constraint-name">${constraint.name}</div>`;
            html += `<div class="constraint-score">Score: ${constraint.scoreStr}</div>`;
            html += '</div>';
        });
        html += '</div></div>';
    }
    
    if (softConstraints.length > 0) {
        html += '<div class="constraint-group">';
        html += '<div class="constraint-group-header" onclick="toggleSoftConstraints()">';
        html += '<div class="constraint-group-title">Constraints</div>';
        html += '<span class="constraint-arrow" id="softConstraintsArrow">▼</span>';
        html += '</div>';
        html += '<div class="constraint-list hidden" id="softConstraintsList">';
        softConstraints.forEach(constraint => {
            const isViolated = constraint.score.soft < 0;
            html += `<div class="constraint-item ${isViolated ? 'violated' : 'satisfied'}">`;
            html += `<div class="constraint-name">${constraint.name}</div>`;
            html += `<div class="constraint-score">Score: ${constraint.scoreStr}</div>`;
            html += '</div>';
        });
        html += '</div></div>';
    }
    
    container.innerHTML = html;
}
function formatTime(time) { return time ? time.substring(0,5) : 'N/A'; }
function getTimeSlotKey(ts) { return ts && ts.schoolDay && ts.startTime ? `${ts.schoolDay}_${ts.startTime}` : null; }

// Helper to convert time to minutes - handles both string "HH:mm" and array [H, M] formats
function timeToMinutes(time) {
    if (!time) return null;
    
    // Handle array format [hours, minutes] (Jackson default for LocalTime)
    if (Array.isArray(time)) {
        return time[0] * 60 + (time[1] || 0);
    }
    
    // Handle string format "HH:mm" or "HH:mm:ss"
    if (typeof time === 'string') {
        const [hours, minutes] = time.substring(0, 5).split(':').map(Number);
        return hours * 60 + minutes;
    }
    
    return null;
}

function isLunchBreak(grade, startTime, endTime) {
    if (!grade || !startTime || !endTime || !lunchGroups || lunchGroups.length === 0) return false;
    
    // Find the lunch group that applies to this grade
    const lunchGroup = lunchGroups.find(lg => 
        lg.minGrade <= grade && grade <= lg.maxGrade
    );
    
    if (!lunchGroup || !lunchGroup.lunchTimeSlots || lunchGroup.lunchTimeSlots.length === 0) {
        return false;
    }
    
    const startMins = timeToMinutes(startTime);
    const endMins = timeToMinutes(endTime);
    
    if (startMins === null || endMins === null) return false;
    
    // Check if the time slot overlaps with any lunch time slot
    return lunchGroup.lunchTimeSlots.some(lunchSlot => {
        const lunchStartMins = timeToMinutes(lunchSlot.startTime);
        const lunchEndMins = timeToMinutes(lunchSlot.endTime);
        
        if (lunchStartMins === null || lunchEndMins === null) return false;
        
        // A time slot overlaps lunch if: start < lunchEnd && end > lunchStart
        return startMins < lunchEndMins && endMins > lunchStartMins;
    });
}
function compareTimeSlots(a, b) {
    const dA = DAY_ORDER.indexOf(a.schoolDay);
    const dB = DAY_ORDER.indexOf(b.schoolDay);
    if (dA !== dB) return dA - dB;
    return a.startTime.localeCompare(b.startTime);
}

async function fetchSolution(isRetry = false) {
    const jobId = document.getElementById('jobIdInput').value.trim();
    if (!jobId) { showStatus('Please enter a Job ID', 'error'); return; }

    if (!isRetry) {
        hideStatus();
        toggleLoading(true);
        resetView();
    }

    try {
        const res = await fetch(`${API_BASE}/jobs/${jobId}/solution`);
        if (res.status === 404) { 
            toggleLoading(false);
            showStatus('Job not found. Please check the Job ID.', 'error'); 
            return; 
        }
        if (res.status === 409) { 
            // Auto-retry after 500ms, keep spinner visible and status hidden
            if (autoRetryTimeout) clearTimeout(autoRetryTimeout);
            autoRetryTimeout = setTimeout(() => {
                fetchSolution(true);
            }, 500);
            return; 
        }
        if (!res.ok) throw new Error(`HTTP error ${res.status}`);

        const data = await res.json();
        handleSolution(data);
        showStatus('Solution loaded. Select a class to view.', 'success');
        toggleLoading(false);
        if (autoRetryTimeout) clearTimeout(autoRetryTimeout);
    } catch (err) {
        console.error(err);
        showStatus(`Error: ${err.message}`, 'error');
        toggleLoading(false);
        if (autoRetryTimeout) clearTimeout(autoRetryTimeout);
    }
}

function handleSolution(data) {
    solutionLessons = data.lessons || [];
    lunchGroups = data.lunchGroups || [];
    if (solutionLessons.length === 0) {
        document.getElementById('emptyState').style.display = 'block';
        return;
    }

    const score = formatScore(data.score || '0hard/0soft');
    const hardEl = document.getElementById('hardScore');
    hardEl.textContent = score.hard;
    hardEl.className = `score-value ${parseInt(score.hard) === 0 ? 'hard-positive' : 'hard-negative'}`;
    document.getElementById('softScore').textContent = score.soft;
    document.getElementById('scoreSection').style.display = 'block';
    
    // Display constraint matches
    displayConstraintMatches(data.constraintMatches || {});

    lessonsByClass = {};
    classInfoMap = new Map();
    const timeSlotMap = new Map();

    solutionLessons.forEach(lesson => {
        const className = lesson.schoolClass?.name || 'Unknown';
        const classGrade = lesson.schoolClass?.grade ?? null;
        classInfoMap.set(className, { name: className, grade: classGrade });
        if (!lessonsByClass[className]) lessonsByClass[className] = [];
        lessonsByClass[className].push(lesson);

        if (lesson.timeSlot) {
            const key = getTimeSlotKey(lesson.timeSlot);
            if (key && !timeSlotMap.has(key)) {
                timeSlotMap.set(key, lesson.timeSlot);
            }
        }
    });

    // Also include lunch time slots so they appear in the timetable even if no lesson is scheduled
    lunchGroups.forEach(lg => {
        if (lg.lunchTimeSlots) {
            lg.lunchTimeSlots.forEach(ts => {
                const key = getTimeSlotKey(ts);
                if (key && !timeSlotMap.has(key)) {
                    timeSlotMap.set(key, ts);
                }
            });
        }
    });

    sortedTimeSlots = Array.from(timeSlotMap.values()).sort(compareTimeSlots);

    const classList = Array.from(classInfoMap.values()).sort((a, b) => {
        const gradeA = a.grade ?? Number.MAX_SAFE_INTEGER;
        const gradeB = b.grade ?? Number.MAX_SAFE_INTEGER;
        if (gradeA !== gradeB) return gradeA - gradeB;
        return (a.name || '').localeCompare(b.name || '');
    });

    populateClassSelect(classList);
}

function populateClassSelect(classList) {
    const select = document.getElementById('classSelect');
    select.innerHTML = '<option value=\"\">Select class...</option>';
    classList.forEach(({ name }) => {
        const opt = document.createElement('option');
        opt.value = name;
        opt.textContent = name; // no numeric prefix in the label
        select.appendChild(opt);
    });
    select.disabled = false;
}

function renderSelectedClass() {
    const className = document.getElementById('classSelect').value;
    if (!className) {
        resetTable();
        return;
    }
    renderTableForClass(className);
}

function renderTableForClass(className) {
    const lessons = lessonsByClass[className] || [];
    const table = document.getElementById('timetableTable');
    table.innerHTML = '';
    
    // Get class grade for lunch break detection
    const classInfo = classInfoMap.get(className);
    const classGrade = classInfo ? classInfo.grade : null;

    // build header
    const thead = document.createElement('thead');
    const headerRow = document.createElement('tr');
    const timeTh = document.createElement('th');
    timeTh.textContent = 'Time';
    headerRow.appendChild(timeTh);
    DAY_ORDER.forEach(day => {
        const th = document.createElement('th');
        th.textContent = DAY_NAMES[day];
        headerRow.appendChild(th);
    });
    thead.appendChild(headerRow);
    table.appendChild(thead);

    const tbody = document.createElement('tbody');

    // map lessons by day/time for quick lookup
    const lessonMap = new Map();
    lessons.forEach(l => {
        if (l.timeSlot) {
            const key = `${l.timeSlot.schoolDay}_${l.timeSlot.startTime}_${l.timeSlot.endTime}`;
            lessonMap.set(key, l);
        }
    });

    // group time slots by start/end regardless of day to build rows
    const timeRows = [];
    const uniqueTimes = new Map();
    sortedTimeSlots.forEach(ts => {
        const key = `${ts.startTime}_${ts.endTime}`;
        if (!uniqueTimes.has(key)) uniqueTimes.set(key, { start: ts.startTime, end: ts.endTime });
    });
    uniqueTimes.forEach(v => timeRows.push(v));
    timeRows.sort((a,b) => a.start.localeCompare(b.start));

    // // Filter out lunch time slots that don't apply to this class's grade
    // const filteredTimeRows = timeRows.filter(t => {
    //     // Check if this time slot is a lunch slot for a DIFFERENT grade group
    //     const isLunchForOtherGrade = lunchGroups.some(lg => {
    //         // Check if this lunch group does NOT apply to this class
    //         const appliesToThisClass = classGrade && lg.minGrade <= classGrade && classGrade <= lg.maxGrade;
    //         if (appliesToThisClass) return false; // This lunch group applies to this class, don't filter out
    //
    //         // Check if this time slot matches a lunch slot in this (other) lunch group
    //         return lg.lunchTimeSlots && lg.lunchTimeSlots.some(lunchSlot => {
    //             const lunchStart = lunchSlot.startTime;
    //             const lunchEnd = lunchSlot.endTime;
    //             return t.start === lunchStart && t.end === lunchEnd;
    //         });
    //     });
    //
    //     return !isLunchForOtherGrade;
    // });
    //
    // filteredTimeRows.forEach(t => {
    //     const row = document.createElement('tr');
    //     const timeTd = document.createElement('td');
    //     timeTd.textContent = `${formatTime(t.start)} - ${formatTime(t.end)}`;
    //     row.appendChild(timeTd);
    //
    //     DAY_ORDER.forEach(day => {
    //         const key = `${day}_${t.start}_${t.end}`;
    //         const lesson = lessonMap.get(key);
    //         const td = document.createElement('td');
    //         if (lesson) {
    //             td.innerHTML = renderLesson(lesson);
    //         } else {
    //             // Check if this is a lunch break
    //             if (classGrade && isLunchBreak(classGrade, t.start, t.end)) {
    //                 td.innerHTML = '<div class="lunch-break">Lunch break</div>';
    //             } else {
    //                 td.innerHTML = '<span class="empty">—</span>';
    //             }
    //         }
    //         row.appendChild(td);
    //     });
    //     tbody.appendChild(row);
    // });

    timeRows.forEach(t => {  // Changed from filteredTimeRows.forEach to timeRows.forEach
        const row = document.createElement('tr');
        const timeTd = document.createElement('td');
        timeTd.textContent = `${formatTime(t.start)} - ${formatTime(t.end)}`;
        row.appendChild(timeTd);

        DAY_ORDER.forEach(day => {
            const key = `${day}_${t.start}_${t.end}`;
            const lesson = lessonMap.get(key);
            const td = document.createElement('td');
            if (lesson) {
                td.innerHTML = renderLesson(lesson);
            } else {
                // Check if this is a lunch break
                if (classGrade && isLunchBreak(classGrade, t.start, t.end)) {
                    td.innerHTML = '<div class="lunch-break">Lunch break</div>';
                } else {
                    td.innerHTML = '<span class="empty">—</span>';
                }
            }
            row.appendChild(td);
        });
        tbody.appendChild(row);
    });

    table.appendChild(tbody);
    document.getElementById('timetableContainer').style.display = 'block';
    document.getElementById('emptyState').style.display = lessons.length === 0 ? 'block' : 'none';
    
    // Equalize heights of cards in each row
    equalizeRowHeights();
}

function equalizeRowHeights() {
    const rows = document.querySelectorAll('#timetableTable tbody tr');
    rows.forEach(row => {
        const cells = Array.from(row.querySelectorAll('td:not(:first-child)'));
        if (cells.length === 0) return;
        
        // Reset heights to auto to get natural heights
        cells.forEach(cell => {
            const content = cell.querySelector('.lesson, .lunch-break, .empty');
            if (content) {
                content.style.height = 'auto';
            }
        });
        
        // Find the maximum height
        let maxHeight = 0;
        cells.forEach(cell => {
            const content = cell.querySelector('.lesson, .lunch-break, .empty');
            if (content) {
                const height = content.offsetHeight;
                if (height > maxHeight) {
                    maxHeight = height;
                }
            }
        });
        
        // Set all content to the maximum height
        if (maxHeight > 0) {
            cells.forEach(cell => {
                const content = cell.querySelector('.lesson, .lunch-break, .empty');
                if (content) {
                    content.style.height = maxHeight + 'px';
                }
            });
        }
    });
}

function renderLesson(lesson) {
    const subject = lesson.teachingUnit?.subject || 'N/A';
    const teacher = lesson.teacher ? `${lesson.teacher.firstName || ''} ${lesson.teacher.lastName || ''}`.trim() || lesson.teacher.id || 'N/A' : 'N/A';
    const room = lesson.room?.id || 'N/A';
    const roomType = lesson.teachingUnit?.roomType || 'N/A';
    return `
        <div class=\"lesson\">
            <div class=\"subject\">${subject}</div>
            <div class=\"teacher\">${teacher}</div>
            <div class=\"room\">Room: ${room}</div>
            <div class=\"room-type\">Room type: ${formatRoomType(roomType)}</div>
        </div>
    `;
}

function formatRoomType(roomType) {
    if (!roomType || roomType === 'N/A') return 'N/A';
    // Convert enum values to readable format
    return roomType
        .split('_')
        .map(word => word.charAt(0) + word.slice(1).toLowerCase())
        .join(' ');
}

function resetView() {
    document.getElementById('scoreSection').style.display = 'none';
    document.getElementById('timetableContainer').style.display = 'none';
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('classSelect').innerHTML = '<option value=\"\">Select class...</option>';
    resetTable();
}

function resetTable() {
    document.getElementById('timetableTable').innerHTML = '';
}

function toggleLoading(isLoading) {
    document.getElementById('loading').style.display = isLoading ? 'block' : 'none';
    document.getElementById('fetchBtn').disabled = isLoading;
}

function toggleSoftConstraints() {
    const list = document.getElementById('softConstraintsList');
    const arrow = document.getElementById('softConstraintsArrow');
    if (!list || !arrow) return;
    
    if (list.classList.contains('hidden')) {
        list.classList.remove('hidden');
        arrow.classList.add('up');
    } else {
        list.classList.add('hidden');
        arrow.classList.remove('up');
    }
}

document.getElementById('jobIdInput').addEventListener('keypress', e => { if (e.key === 'Enter') fetchSolution(); });