<template>
    <div class="relative z-12 w-full min-h-screen bg-linear-to-tl from-blue-100 via-blue-300 to-blue-200 py-10">

        <!-- 账号设置和更换背景按钮 -->
        <div class="w-2/3   h-12 m-auto flex text-white font-semibold items-center justify-end gap-4 px-4 select-none">
            <div class="cursor-pointer hover:underline flex items-center gap-1"
                @click="router.push({ name: 'userSettings' })">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z">
                    </path>
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                </svg>
                <span>账号设置</span>
            </div>
            <div class="cursor-pointer hover:underline flex items-center gap-1">
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                        d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z">
                    </path>
                </svg>
                <span>更换主页背景</span>
            </div>
        </div>
        <div class="w-2/3 min-h-200  m-auto bg-white/70 ">
            <!-- 个人主页卡片 -->
            <div class="rounded-2xl shadow-md mt-16 bg-white relative overflow-visible">
                <div class="top">
                    <!-- 凸出头像组件 -->
                    <div
                        class="avatar-wrapper absolute left-8 top-0 w-28 h-28 cursor-pointer -translate-y-1/2 rounded-full bg-white p-1.5 flex items-center justify-center z-10 shadow-[0_-3px_10px_rgba(0,0,0,0.04)]">
                        <img class="w-full h-full rounded-full object-cover"
                            :src="userInfo?.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'"
                            alt="Avatar">
                    </div>

                    <!-- 卡片信息区 -->
                    <div class="avatar-introduce   flex  justify-between px-8 pt-4">
                        <div class="left ml-32 h-12 flex items-center gap-1 select-none">
                            <span class="font-bold text-gray-800 text-xl">{{ userInfo?.nick || '' }}</span>
                            <div class="bg-blue-200 h-5 w-20 rounded-2xl flex items-center " v-if="userInfo">
                                <img class="w-5 h-5" v-if="userInfo?.vipConfigIcon" :src="userInfo?.vipConfigIcon"
                                    alt="">
                                <span class="text-xs shrink-0 text-blue-500 p-1">{{ userInfo?.memberLevel || 'Lv.1'
                                }}</span>
                            </div>
                        </div>
                        <div class="right w-1/3 h-full flex items-center ">
                            <div
                                class="liked flex-1  border-r border-gray-300 flex flex-col items-center justify-center gap-1">
                                <div>获赞</div>
                                <div>{{ formatNum(Number(userInfo?.liked || 0)) }}</div>
                            </div>
                            <div
                                class="fans flex-1 border-r border-gray-300 flex flex-col items-center justify-center gap-1">
                                <div>粉丝</div>
                                <div>{{ formatNum(Number(userInfo?.fans || 0)) }}</div>
                            </div>
                            <div class="topic flex-1 flex flex-col items-center justify-center gap-1">
                                <div>话题</div>
                                <div>{{ formatNum(Number(userInfo?.topic || 0)) }}</div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="tag flex items-center justify-between gap-4  mx-4 p-4">
                    <div class=" w-auto text-sm h-6 rounded-xl text-stone-600 flex items-center justify-center p-2 bg-gray-200/80"
                        v-for="(item, index) in userTag" :key="index">
                        IP归属地: {{ item.name }}
                    </div>

                </div>
                <div class="introduce w-full flex justify-between items-center">
                    <div class="left text-gray-500 mx-4 p-4 w-1/2 select-none">
                        {{ userInfo?.introduction || (isme ? '添加简介,让大家认识你' : '该用户暂无简介') }}
                    </div>
                    <div class="right w-1/2 text-right flex justify-center items-center gap-12 mb-6" v-if="!isme">
                        <button
                            class="bg-linear-to-bl rounded-md shadow-md from-blue-100 to-blue-200 w-24 h-10 cursor-pointer hover:scale-[1.05]">关注</button>
                        <button
                            class="bg-linear-to-bl rounded-md shadow-md from-blue-100 to-blue-200 w-24 h-10 cursor-pointer hover:scale-[1.05]">私信</button>
                    </div>
                    <div v-else class="flex items-center gap-4 px-4">
                        <span>个人信息完善度</span>
                        <span class="text-blue-300">{{ 70 }}%</span>
                        <span class="cursor-pointer hover:text-blue-600 text-blue-500"
                            @click="edit_profile = true">编辑资料></span>
                    </div>
                </div>

            </div>

            <!-- 足迹 组件 -->
            <div class="h-150 bg-gay-100 rounded-md flex">
                <div class="left w-[75%] p-2 pb-0 h-full ">
                    <div
                        class=" w-full h-[10%]  rounded-md bg-white p-3  flex gap-18 text-2xl text-gray-500 font-semibold">
                        <span
                            class="hover:text-blue-200 transition-all duration-300 cursor-pointer flex items-center justify-center"
                            :class="curTab == PostStatus.NORMAL ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-200 to-blue-300' : ''"
                            @click="curTab = PostStatus.NORMAL">发布</span>
                        <div class="flex items-center gap-18 h-full" v-if="isme">
                            <span class="hover:text-blue-200 cursor-pointer transition-all duration-300"
                                @click="curTab = PostStatus.DRAFT"
                                :class="curTab == PostStatus.DRAFT ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-200 to-blue-300' : ''">草稿</span>
                            <span class="hover:text-blue-200 cursor-pointer transition-all duration-300"
                                @click="curTab = PostStatus.DELETED"
                                :class="curTab == PostStatus.DELETED ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-200 to-blue-300' : ''">已删除</span>
                            <span class="hover:text-blue-200 cursor-pointer transition-all duration-300"
                                @click="curTab = PostStatus.BLOCKED"
                                :class="curTab == PostStatus.BLOCKED ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-200 to-blue-300' : ''">禁用</span>
                            <span class="hover:text-blue-200 cursor-pointer transition-all duration-300"
                                @click="curTab = PostStatus.REPORTED"
                                :class="curTab == PostStatus.REPORTED ? 'bg-clip-text text-transparent bg-linear-to-r from-blue-200 to-blue-300' : ''">被举报</span>

                        </div>
                    </div>
                    <div class="w-full mt-2 h-[90%]">
                        <UserProfileTabPublish ref="publishTabRef" :status="curTab" @delete="handleDelete" />
                    </div>
                </div>
                <div class="right w-[25%] bg-white h-full mt-2"></div>
            </div>
        </div>
        <div class="absolute inset-0 z-11 bg-gray-400/50 flex flex-col items-center justify-center"
            v-show="pendingDeleteId">
            <div class="relative w-1/3 h-1/3 rounded-md flex flex-col items-center justify-center bg-white">
                <div class="absolute w-full top-2 right-8 flex justify-end items-center h-12 cursor-pointer"
                    @click="pendingDeleteId = null">X</div>
                <span class="text-2xl font-bold">你确定要删除此内容吗?</span>
                <div class="p-12 text-gray-500 flex   justify-between w-full gap-4">
                    <span
                        class="w-1/2 text-center font-semibold bg-blue-500 shadow-md rounded-md cursor-pointer text-white py-1 hover:bg-blue-700"
                        @click="handleConfirmDelete">确认</span>
                    <span
                        class="w-1/2 text-center font-semibold hover:bg-gray-200   shadow-md rounded-md cursor-pointer py-1"
                        @click="pendingDeleteId = null">取消</span>
                </div>
            </div>
        </div>
        <div class="profile-edit fixed inset-0 z-12 transition-all duration-500  pointer-event-none "
            :class="edit_profile ? 'translate-x-0' : 'translate-x-full'">
            <div class="preview flex justify-end w-full h-full">
                <div class="w-2/3 h-full " @click="edit_profile = false"></div>
                <div class="body  bg-white w-1/3 h-full flex flex-col gap-4 p-8">
                    <span>基本资料 完善度
                        <span class="text-blue-300">{{ 70 }}%</span>
                    </span>
                    <span class=" flex justify-between w-full">
                        <span class="text-xl">基本信息</span>
                        <span
                            class="px-2 py-1 bg-linear-to-l rounded-lg from-blue-200 via-blue-300 to-blue-400 hover:scale-105 cursor-pointer"
                            @click="is_editing = true">编辑
                        </span>
                    </span>
                    <div class="flex flex-col gap-4 p-4">
                        <div class="flex items-center  gap-4">
                            <span>我的头像</span>
                            <span>
                                <div class="w-12 h-12 rounded-full overflow-hidden"
                                    :class="is_editing ? 'cursor-pointer relative group' : ''"
                                    @click="is_editing && triggerAvatarSelect()">
                                    <el-image
                                        :src="is_editing ? (previewAvatarUrl || editProfileForm.avatar) : userInfo?.avatar"
                                        class="w-12 h-12 rounded-full" alt="">
                                        <template #error>
                                            <div
                                                class="w-12 h-12 rounded-full flex items-center justify-center bg-gray-100 text-gray-400">
                                                <el-icon>
                                                    <Plus />
                                                </el-icon>
                                            </div>
                                        </template>
                                    </el-image>
                                    <div v-if="is_editing"
                                        class="absolute inset-0 bg-black/30 flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-opacity rounded-full">
                                        <el-icon class="text-base">
                                            <Plus />
                                        </el-icon>
                                    </div>
                                </div>
                                <input v-if="is_editing" type="file" ref="avatarInput" accept="image/*" class="hidden"
                                    @change="handleAvatarChange" />
                            </span>
                        </div>
                    </div>
                    <div class="flex flex-col gap-4 p-4">
                        <div class="flex items-center  gap-4">
                            <span class="shrink-0">我的昵称</span>
                            <span v-if="!is_editing">{{ currentUser?.nick }}</span>
                            <el-input v-else v-model="editProfileForm.nick" v-if="is_editing"
                                :placeholder="userInfo?.nick || '请输入昵称'" />
                        </div>
                    </div>
                    <div class="flex flex-col gap-4 p-4">
                        <div class="flex items-center  gap-4">
                            <span class="shrink-0">我的性别</span>
                            <span v-if="!is_editing">{{ currentUser?.gender == 1 ? '男' : '女' }}</span>
                            <div v-else class="flex px-4 gap-4 items-center w-1/2">
                                <button
                                    class="w-1/2  text-black px-4 py-2 rounded-md cursor-pointer transition-colors duraton-200 hover:bg-gray-400"
                                    :class="editProfileForm.gender == 0 ? 'bg-gray-200' : 'bg-blue-300'"
                                    @click="editProfileForm.gender = 1">男</button>
                                <button
                                    class="w-1/2 text-black px-4 py-2 rounded-md  transition-colors duraton-200 cursor-pointer hover:bg-gray-400"
                                    :class="editProfileForm.gender == 1 ? 'bg-gray-200' : 'bg-blue-300'"
                                    @click="editProfileForm.gender = 0">女</button>
                            </div>
                        </div>
                    </div>
                    <div class="flex flex-col gap-4 p-4">
                        <div class="flex items-center  gap-4">
                            <span class="shrink-0">我的简介</span>
                            <span v-if="!is_editing">{{ currentUser?.introduction }}</span>
                            <el-input type="textarea" max="50" v-else v-model="editProfileForm.introduction"
                                :placeholder="userInfo?.introduction || '请输入简介'" class="w-full"></el-input>
                        </div>
                    </div>
                    <div class="flex flex-col gap-4 p-4">
                        <div class="flex items-center  gap-4">
                            <span class="shrink-0">我的年龄</span>
                            <span v-if="!is_editing">{{ currentUser?.age }}</span>
                            <el-input v-else v-model="editProfileForm.age" :placeholder="userInfo?.age || '请输入年龄'"
                                class="w-full"></el-input>
                        </div>
                    </div>
                    <div class="w-full flex items-center shadow-md gap-2" v-if="is_editing">
                        <button
                            class="btn w-1/2 text-center bg-blue-200 transition-colors duration-200 hover:bg-blue-400 cursor-pointer"
                            @click="saveProfile">保存</button>
                        <button
                            class="btn w-1/2 text-center bg-gray-300 transition-colors duration-200 hover:bg-gray-500 cursor-pointer"
                            @click=" is_editing = false">取消</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

