const SEMESTER_START = new Date(2026, 7, 31, 12);
const DAYS = ['周一', '周二', '周三', '周四', '周五', '周六', '周日'];
const PERIODS = ['1-2节', '3-4节', '5-6节', '7-8节', '9-11节', '9-10节'];
const COLORS = {
  matrix: '#3b82f6', theory: '#06a6a6', ethics: '#f59e0b', algorithm: '#8b5cf6',
  ml: '#ec4899', random: '#14b8a6', database: '#f97316', ai: '#6366f1'
};

const ORIGINAL_COURSES = [
  { id:'matrix-tu', name:'矩阵理论', day:2, period:'3-4节', startWeek:1, endWeek:13, weekType:'all', teacher:'高中营', location:'二教307', note:'12班 · 48学时', color:COLORS.matrix },
  { id:'matrix-th-a', name:'矩阵理论', day:4, period:'3-4节', startWeek:1, endWeek:4, weekType:'all', teacher:'高中营', location:'二教307', note:'12班 · 48学时', color:COLORS.matrix },
  { id:'matrix-th-b', name:'矩阵理论', day:4, period:'3-4节', startWeek:6, endWeek:12, weekType:'all', teacher:'高中营', location:'二教307', note:'12班 · 48学时', color:COLORS.matrix },
  { id:'modern-we', name:'中国式现代化的理论与实践', day:3, period:'3-4节', startWeek:1, endWeek:10, weekType:'all', teacher:'武传鹏', location:'二教308', note:'13班（沙河）· 36学时', color:COLORS.theory },
  { id:'modern-fr-a', name:'中国式现代化的理论与实践', day:5, period:'3-4节', startWeek:1, endWeek:3, weekType:'all', teacher:'武传鹏', location:'二教308', note:'13班（沙河）· 36学时', color:COLORS.theory },
  { id:'modern-fr-b', name:'中国式现代化的理论与实践', day:5, period:'3-4节', startWeek:6, endWeek:10, weekType:'all', teacher:'武传鹏', location:'二教308', note:'13班（沙河）· 36学时', color:COLORS.theory },
  { id:'ethics-mo', name:'工程伦理与学术道德', day:1, period:'5-6节', startWeek:1, endWeek:4, weekType:'all', teacher:'张安安', location:'二教305', note:'14班（线上线下）· 16学时', color:COLORS.ethics },
  { id:'ethics-we', name:'工程伦理与学术道德', day:3, period:'5-6节', startWeek:1, endWeek:4, weekType:'all', teacher:'张安安', location:'二教305', note:'14班（线上线下）· 16学时', color:COLORS.ethics },
  { id:'algo-tu', name:'算法设计与分析', day:2, period:'7-8节', startWeek:1, endWeek:8, weekType:'all', teacher:'陈佳', location:'二教306', note:'2班 · 32学时', color:COLORS.algorithm },
  { id:'algo-th', name:'算法设计与分析', day:4, period:'5-6节', startWeek:1, endWeek:8, weekType:'all', teacher:'陈佳', location:'二教306', note:'2班 · 32学时', color:COLORS.algorithm },
  { id:'random-tu', name:'随机过程与排队论', day:2, period:'9-11节', startWeek:1, endWeek:10, weekType:'all', teacher:'王庆先', location:'二教308', note:'32学时', color:COLORS.random },
  { id:'ml-mo-a', name:'机器学习理论与算法', day:1, period:'9-11节', startWeek:7, endWeek:11, weekType:'all', teacher:'张云', location:'二教308', note:'1班 · 32学时', color:COLORS.ml },
  { id:'ml-mo-b', name:'机器学习理论与算法', day:1, period:'9-10节', startWeek:12, endWeek:12, weekType:'all', teacher:'张云', location:'二教308', note:'1班 · 32学时', color:COLORS.ml },
  { id:'ml-we', name:'机器学习理论与算法', day:3, period:'9-11节', startWeek:7, endWeek:11, weekType:'all', teacher:'张云', location:'二教308', note:'1班 · 32学时', color:COLORS.ml },
  { id:'ai-th-a', name:'人工智能系统架构与应用编程', day:4, period:'9-11节', startWeek:1, endWeek:5, weekType:'all', teacher:'敬豪豪', location:'软件楼305', note:'32学时', color:COLORS.ai },
  { id:'ai-th-b', name:'人工智能系统架构与应用编程', day:4, period:'9-10节', startWeek:6, endWeek:6, weekType:'all', teacher:'敬豪豪', location:'软件楼305', note:'32学时', color:COLORS.ai },
  { id:'ai-fr', name:'人工智能系统架构与应用编程', day:5, period:'9-11节', startWeek:1, endWeek:5, weekType:'all', teacher:'敬豪豪', location:'软件楼305', note:'32学时', color:COLORS.ai },
  { id:'db-th-a', name:'高级数据库系统技术', day:4, period:'9-11节', startWeek:7, endWeek:11, weekType:'all', teacher:'张凤燕、陈安龙', location:'二教306', note:'32学时', color:COLORS.database },
  { id:'db-th-b', name:'高级数据库系统技术', day:4, period:'9-10节', startWeek:12, endWeek:12, weekType:'all', teacher:'张凤燕、陈安龙', location:'二教306', note:'32学时', color:COLORS.database },
  { id:'db-th-c', name:'高级数据库系统技术', day:4, period:'9-11节', startWeek:13, endWeek:14, weekType:'all', teacher:'张凤燕、陈安龙', location:'二教307', note:'32学时', color:COLORS.database },
  { id:'db-tu-a', name:'高级数据库系统技术', day:2, period:'9-11节', startWeek:13, endWeek:14, weekType:'all', teacher:'张凤燕、陈安龙', location:'二教307', note:'32学时', color:COLORS.database },
  { id:'db-tu-b', name:'高级数据库系统技术', day:2, period:'9-11节', startWeek:15, endWeek:15, weekType:'all', teacher:'张凤燕、陈安龙', location:'二教307', note:'32学时', color:COLORS.database }
];

