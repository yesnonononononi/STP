<template>
    <div class=" md:w-2/3 w-full m-auto  flex gap-2 h-[calc(100vh-120px)] mt-1">
        <div class="tab w-2/5 md:w-1/6 bg-white rounded-xl flex flex-col gap-6 items-center p-2">
            <div class="flex items-center gap-2 mt-4">
                <span class="text-xl md:text-2xl shrink-0 font-semibold">消息中心</span>
                <div class="relative flex flex-col items-center" @click="readAll()">
                    <Tooltip placement="bottom" content="全部已读">
                        <template v-slot>
                            <svg t="1782114365061" class="icon size-4 cursor-pointer hover:fill-blue-400"
                                viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5185">
                                <path
                                    d="M975.36 911.36l-43.52-126.08V376.32c0-42.24-34.56-76.16-76.16-76.16h-209.92V156.8C645.76 83.2 585.6 23.04 512 23.04S378.24 83.2 378.24 156.8v143.36H168.32c-42.24 0-76.16 34.56-76.16 76.16v408.96l-43.52 126.08c-7.04 20.48-3.84 43.52 8.96 60.8 12.8 17.92 33.28 28.16 55.04 28.16h800c21.76 0 42.24-10.24 55.04-28.16 11.52-16.64 14.72-40.32 7.68-60.8zM429.44 156.8c0-45.44 37.12-82.56 82.56-82.56 45.44 0 82.56 37.12 82.56 82.56v143.36H429.44V156.8zM143.36 376.32c0-14.08 11.52-24.96 24.96-24.96H855.68c14.08 0 24.96 11.52 24.96 24.96v80.64H143.36V376.32z m781.44 566.4c-1.28 1.92-5.76 6.4-12.8 6.4h-100.48c3.2-12.16 2.56-25.6-1.92-37.76l-43.52-126.08v-110.72h-51.2v119.68l46.08 134.4c2.56 7.04-0.64 12.8-1.92 14.72-1.28 1.92-5.76 6.4-12.8 6.4H537.6v-275.2h-51.2v275.2H278.4c-7.68 0-11.52-4.48-12.8-6.4-1.28-1.92-4.48-7.68-1.92-14.72l46.08-134.4v-119.68h-51.2v110.72l-43.52 126.08c-4.48 12.16-4.48 25.6-1.92 37.76H112c-7.68 0-11.52-4.48-12.8-6.4s-4.48-7.68-1.92-14.72l46.08-134.4V508.16h737.92v286.08l46.08 134.4c1.92 6.4-0.64 12.8-2.56 14.08z"
                                    p-id="5186"></path>
                            </svg>
                        </template>
                    </Tooltip>
                </div>
            </div>
            <div class="flex items-center gap-2 hover:bg-gray-100 cursor-pointer justify-center w-full p-2 rounded-lg transition-colors duration-150"
                @click="curTab = item.id" v-for="(item, index) in tabList" :key="index"
                :class="curTab === item.id ? 'text-blue-500' : 'text-gray-400 '">
                <div v-html="item.icon" class="size-4"></div>
                <div class="text-md shrink-0 relative">
                    <span>{{ item.name }}</span>
                    <div v-if="item.unReadCount > 0"
                        class="absolute -right-0.5 -top-0.5 rounded-full size-2 bg-red-600"></div>
                </div>
            </div>

        </div>
        <div class="body w-5/6 bg-linear-to-br from-blue-200 to-blue-100/50 rounded-xl h-full">
            <SysMessage v-if="curTab == tabList[0]?.id" v-model="noticeList" :loading="sysLoading"></SysMessage>
            <Private_message v-else-if="curTab === tabList[1]?.id" v-model="sessionList"></Private_message>
        </div>
    </div>
