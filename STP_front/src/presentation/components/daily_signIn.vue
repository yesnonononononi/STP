<template>
    <Teleport to="body">
        <Transition name="fade">
            <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center bg-black/45 backdrop-blur-xs"
                @click.self="handleClose">
                <!-- 弹窗卡片 -->
                <div
                    class="w-110 bg-white rounded-3xl p-6 shadow-2xl border border-slate-100 flex flex-col gap-5 select-none transition-all duration-300 transform scale-100">
                    <!-- 头部 -->
                    <div class="flex justify-between items-center pb-2">
                        <div class="font-semibold text-lg flex items-center gap-2 text-slate-800">
                            <svg class="size-6 text-amber-500" viewBox="0 0 1024 1024" version="1.1"
                                xmlns="http://www.w3.org/2000/svg">
                                <path
                                    d="M861.7 104.6c42.9 0 77.9 34.8 77.9 77.4v700.3c-0.1 42.7-34.7 77.3-77.4 77.4H161.9c-42.7 0-77.4-34.7-77.4-77.4V182c0.1-42.7 34.7-77.3 77.4-77.4h77.9c0 13.2 0.2 24.9 0.2 39.3v77c-0.1 43 34.6 78 77.6 78.2h389.1c43-0.2 77.7-35.2 77.5-78.2v-77-18.7-19.5-1.1h77.5zM697 430.8c-15.4-15-40-14.7-55 0.7L457.7 633l-75.4-84.5c-14.3-16-38.9-17.4-54.9-3.1-16 14.3-17.4 38.9-3.1 54.9l104.1 116.6c7.3 8.2 17.8 12.9 28.8 13 11.1 0 21.5-4.6 28.9-12.6L699.4 484c13.4-15.6 12.4-38.9-2.4-53.2zM657.5 64.3c18.5 0.1 35.5 10 44.7 26l1.8 3.4c3.4 6.9 5.5 14.6 5.5 22.9v51.6c0 29-23.3 52.5-52 52.5H370.9c-28.8 0-52.1-23.5-52.1-52.5v-51.6c0.1-9.3 2.6-18.4 7.3-26.4l1.9-3c9.3-13.8 25-23 42.9-23h286.6z"
                                    fill="currentColor"></path>
                            </svg>
                            <span>每日签到打卡</span>
                        </div>
                        <button @click="handleClose"
                            class="size-8 rounded-full flex items-center justify-center hover:bg-slate-100 text-slate-400 hover:text-slate-600 transition-all duration-200 cursor-pointer">
                            <span class="text-lg font-semibold">✕</span>
                        </button>
                    </div>

                    <!-- 月份 Tab -->
                    <div class="flex gap-2 p-1 bg-slate-100 rounded-2xl">
                        <button v-for="m in recentMonths" :key="m.label" @click="selectMonth(m.year, m.month)"
                            class="flex-1 py-2 text-sm font-semibold rounded-xl transition-all duration-300 cursor-pointer text-center"
                            :class="selectedYear === m.year && selectedMonth === m.month
                                ? 'bg-linear-to-r from-blue-300 to-blue-400 text-white shadow-sm'
                                : 'text-slate-600 hover:bg-slate-200/60'">
                            {{ m.label }}
                        </button>
                    </div>

                    <!-- 星期表头 -->
                    <div class="grid grid-cols-7 gap-2 text-center">
                        <span v-for="w in weekDays" :key="w" class="text-xs font-semibold text-slate-400">
                            {{ w }}
                        </span>
                    </div>

                    <!-- 网格状日历 -->
                    <div class="grid grid-cols-7 gap-2 text-center min-h-55">
                        <div v-for="cell in calendarCells" :key="cell.dateStr"
                            class="size-11 flex flex-col items-center justify-center rounded-full text-sm font-medium transition-all duration-300 relative mx-auto"
                            :class="getCellClass(cell)" @click="handleCellClick(cell)">
                            <span>{{ cell.day }}</span>
                            <!-- 打卡成功的小徽标 -->
                            <div v-if="cell.isChecked && cell.isCurrentMonth"
                                class="absolute -bottom-0.5 right-1 size-3 bg-emerald-500 border border-white rounded-full flex items-center justify-center">
                                <svg class="size-2 text-white" viewBox="0 0 1024 1024" version="1.1"
                                    xmlns="http://www.w3.org/2000/svg">
                                    <path
                                        d="M382.4 832.8L121.6 572c-15.6-15.6-15.6-40.8 0-56.4s40.8-15.6 56.4 0l204.4 204.4L846 250.4c15.6-15.6 40.8-15.6 56.4 0s15.6 40.8 0 56.4L382.4 832.8z"
                                        fill="currentColor"></path>
                                </svg>
                            </div>
                        </div>
                    </div>

                    <!-- 底部操作区域 -->
                    <div class="flex flex-col gap-3 pt-2">
                        <div class="flex justify-between items-center text-xs text-slate-500 px-1">
                            <span>本月已打卡：<strong class="text-emerald-500 text-sm">{{ currentMonthCheckedCount }}</strong>
                                天</span>
                            <span>连续打卡：<strong class="text-amber-500 text-sm">{{ consecutiveDays }}</strong> 天</span>
                        </div>
                        <button @click="doSignIn" :disabled="!isCurrentMonthActive || isTodayChecked"
                            class="w-full py-3 bg-linear-to-r text-white font-bold rounded-2xl shadow-lg transition-all duration-300 flex items-center justify-center gap-2 cursor-pointer active:scale-98"
                            :class="(!isCurrentMonthActive || isTodayChecked)
                                ? 'from-slate-200 to-slate-300 text-slate-400 cursor-not-allowed shadow-none'
                                : 'from-blue-300 to-blue-500 hover:from-blue-600 hover:to-indigo-600 hover:shadow-xl hover:scale-[1.01]'">
                            <span v-if="!isCurrentMonthActive">非本月不可打卡</span>
                            <span v-else-if="isTodayChecked">今日已完成打卡</span>
                            <span v-else>立即打卡</span>
                        </button>
                    </div>
                </div>
            </div>
        </Transition>
    </Teleport>