const SPECIAL_DAYS = {
  '2026-09-19': '运动会', '2026-09-20': '运动会',
  '2026-09-25': '中秋节', '2026-09-26': '中秋节', '2026-09-27': '中秋节',
  '2026-10-01': '国庆节', '2026-10-02': '国庆节', '2026-10-03': '国庆节', '2026-10-04': '国庆节', '2026-10-05': '国庆节', '2026-10-06': '国庆节', '2026-10-07': '国庆节',
  '2027-01-01': '元旦', '2027-01-02': '元旦', '2027-01-03': '元旦'
};

const DEFAULT_TIMES = {
  '1-2节':'08:30—10:05',
  '3-4节':'10:20—11:55',
  '5-6节':'14:30—16:05',
  '7-8节':'16:20—17:55',
  '9-11节':'19:30—21:55',
  '9-10节':'19:30—21:55'
};

let courses = load('courses', []);
let classTimes = load('classTimes', DEFAULT_TIMES);
let reminderEnabled = localStorage.getItem('reminderEnabled') !== 'false';
let reminderMinutes = Number(localStorage.getItem('reminderMinutes') || 15);
let scheduleMode = localStorage.getItem('scheduleMode') || 'day';
let widgetAppearance = load('widgetAppearance', {title:'', textTheme:'light', overlay:35});
let selectedWeek = clamp(getWeekNumber(new Date()), 1, 20);
let selectedDay = getMondayDay(new Date());

function load(key, fallback) {
  try { return JSON.parse(localStorage.getItem(key)) || clone(fallback); }
  catch { return clone(fallback); }
}
function save() { localStorage.setItem('courses', JSON.stringify(courses)); }
function clone(value) { return JSON.parse(JSON.stringify(value)); }
function clamp(value, min, max) { return Math.min(max, Math.max(min, value)); }
function addDays(date, days) { const d = new Date(date); d.setDate(d.getDate() + days); return d; }
function pad(n) { return String(n).padStart(2, '0'); }
function keyOf(date) { return `${date.getFullYear()}-${pad(date.getMonth()+1)}-${pad(date.getDate())}`; }
function getWeekNumber(date) { return Math.floor((new Date(date.getFullYear(), date.getMonth(), date.getDate(), 12) - SEMESTER_START) / 604800000) + 1; }
function getMondayDay(date) { const day = date.getDay(); return day === 0 ? 7 : day; }
function dateFor(week, day) { return addDays(SEMESTER_START, (week - 1) * 7 + day - 1); }
function courseMatches(course, week, day) {
  if (course.day !== day || week < course.startWeek || week > course.endWeek) return false;
  return course.weekType === 'all' || (course.weekType === 'odd' && week % 2 === 1) || (course.weekType === 'even' && week % 2 === 0);
}
function periodOrder(period) { return Number(period.match(/\d+/)?.[0] || 99); }
function coursesFor(week, day) { return courses.filter(c => courseMatches(c, week, day)).sort((a,b) => periodOrder(a.period)-periodOrder(b.period)); }
function escapeHtml(text='') { const div=document.createElement('div'); div.textContent=text; return div.innerHTML; }