</template>

<script lang="ts" setup>
import { ref, onMounted, watch, computed } from 'vue';
import { useUserInfoStore } from '@/stores/userInfo';
import router from '@/router';
import { useRoute } from 'vue-router';
import { UserAPI, type UserProfileData, type UserProfileUpdateForm } from '@/services/user';
import { CommonAPI } from '@/services/common/api';
import { log } from '@/utils/log';
import { PostStatus } from '@/services/post';
import { PostAPI } from '@/services/post/api';
import { formatNum } from '@/utils/page';
import UserProfileTabPublish from './user-profile-tab-publish.vue';
import { Plus } from '@element-plus/icons-vue';
const route = useRoute();
const uid = computed(() => route.params.id as string);
const userStore = useUserInfoStore();
const currentUser = computed(() => userStore.user);
const isme = computed(() => uid.value === currentUser?.value?.id.toString());
const curTab = ref();
const userInfo = ref<UserProfileData | null>(null);
const userTag = ref<{ id: number, name: string }[]>([]);
const pendingDeleteId = ref<number | string | null>(null);
const publishTabRef = ref();
const edit_profile = ref(false);
const is_editing = ref(false);
const handleDelete = (id: number | string) => {
    pendingDeleteId.value = id;
};
const editProfileForm = ref<UserProfileUpdateForm>(
    {
        introduction: userInfo.value?.introduction || '',
        nick: userInfo.value?.nick || '',
        avatar: userInfo.value?.avatar || '',
        age: userInfo.value?.age || 0,
        gender: userInfo.value?.gender || 0,
    }
)