</template>
<script setup lang="ts">
import Tooltip from '@/presentation/components/Tooltip.vue';
import SysMessage from './sysMessage.vue';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import Private_message from './private_message.vue';
import { MessageAPI, SessionAPI, type SessionVO, type sysNotice } from '@/services/message/message.ts';
import { useUserInfoStore } from '@/stores/userInfo.ts';
const tabList = reactive([
    {
        id: 1,
        name: '系统通知',
        icon: '<svg t="1782115068360" class="icon size-4" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5763">   <path d="M861.575529 783.058824a30.117647 30.117647 0 0 1-30.117647-30.117648V378.096941C831.457882 202.541176 689.152 60.235294 513.626353 60.235294 338.070588 60.235294 195.764706 202.541176 195.764706 378.096941V752.941176a30.117647 30.117647 0 0 1-30.117647 30.117648H105.411765v60.235294h813.17647v-60.235294h-57.012706zM918.588235 722.823529a60.235294 60.235294 0 0 1 60.235294 60.235295v60.235294a60.235294 60.235294 0 0 1-60.235294 60.235294H105.411765a60.235294 60.235294 0 0 1-60.235294-60.235294v-60.235294a60.235294 60.235294 0 0 1 60.235294-60.235295h30.117647V378.096941C135.529412 169.261176 304.790588 0 513.626353 0c208.805647 0 378.066824 169.261176 378.066823 378.096941V722.823529H918.588235z m-481.882353 210.82353h180.705883a90.352941 90.352941 0 0 1-180.705883 0z" fill="#7F7F7F" p-id="5764"></path></svg>',
        unReadCount: 0
    },
    {
        id: 2,
        name: '私信消息',
        icon: '<svg t="1782115860701" class="icon size-5" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" p-id="5012"><path d="M874.666667 277.333333v469.333334H149.333333V277.333333h725.333334m14.72-64H134.613333A49.066667 49.066667 0 0 0 85.333333 262.613333v498.773334A49.066667 49.066667 0 0 0 134.613333 810.666667h754.773334A49.066667 49.066667 0 0 0 938.666667 761.386667V262.613333A49.066667 49.066667 0 0 0 889.386667 213.333333z" fill="#7F7F7F" p-id="5013"></path><path d="M786.986667 400.426667a32 32 0 0 0-42.666667-11.52L512 522.24l-231.253333-133.333333a32 32 0 1 0-32 55.253333l240.213333 138.666667a31.36 31.36 0 0 0 23.04 3.413333 31.36 31.36 0 0 0 23.04-3.413333l240.213333-138.666667a32 32 0 0 0 11.733334-43.733333z" fill="#7F7F7F" p-id="5014"></path></svg>',
        unReadCount: computed(() => {
            let count = 0;
            sessionList.value.forEach(session => {
                count += session.unreadCount;
            })
            return count;
        })
    }
]);
const curTab = ref(tabList[0] && tabList[0].id)
const me = useUserInfoStore().user;
const sysLoading = ref(false)
const sessionList = ref<SessionVO[]>([])
const noticeList = ref<sysNotice[]>([])
onMounted(async () => {
    await loadSysMsgList();
    await loadSessionList();
})
watch(() => curTab.value, async () => {
    if (curTab.value === tabList[1]?.id) {
        await loadSessionList()
    } else if (curTab.value === tabList[0]?.id) {
        await loadSysMsgList()
    }
})
async function loadSessionList() {
    const sessions = (await SessionAPI.querySessionList()).data || [];
    sessionList.value = sessions;
    let uc = 0;
    sessions.forEach(session => {
        uc += session.unreadCount;
    })
    tabList[1]!.unReadCount = uc;
}
function read(id: number) {
    if (tabList[id]) {
        tabList[id].unReadCount = 0;
    }
}
async function loadSysMsgList() {
    try {
        if (sysLoading.value) return;
        sysLoading.value = true;
        noticeList.value = (await MessageAPI.querySysMessage()).data;
    } finally {
        sysLoading.value = false;
    }
}
async function readAll() {
    await MessageAPI.readAll()
    sessionList.value.forEach(session => {
        session.unreadCount = 0;
    })
}

</script>