function renderCourseList(target, list, emptyText='今天没有课程') {
  const el = document.getElementById(target);
  if (!list.length) {
    el.innerHTML = `<div class="empty-state"><span class="empty-icon">☕</span>${emptyText}</div>`;
    return;
  }
  el.innerHTML = list.map(c => `
    <article class="course-card" data-id="${escapeHtml(c.id)}">
      <span class="color-bar" style="background:${c.color}"></span>
      <div><h3>${escapeHtml(c.name)}</h3>
        <p>${escapeHtml([c.location, c.teacher].filter(Boolean).join(' · ') || '地点与教师未设置')}</p>
        <p>${escapeHtml(c.note || `${c.startWeek}-${c.endWeek}周`)}</p>
      </div>
      <span class="period-pill">${escapeHtml(c.period)}${classTimes[c.period] ? `<br>${escapeHtml(classTimes[c.period])}` : ''}</span>
    </article>`).join('');
  el.querySelectorAll('.course-card').forEach(card => card.addEventListener('click', () => openDialog(card.dataset.id)));
}

function renderHome() {
  const now = new Date();
  const week = clamp(getWeekNumber(now), 1, 20);
  const day = getMondayDay(now);
  const list = getWeekNumber(now) >= 1 && getWeekNumber(now) <= 20 ? coursesFor(week, day) : [];
  document.getElementById('fullDate').textContent = `${now.getFullYear()}年${now.getMonth()+1}月${now.getDate()}日 · ${DAYS[day-1]}`;
  document.getElementById('currentWeekBadge').textContent = getWeekNumber(now) < 1 ? '尚未开学' : getWeekNumber(now) > 20 ? '学期已结束' : `第${week}周`;
  document.getElementById('todayCount').textContent = list.length ? `共 ${list.length} 节安排` : '轻松一下';
  const next = list[0];
  const nextEl = document.getElementById('nextCourseCard');
  if (next) {
    nextEl.className = 'next-card';
    nextEl.innerHTML = `<div class="hint">今日课程提醒 · ${next.period}</div><h3>${escapeHtml(next.name)}</h3><p>${escapeHtml([next.location, next.teacher].filter(Boolean).join(' · '))}</p>`;
  } else {
    nextEl.className = 'next-card empty';
    nextEl.innerHTML = `<div class="hint">今日课程提醒</div><h3>今天没有课程</h3><p>可以安排自习、运动或休息</p>`;
  }
  renderCourseList('todayList', list);
}

function renderSchedule() {
  const todayKey = keyOf(new Date());
  document.getElementById('weekTitle').textContent = `第${selectedWeek}周 · ${formatRange(dateFor(selectedWeek,1), dateFor(selectedWeek,7))}`;
  const strip = document.getElementById('dayStrip');
  strip.innerHTML = DAYS.map((d, i) => {
    const date = dateFor(selectedWeek, i + 1);
    const selected = selectedDay === i + 1 ? 'selected' : '';
    const today = keyOf(date) === todayKey ? 'today' : '';
    return `<button class="day-button ${selected} ${today}" data-day="${i+1}"><span>${d.slice(1)}</span><strong>${date.getDate()}</strong></button>`;
  }).join('');
  strip.querySelectorAll('button').forEach(btn => btn.addEventListener('click', () => { selectedDay = Number(btn.dataset.day); renderSchedule(); }));
  const date = dateFor(selectedWeek, selectedDay);
  const event = SPECIAL_DAYS[keyOf(date)];
  renderCourseList('scheduleList', coursesFor(selectedWeek, selectedDay), event ? `${event} · 当天没有排课` : `${DAYS[selectedDay-1]}没有课程`);
  renderWeekOverview(todayKey);
  document.getElementById('dayScheduleMode').classList.toggle('hidden', scheduleMode !== 'day');
  document.getElementById('weekScheduleMode').classList.toggle('hidden', scheduleMode !== 'week');
  document.getElementById('dayModeButton').classList.toggle('active', scheduleMode === 'day');
  document.getElementById('weekModeButton').classList.toggle('active', scheduleMode === 'week');
}

