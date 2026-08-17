<template>

  <div class="avatar relative hover:z-50" @mouseenter="loadDetail">
    <div class="group cursor-pointer" :class="size === 'small' ? 'w-12 h-12' : 'w-14 h-14'">
      <img class="rounded-full bg-gray-400" :class="size === 'small' ? 'w-10 h-10' : 'w-12 h-12'"
        :src="displayUser?.avatar" alt=""
        @click="router.push({ name: 'userProfile', params: { 'id': displayUser?.id } })" />

      <div v-loading="loading"
        class="publisher-introduce bg-linear-to-tl max-h-64 bg-white rounded-md shadow-md opacity-0 pointer-events-none hover:opacity-100 hover:pointer-events-auto group-hover:pointer-events-auto group-hover:opacity-100 transition-opacity duration-400 absolute w-96 h-auto z-10"
        :class="size === 'small' ? 'top-10' : 'top-14'">
        <div class="flex flex-col h-auto items-center gap-2">
          <div class="introduce w-full flex flex-col">
            <div class="h-14 w-full bg-linear-to-br from-blue-400 via-blue-200 to-blue-300"></div>
            <div class="flex w-full">
              <div class="-translate-y-1/3 w-20 h-16 p-2">
                <img class="size-16 m-auto  rounded-full bg-gray-200" :src="displayUser?.avatar" alt="" />
              </div>
              <div class="flex-2 detail text-stone-600">
                <div class="grid grid-cols-3 gap-4">
                  <div class="fans flex flex-col p-2 items-center">
                    <span class="text-xs mb-2">粉丝</span>
                    <div class="text-md">{{ formatNum(Number(displayUser?.fans) || 0) }}</div>
                  </div>
                  <div class="topic flex flex-col p-2 items-center">
                    <span class="text-xs mb-2">帖子</span>
                    <div class="text-md">{{ formatNum(Number(displayUser?.topic) || 0) }}</div>
                  </div>
                  <div class="liked flex flex-col p-2 items-center">
                    <span class="text-xs mb-2">获赞</span>
                    <div class="text-md">{{ formatNum(Number(displayUser?.liked) || 0) }}</div>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="mid w-full pl-5 flex gap-2">
            <span class="truncate max-w-48 block font-semibold text-black">{{ displayUser?.nick }}</span>
            <VipTag :user="displayUser" />
          </div>
          <div class="habbit ml-8 w-full h-6">
            <div class="w-auto h-6 text-xs flex items-center gap-3 justify-start">
              <span class="bg-gray-200 px-1.5 py-0.5 rounded-sm text-stone-600">{{ displayUser?.ip }}</span>
              <span class="bg-gray-200 px-1.5 py-0.5 rounded-sm text-stone-600" v-if="displayUser?.age">
                {{ displayUser.age }}
              </span>
            </div>
          </div>
          <div class="operation w-full pl-5 h-12 px-8">
            <div class="flex items-center justify-between gap-2" v-if="!me || displayUser?.id != me.id.toString()">
              <button @click="handleFollow" class="flex-1 text-center shadow-md hover:scale-[1.05] cursor-pointer"
                :class="displayUser?.followed
                  ? 'bg-gray-200 text-gray-500 border border-gray-300'
                  : 'bg-linear-to-l from-blue-300 to-blue-200 text-white'">
                {{ displayUser?.followed ? '已关注' : '关注' }}
              </button>
              <button @click="handleMessage"
                class="flex-1 text-center bg-linear-to-l from-blue-300 to-blue-200 shadow-md hover:scale-[1.05] cursor-pointer text-white">私信</button>
            </div>
          </div>
        </div>
      </div>

    </div>
  </div>

  <!-- 页面中心私信输入弹窗 -->
  <Teleport to="body">
    <div v-if="showMessageInput && displayUser"
      class="fixed inset-0 z-1000 flex items-center justify-center bg-black/40 backdrop-blur-xs">
      <PrivateMessageInput :user="messageTargetUser" :visible="showMessageInput" v-model="messageContent"
        @close="showMessageInput = false" @success="handleMessageSuccess" />
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import router from '@/router';
import {formatNum} from '@/utils/page';
import type {UserProfileData, UserSimpleData} from '@/services/user';
import {UserAPI} from '@/services/user';
import {useUserInfoStore} from '@/stores/userInfo';
import {useAuthStore} from '@/views/auth/store';
import {log} from '@/utils/log';
import {computed, ref} from 'vue';
import PrivateMessageInput from './private_message_input.vue';
import VipTag from './VipTag.vue';