const selectedFile = ref<File | null>(null);
const previewAvatarUrl = ref<string>('');
const avatarInput = ref<HTMLInputElement | null>(null);

function triggerAvatarSelect() {
    avatarInput.value?.click();
}

function handleAvatarChange(e: Event) {
    const target = e.target as HTMLInputElement;
    if (target.files && target.files.length > 0) {
        const file = target.files[0];
        if (file) {
            selectedFile.value = file;
            if (previewAvatarUrl.value) {
                URL.revokeObjectURL(previewAvatarUrl.value);
            }
            previewAvatarUrl.value = URL.createObjectURL(file);
        }
    }
}

watch(is_editing, (newVal) => {
    if (!newVal) {
        selectedFile.value = null;
        if (previewAvatarUrl.value) {
            URL.revokeObjectURL(previewAvatarUrl.value);
            previewAvatarUrl.value = '';
        }
    } else {
        if (userInfo.value) {
            editProfileForm.value = {
                introduction: userInfo.value.introduction || '',
                nick: userInfo.value.nick || '',
                avatar: userInfo.value.avatar || '',
                age: userInfo.value.age || 0,
                gender: userInfo.value.gender || 0,
            };
        }
    }
});

watch(edit_profile, (newVal) => {
    if (!newVal) {
        is_editing.value = false;
    }
});