function renderWeekOverview(todayKey = keyOf(new Date())) {
  const container = document.getElementById('weekOverview');
  container.innerHTML = DAYS.map((dayName, index) => {
    const day = index + 1;
    const date = dateFor(selectedWeek, day);
    const list = coursesFor(selectedWeek, day);
    const chips = list.length ? list.map(course => {
      const start = (classTimes[course.period] || course.period).split(/[—~-]/)[0];
      return `<button class="week-chip" data-id="${escapeHtml(course.id)}" style="border-left:3px solid ${course.color}"><b>${escapeHtml(course.name)}</b><span>${escapeHtml(start)} · ${escapeHtml(course.location || '待定')}</span></button>`;
    }).join('') : '<div class="week-empty">无课</div>';
    return `<section class="week-day-column ${keyOf(date)===todayKey?'today':''}"><div class="week-day-head">${dayName.slice(1)}<strong>${date.getDate()}</strong></div>${chips}</section>`;
  }).join('');
  container.querySelectorAll('.week-chip').forEach(card => card.addEventListener('click', () => openDialog(card.dataset.id)));
}

function formatRange(a,b) { return `${a.getMonth()+1}.${a.getDate()}—${b.getMonth()+1}.${b.getDate()}`; }

function renderCalendar() {
  const nowKey = keyOf(new Date());
  const container = document.getElementById('semesterCalendar');
  container.innerHTML = Array.from({length:20}, (_, wi) => {
    const week = wi + 1;
    const days = Array.from({length:7}, (_, di) => {
      const date = dateFor(week, di + 1);
      const event = week >= 19 ? (week === 19 || week === 20 ? '考试周' : '') : SPECIAL_DAYS[keyOf(date)] || '';
      return `<div class="calendar-day ${di>4?'weekend':''} ${keyOf(date)===nowKey?'current':''}"><strong>${date.getDate()}</strong><span>${date.getMonth()+1}月</span>${event?`<em>${event}</em>`:''}</div>`;
    }).join('');
    return `<div class="calendar-week"><div class="calendar-week-label">第${week}周</div><div class="calendar-week-days">${days}</div></div>`;
  }).join('');
}

function renderSettings() {
  const el = document.getElementById('timeSettings');
  el.innerHTML = PERIODS.map(p => `<div class="time-setting"><label>${p}</label><input data-period="${p}" value="${escapeHtml(classTimes[p] || '')}" placeholder="如 10:20—11:55"></div>`).join('');
  el.querySelectorAll('input').forEach(input => input.addEventListener('change', () => {
    classTimes[input.dataset.period] = input.value.trim();
    localStorage.setItem('classTimes', JSON.stringify(classTimes));
    showToast('上课时间已保存');
    scheduleReminders();
  }));
  const enabled = document.getElementById('reminderEnabled');
  const minutes = document.getElementById('reminderMinutes');
  const status = document.getElementById('reminderStatus');
  enabled.checked = reminderEnabled;
  minutes.value = String(reminderMinutes);
  status.textContent = reminderEnabled ? `将在上课前${reminderMinutes}分钟通知` : '提醒已关闭';
  document.getElementById('widgetTitle').value = widgetAppearance.title || '';
  document.getElementById('widgetTextTheme').value = widgetAppearance.textTheme || 'light';
  document.getElementById('widgetOverlay').value = String(widgetAppearance.overlay ?? 35);
  document.getElementById('widgetOverlayValue').textContent = `${widgetAppearance.overlay ?? 35}%`;
  refreshWidgetBackgroundStatus();
}