const props = withDefaults(
  defineProps<{
    publisher?: UserSimpleData | null;
    size?: 'small' | 'default';
  }>(),
  {
    size: 'default'
  }
);

const me = useUserInfoStore().user;

const detailData = ref<UserProfileData | null>(null);
const loading = ref(false);

async function loadDetail() {
  if (detailData.value || loading.value) return;
  const id = Number(props.publisher?.id);
  if (!id) return;
  loading.value = true;
  try {
    const res = await UserAPI.getUserById(id);
    if (res.code === 1 && res.data) {
      detailData.value = res.data;
    }
  } catch (e) {
    console.error('获取用户详细信息失败:', e);
  } finally {
    loading.value = false;
  }
}

const displayUser = computed(() => {
  return {
    ...props.publisher,
    ...detailData.value,
    followed: detailData.value ? detailData.value.followed : props.publisher?.followed
  };
});

async function handleFollow() {
  if (!me?.id) {
    log.error("请先登录");
    const authStore = useAuthStore()
    authStore.showLoginDialog();
    return;
  }
  const userId = displayUser.value.id;
  if (!userId) return;
  if (String(userId) === String(me.id)) {
    log.error("不能关注自己");
    return;
  }
  try {
    const followerId = Number(me.id);
    const followeeId = Number(userId);
    const res = await UserAPI.toggleFollow(followerId, followeeId, 'hover_card');
    if (res.code === 1) {
      const targetFollowed = !displayUser.value.followed;
      if (detailData.value) {
        detailData.value.followed = targetFollowed;
        const currentFans = Number(detailData.value.fans || 0);
        detailData.value.fans = String(targetFollowed ? currentFans + 1 : Math.max(0, currentFans - 1));
      }
      if (props.publisher) {
        props.publisher.followed = targetFollowed;
        const currentFans = Number(props.publisher.fans || 0);
        props.publisher.fans = targetFollowed ? currentFans + 1 : Math.max(0, currentFans - 1);
      }
    } else {
      log.error(res.errMsg || "操作失败");
    }
  } catch (e: any) {
    console.error('关注操作异常:', e);
    log.error(e.message || "操作失败");
  }
}

const showMessageInput = ref(false);
const messageContent = ref('');

const messageTargetUser = computed<UserSimpleData>(() => {
  const u = displayUser.value;
  if (!u) {
    return { id: '', nick: '', avatar: '', memberLevel: '', memberLevelName: '', vipType: '', vipConfigIcon: '', ip: '' };
  }
  return {
    id: String(u.id || ''),
    nick: u.nick || '',
    avatar: u.avatar || '',
    memberLevel: u.memberLevel || '',
    memberLevelName: u.memberLevelName || '',
    vipType: u.vipType || '',
    vipConfigIcon: u.vipConfigIcon || '',
    ip: u.ip || '',
    introduction: u.introduction || '',
    fans: u.fans || 0,
    liked: u.liked || 0,
    topic: u.topic || 0,
    gender: u.gender !== undefined ? String(u.gender) : undefined,
    followed: u.followed
  };
});

function handleMessage() {
  const authStore = useAuthStore()
  if (!authStore.token) {
    log.warning("请先登录后操作");
    authStore.showLoginDialog();
    return;
  }
  const userId = displayUser.value.id;
  if (userId && String(userId) === String(me?.id)) {
    log.error("不能给自己发送私信");
    return;
  }
  showMessageInput.value = true;
}

function handleMessageSuccess() {
  router.push({ name: 'message' });
}
</script>