const handleConfirmDelete = async () => {
    if (pendingDeleteId.value === null || pendingDeleteId.value === undefined) return;
    try {
        const res = await PostAPI.delete(Number(pendingDeleteId.value));
        if (res.code === 1) {
            publishTabRef.value?.loadData(curTab.value);
        } else {
            log.error(res.errMsg || "删除失败");
        }
    } catch (e) {
        console.error('删除帖子异常:', e);
        log.error("删除失败");
    } finally {
        pendingDeleteId.value = null;
    }
};
async function saveProfile() {
    if (!editProfileForm.value) return;

    let avatarUrl = editProfileForm.value.avatar;
    if (selectedFile.value) {
        try {
            const uploadRes = await CommonAPI.upload(selectedFile.value, 'avatar');
            if (uploadRes.code === 1 && uploadRes.data?.url) {
                avatarUrl = uploadRes.data.url;
                editProfileForm.value.avatar = avatarUrl;
            } else {
                log.error(uploadRes.errMsg || "头像上传失败");
                return;
            }
        } catch (error: any) {
            log.error(error.message || "头像上传失败");
            return;
        }
    }

    try {
        const { code, errMsg } = await UserAPI.updateProfile(editProfileForm.value)
        if (code != 1) {
            log.error(errMsg || "更新失败");
        } else {
            is_editing.value = false;
            if (uid.value) {
                await loadUserProfile(uid.value);
            }
            await userStore.flush();
        }
    } catch (error: any) {
        log.error(error.message || "更新失败");
    }
}
async function loadUserProfile(targetUid: string) {
    if (!targetUid) return;
    try {
        const res = await UserAPI.getUserById(Number(targetUid));
        if (res.code === 1 && res.data) {
            userInfo.value = res.data;
            userTag.value = [
                {
                    id: 1,
                    name: res.data.ip || "未知",
                }
            ];
        } else {
            log.error('未找到该用户信息');
            router.push({ name: 'home' });
        }

        await loadSelfPost();
    } catch (e) {
        console.error('获取用户信息失败', e);
        log.error('未找到该用户信息');
        router.push({ name: 'home' });
    }
}
async function loadSelfPost(status: PostStatus = PostStatus.NORMAL) {
    const res = await PostAPI.getPage({
        cursor: '',
        self: isme.value,
        creatorId: Number(uid.value),
        status: status,
    });
    console.log('用户动态列表: ', res.data);
}

onMounted(() => {
    loadUserProfile(uid.value);
});

watch(uid, (newUid) => {
    if (newUid) {
        loadUserProfile(newUid);
    }
});
</script>

<style scoped>
/* 弧度平滑向外凸起的反向圆角效果 */
.avatar-wrapper::before,
.avatar-wrapper::after {
    content: "";
    position: absolute;
    bottom: calc(50% - 0.5px);
    /* 微调贴合，确保与卡片顶部无缝拼合 */
    width: 32px;
    height: 32px;
    background: transparent;
    pointer-events: none;
    z-index: 10;
}

/* 左侧反向圆角 */
.avatar-wrapper::before {
    left: -30px;
    /* 稍微向右内收 2px，利用 avatar-wrapper 的圆形白底盖住伪元素的直边 */
    background: radial-gradient(circle at 0 0, transparent 34px, white 35px);
}

/* 右侧反向圆角 */
.avatar-wrapper::after {
    right: -30px;
    /* 稍微向左内收 2px，利用 avatar-wrapper 的圆形白底盖住伪元素的直边 */
    background: radial-gradient(circle at 100% 0, transparent 34px, white 35px);
}
</style>