function refreshWidgetBackgroundStatus(hasBackground) {
  if (typeof hasBackground !== 'boolean' && window.WidgetBridge?.hasBackground) {
    hasBackground = Boolean(window.WidgetBridge.hasBackground());
  }
  const status = document.getElementById('widgetBackgroundStatus');
  if (status) status.textContent = hasBackground ? '当前使用自定义背景图片' : '当前使用默认蓝色背景';
}

function saveWidgetAppearance() {
  widgetAppearance = {
    title:document.getElementById('widgetTitle').value.trim(),
    textTheme:document.getElementById('widgetTextTheme').value,
    overlay:Number(document.getElementById('widgetOverlay').value)
  };
  localStorage.setItem('widgetAppearance', JSON.stringify(widgetAppearance));
  document.getElementById('widgetOverlayValue').textContent = `${widgetAppearance.overlay}%`;
  if (window.WidgetBridge?.updateAppearance) {
    window.WidgetBridge.updateAppearance(JSON.stringify(widgetAppearance));
    showToast('小组件外观已更新');
  }
}

function colorForName(name) {
  const palette = ['#3b82f6','#06a6a6','#8b5cf6','#ec4899','#f97316','#14b8a6','#6366f1','#f59e0b'];
  let hash = 0;
  for (const ch of name) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
  return palette[hash % palette.length];
}

function buildReminderItems() {
  const now = Date.now();
  const items = [];
  for (let week = 1; week <= 20; week++) {
    for (let day = 1; day <= 7; day++) {
      for (const course of coursesFor(week, day)) {
        const range = classTimes[course.period] || '';
        const start = range.match(/(\d{1,2}):(\d{2})/);
        if (!start) continue;
        const date = dateFor(week, day);
        date.setHours(Number(start[1]), Number(start[2]), 0, 0);
        const triggerAt = date.getTime() - reminderMinutes * 60000;
        if (triggerAt <= now) continue;
        items.push({
          id:`${course.id}-${keyOf(date)}-${reminderMinutes}`,
          name:course.name,
          location:course.location || '地点未设置',
          startTime:`${keyOf(date)} ${start[1].padStart(2,'0')}:${start[2]}`,
          triggerAt
        });
      }
    }
  }
  return items;
}

function scheduleReminders() {
  localStorage.setItem('reminderEnabled', String(reminderEnabled));
  localStorage.setItem('reminderMinutes', String(reminderMinutes));
  const items = buildReminderItems();
  if (window.ReminderBridge?.syncReminders) {
    window.ReminderBridge.syncReminders(JSON.stringify(items), reminderEnabled);
  }
  const status = document.getElementById('reminderStatus');
  if (status) status.textContent = reminderEnabled ? `将在上课前${reminderMinutes}分钟通知 · 已计划${items.length}次` : '提醒已关闭';
  updateDesktopWidget();
}

function buildWidgetSchedule() {
  const items = [];
  for (let week = 1; week <= 20; week++) {
    for (let day = 1; day <= 7; day++) {
      const date = dateFor(week, day);
      for (const course of coursesFor(week, day)) {
        items.push({
          date:keyOf(date),
          name:course.name,
          location:course.location || '地点未设置',
          time:classTimes[course.period] || course.period,
          order:periodOrder(course.period)
        });
      }
    }
  }
  return items;
}

function updateDesktopWidget() {
  if (window.WidgetBridge?.updateSchedule) {
    window.WidgetBridge.updateSchedule(JSON.stringify(buildWidgetSchedule()));
  }
}

function populateFormOptions() {
  document.getElementById('courseDay').innerHTML = DAYS.map((d,i)=>`<option value="${i+1}">${d}</option>`).join('');
  document.getElementById('coursePeriod').innerHTML = PERIODS.map(p=>`<option>${p}</option>`).join('');
}