</template>

<script lang="ts" setup>
import { ref, computed, watch, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import { ToolBoxAPI, type SignInInfoData } from '@/services/toolbox';

const props = defineProps<{
    visible: boolean;
}>();

const emit = defineEmits<{
    (e: 'close'): void;
}>();

const weekDays = ['一', '二', '三', '四', '五', '六', '日'];

// 获取当前日期
const now = new Date();
const currentYear = now.getFullYear();
const currentMonth = now.getMonth(); // 0-11

// 选择的年月，默认为当前月
const selectedYear = ref(currentYear);
const selectedMonth = ref(currentMonth);

// 后端返回的签到打卡数据
const signInInfo = ref<SignInInfoData>({
    checkedDates: [],
    consecutiveDays: 0,
    todayChecked: false,
    monthCheckedCount: 0
});

// 初始化月份 Tab（当前月和前三个月，共4个月）
const recentMonths = computed(() => {
    const months = [];
    for (let i = 3; i >= 0; i--) {
        const d = new Date(currentYear, currentMonth - i, 1);
        months.push({
            year: d.getFullYear(),
            month: d.getMonth(),
            label: `${d.getMonth() + 1}月`
        });
    }
    return months;
});

// 是否为当前本月
const isCurrentMonthActive = computed(() => {
    return selectedYear.value === currentYear && selectedMonth.value === currentMonth;
});

// 选择月份
function selectMonth(year: number, month: number) {
    selectedYear.value = year;
    selectedMonth.value = month;
    loadSignInStatus(month + 1);
}

// 打卡日期列表
const checkedDates = computed(() => signInInfo.value.checkedDates || []);

// 获取今天的 YYYY-MM-DD 字符串
const todayDateStr = computed(() => {
    const y = now.getFullYear();
    const m = String(now.getMonth() + 1).padStart(2, '0');
    const d = String(now.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
});

// 判断今天是否已打卡
const isTodayChecked = computed(() => signInInfo.value.todayChecked);

// 格式化日期为 YYYY-MM-DD
function formatDateStr(date: Date): string {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

// 计算当前月份的已打卡天数
const currentMonthCheckedCount = computed(() => {
    return signInInfo.value.monthCheckedCount || 0;
});

// 连续打卡天数
const consecutiveDays = computed(() => signInInfo.value.consecutiveDays);

// 计算选定月份的日历格子列表
const calendarCells = computed(() => {
    const cells = [];
    const year = selectedYear.value;
    const month = selectedMonth.value;

    const firstDay = new Date(year, month, 1);
    const firstDayOfWeek = firstDay.getDay(); // 0是周日，1-6是周一到周六
    const offset = firstDayOfWeek === 0 ? 6 : firstDayOfWeek - 1;

    const totalDays = new Date(year, month + 1, 0).getDate();
    const prevMonthTotalDays = new Date(year, month, 0).getDate();

    // 1. 填充前一个月的末尾天数
    for (let i = offset - 1; i >= 0; i--) {
        const d = prevMonthTotalDays - i;
        const cellDate = new Date(year, month - 1, d);
        const dateStr = formatDateStr(cellDate);
        cells.push({
            date: cellDate,
            day: d,
            isCurrentMonth: false,
            dateStr,
            isChecked: false,
            isToday: dateStr === todayDateStr.value
        });
    }

    // 2. 填充当前月的天数
    for (let d = 1; d <= totalDays; d++) {
        const cellDate = new Date(year, month, d);
        const dateStr = formatDateStr(cellDate);
        const isChecked = checkedDates.value[d - 1] === 1;

        cells.push({
            date: cellDate,
            day: d,
            isCurrentMonth: true,
            dateStr,
            isChecked: isChecked,
            isToday: dateStr === todayDateStr.value
        });
    }

    // 3. 填充下一个月的开头天数 (总共42个格子)
    const remaining = 42 - cells.length;
    for (let d = 1; d <= remaining; d++) {
        const cellDate = new Date(year, month + 1, d);
        const dateStr = formatDateStr(cellDate);
        cells.push({
            date: cellDate,
            day: d,
            isCurrentMonth: false,
            dateStr,
            isChecked: false,
            isToday: dateStr === todayDateStr.value
        });
    }

    return cells;
});

// 获取各格子的样式类
function getCellClass(cell: any) {
    if (!cell.isCurrentMonth) {
        return 'text-slate-300 pointer-events-none';
    }
    if (cell.isChecked) {
        return 'bg-gradient-to-tr from-emerald-50 to-teal-50 text-slate-800 border border-emerald-100 shadow-2xs font-semibold';
    }
    if (cell.isToday) {
        return 'border-2 border-blue-500 text-blue-600 font-bold shadow-inner cursor-pointer hover:bg-blue-50/50 animate-pulse';
    }
    return 'text-slate-700 hover:bg-slate-100 cursor-pointer';
}

// 处理日期格子的点击（仅限今日打卡）
function handleCellClick(cell: any) {
    if (cell.isCurrentMonth && cell.isToday && !cell.isChecked) {
        doSignIn();
    }
}

// 从后端获取签到打卡状态
async function loadSignInStatus(month?: number) {
    try {
        const queryMonth = month !== undefined ? month : (selectedMonth.value + 1);
        const res = await ToolBoxAPI.getSignInStatus(queryMonth);
        if (res.code === 1 && res.data) {
            signInInfo.value = res.data;
        }
    } catch (e) {
        console.error('获取打卡状态异常:', e);
    }
}

// 立即打卡操作
async function doSignIn() {
    if (isTodayChecked.value) return;
    const res = await ToolBoxAPI.doSignIn();
    await loadSignInStatus();
}


watch(() => props.visible, (newVal) => {
    if (newVal) {
        loadSignInStatus();
    }
});

onMounted(() => {
    loadSignInStatus();
});

function handleClose() {
    emit('close');
}
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}
</style>