function openDialog(id='') {
  const c = courses.find(item => item.id === id);
  document.getElementById('dialogTitle').textContent = c ? '编辑课程' : '添加课程';
  document.getElementById('courseId').value = c?.id || '';
  document.getElementById('courseName').value = c?.name || '';
  document.getElementById('courseDay').value = c?.day || selectedDay;
  document.getElementById('coursePeriod').value = c?.period || '1-2节';
  document.getElementById('startWeek').value = c?.startWeek || selectedWeek;
  document.getElementById('endWeek').value = c?.endWeek || selectedWeek;
  document.getElementById('weekType').value = c?.weekType || 'all';
  document.getElementById('courseColor').value = c?.color || '#3b82f6';
  document.getElementById('teacher').value = c?.teacher || '';
  document.getElementById('location').value = c?.location || '';
  document.getElementById('deleteCourse').classList.toggle('hidden', !c);
  document.getElementById('courseDialog').showModal();
}

document.querySelectorAll('.nav-item').forEach(button => button.addEventListener('click', () => {
  document.querySelectorAll('.nav-item,.view').forEach(el => el.classList.remove('active'));
  button.classList.add('active');
  document.getElementById(button.dataset.view).classList.add('active');
  document.getElementById('pageTitle').textContent = button.dataset.title;
  if (button.dataset.view === 'scheduleView') renderSchedule();
  if (button.dataset.view === 'calendarView') renderCalendar();
  if (button.dataset.view === 'settingsView') renderSettings();
}));

document.getElementById('prevWeek').addEventListener('click', () => { selectedWeek = clamp(selectedWeek - 1, 1, 20); renderSchedule(); });
document.getElementById('nextWeek').addEventListener('click', () => { selectedWeek = clamp(selectedWeek + 1, 1, 20); renderSchedule(); });
document.getElementById('weekTitle').addEventListener('click', () => { selectedWeek = clamp(getWeekNumber(new Date()),1,20); selectedDay=getMondayDay(new Date()); renderSchedule(); });
document.getElementById('dayModeButton').addEventListener('click', () => {
  scheduleMode = 'day'; localStorage.setItem('scheduleMode', scheduleMode); renderSchedule();
});
document.getElementById('weekModeButton').addEventListener('click', () => {
  scheduleMode = 'week'; localStorage.setItem('scheduleMode', scheduleMode); renderSchedule();
});
document.getElementById('addButton').addEventListener('click', () => openDialog());
document.getElementById('cancelDialog').addEventListener('click', () => document.getElementById('courseDialog').close());

document.getElementById('courseForm').addEventListener('submit', event => {
  event.preventDefault();
  const id = document.getElementById('courseId').value || `custom-${Date.now()}`;
  const start = Number(document.getElementById('startWeek').value);
  const end = Number(document.getElementById('endWeek').value);
  if (start > end) { showToast('起始周不能晚于结束周'); return; }
  const existing = courses.find(item => item.id === id);
  const course = {
    id, name:document.getElementById('courseName').value.trim(), day:Number(document.getElementById('courseDay').value),
    period:document.getElementById('coursePeriod').value, startWeek:start, endWeek:end,
    weekType:document.getElementById('weekType').value, color:document.getElementById('courseColor').value,
    teacher:document.getElementById('teacher').value.trim(), location:document.getElementById('location').value.trim(),
    note: existing?.note || ''
  };
  if (existing) Object.assign(existing, course); else courses.push(course);
  save(); document.getElementById('courseDialog').close(); renderAll(); scheduleReminders(); showToast('课程已保存');
});

document.getElementById('deleteCourse').addEventListener('click', () => {
  const id = document.getElementById('courseId').value;
  if (!id || !confirm('确定删除这条课程安排吗？')) return;
  courses = courses.filter(c => c.id !== id); save(); document.getElementById('courseDialog').close(); renderAll(); scheduleReminders(); showToast('课程已删除');
});

document.getElementById('exportButton').addEventListener('click', () => {
  const payload = JSON.stringify({
    schemaVersion:1,
    appName:'研一课程表',
    exportedAt:new Date().toISOString(),
    courses,
    classTimes
  }, null, 2);
  if (window.ScheduleFileBridge?.exportSchedule) {
    window.ScheduleFileBridge.exportSchedule(payload);
    return;
  }
  const blob = new Blob([payload], {type:'application/json'});
  const a = document.createElement('a'); a.href=URL.createObjectURL(blob); a.download='研一课程表数据.json'; a.click(); URL.revokeObjectURL(a.href);
});

function applyImportedSchedule(raw) {
  try {
    const data = typeof raw === 'string' ? JSON.parse(raw) : raw;
    if (!Array.isArray(data.courses) || !data.courses.length) throw new Error('没有课程');
    const valid = data.courses.every(course =>
      course && typeof course.name === 'string' && course.name.trim() &&
      Number(course.day) >= 1 && Number(course.day) <= 7 &&
      typeof course.period === 'string' &&
      Number(course.startWeek) >= 1 && Number(course.endWeek) >= Number(course.startWeek)
    );
    if (!valid) throw new Error('课程字段不完整');
    if (!confirm(`文件中包含 ${data.courses.length} 条课程安排。导入后将替换当前课程表，确定继续吗？`)) return false;
    courses = data.courses.map(course => ({
      ...course,
      day:Number(course.day),
      startWeek:Number(course.startWeek),
      endWeek:Number(course.endWeek),
      weekType:['all','odd','even'].includes(course.weekType) ? course.weekType : 'all',
      color:course.color || colorForName(course.name)
    }));
    classTimes = data.classTimes && typeof data.classTimes === 'object' ? {...DEFAULT_TIMES, ...data.classTimes} : clone(DEFAULT_TIMES);
    save();
    localStorage.setItem('classTimes', JSON.stringify(classTimes));
    renderAll();
    renderSettings();
    scheduleReminders();
    showToast(`导入成功：${courses.length} 条课程`);
    return true;
  } catch (error) {
    showToast('文件格式不正确，未修改当前课表');
    return false;
  }
}

window.handleImportedSchedule = applyImportedSchedule;

document.getElementById('importButton').addEventListener('click', () => {
  if (window.ScheduleFileBridge?.importSchedule) window.ScheduleFileBridge.importSchedule();
  else document.getElementById('importInput').click();
});
document.getElementById('importInput').addEventListener('change', async event => {
  if (event.target.files?.[0]) applyImportedSchedule(await event.target.files[0].text());
  event.target.value='';
});
document.getElementById('resetButton').addEventListener('click', () => {
  if (!confirm('这会删除当前全部课程，确定清空吗？')) return;
  courses=[]; save(); renderAll(); scheduleReminders(); showToast('课程已全部清空');
});
document.getElementById('reminderEnabled').addEventListener('change', event => {
  reminderEnabled = event.target.checked;
  renderSettings();
  scheduleReminders();
});
document.getElementById('reminderMinutes').addEventListener('change', event => {
  reminderMinutes = Number(event.target.value);
  renderSettings();
  scheduleReminders();
});
document.getElementById('widgetTitle').addEventListener('change', saveWidgetAppearance);
document.getElementById('widgetTextTheme').addEventListener('change', saveWidgetAppearance);
document.getElementById('widgetOverlay').addEventListener('input', event => {
  document.getElementById('widgetOverlayValue').textContent = `${event.target.value}%`;
});
document.getElementById('widgetOverlay').addEventListener('change', saveWidgetAppearance);
document.getElementById('widgetBackgroundButton').addEventListener('click', () => {
  if (window.WidgetBridge?.pickBackground) window.WidgetBridge.pickBackground();
  else showToast('桌面小组件背景仅在安卓 App 中可用');
});
document.getElementById('clearWidgetBackgroundButton').addEventListener('click', () => {
  if (window.WidgetBridge?.clearBackground) {
    window.WidgetBridge.clearBackground();
    refreshWidgetBackgroundStatus(false);
    showToast('已恢复默认小组件背景');
  }
});
window.onWidgetBackgroundChanged = hasBackground => {
  refreshWidgetBackgroundStatus(Boolean(hasBackground));
  if (hasBackground) showToast('小组件背景已更新');
};

let toastTimer;
function showToast(message) { const t=document.getElementById('toast'); t.textContent=message; t.classList.add('show'); clearTimeout(toastTimer); toastTimer=setTimeout(()=>t.classList.remove('show'),1800); }
function renderAll() { renderHome(); renderSchedule(); renderCalendar(); }

populateFormOptions();
renderAll();
renderSettings();
scheduleReminders();
if ('serviceWorker' in navigator && location.protocol.startsWith('http')) navigator.serviceWorker.register('./sw.js');
