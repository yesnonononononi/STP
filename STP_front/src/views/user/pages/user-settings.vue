<template>
    <div class="w-full min-h-screen bg-linear-to-tl from-blue-100 via-blue-300 to-blue-200 flex justify-center p-8">
        <div class="w-full md:max-w-2xl bg-white rounded-md shadow-sm border border-gray-200 self-start">
            <div class="phone-setting w-full p-6">
                <!-- 头部标题 -->
                <div class="border-b border-gray-200 pb-4 mb-8">
                    <h2 class="text-xl font-bold text-gray-800">登录手机</h2>
                    <div class="text-xs text-gray-400 mt-1 flex items-center gap-1 select-none">
                        <span
                            class="inline-flex items-center justify-center w-3.5 h-3.5 rounded-full border border-gray-300 text-[10px] text-gray-400">?</span>
                        手机号码已被注册?
                    </div>
                </div>

                <div class="flex flex-col gap-6 max-w-md mx-auto  py-4">
                    <div class="flex items-center gap-6">
                        <label class="w-18 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">原手机号码</label>
                        <div class="text-sm text-gray-500 font-medium ml-2 select-none">
                            {{ user?.phone || "未绑定" }}
                        </div>
                    </div>

                    <div class="flex items-center gap-6">
                        <label class="w-18 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">新手机号码</label>
                        <div class="flex gap-2 flex-1">
                            <input v-model="phoneForm.phoneNumber"
                                class="flex-1 border border-gray-300 rounded px-3 py-1.5 outline-none text-sm placeholder-gray-400  focus:ring-1 focus:ring-blue-300 transition-all"
                                type="text" maxlength="11" placeholder="请输入手机号码" />
                        </div>
                    </div>

                    <!-- 验证码 -->
                    <div class="flex items-center gap-6">
                        <label class="w-12 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">验证码</label>
                        <div class="flex flex-1">
                            <input type="text" v-model.number="phoneForm.verifyCode"
                                class="flex-1 border border-gray-300 rounded-l  md:px-3 py-1.5 outline-none text-sm placeholder-gray-400   focus:ring-1 focus:ring-blue-200 transition-all border-r-0"
                                placeholder="请输入验证码" />
                            <button type="button" @click="sendPhoneCode"
                                class=" text-white bg-blue-400 hover:bg-blue-600 px-5 py-1.5 text-xs font-medium rounded-r   cursor-pointer shrink-0">
                                获取验证码
                            </button>
                        </div>
                    </div>

                    <!-- 保存按钮 -->
                    <div class="flex gap-6 mt-4">
                        <div class="w-24 shrink-0"></div>
                        <button type="button" @click="handleSavePhone"
                            class="flex-1 hover:scale-[1.05] text-white bg-linear-to-tr from-blue-300 to-blue-100   py-2 px-4 rounded text-sm font-medium   cursor-pointer shadow-sm text-center">
                            保存
                        </button>
                    </div>
                </div>
            </div>
            <div class="email-setting w-full p-6">
                <!-- 头部标题 -->
                <div class="border-b border-gray-200 pb-4 mb-8">
                    <h2 class="text-xl font-bold text-gray-800">绑定邮箱</h2>
                </div>

                <div class="flex flex-col gap-6 max-w-md mx-auto  py-4">
                    <div class="flex items-center gap-6">
                        <label class="w-18 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">原邮箱</label>
                        <div class="text-sm text-gray-500 font-medium ml-2 select-none">
                            {{ user?.email || "未绑定" }}
                        </div>
                    </div>
                    <div class="flex items-center gap-6">
                        <label class="w-18 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">新邮箱</label>
                        <div class="flex gap-2 flex-1">
                            <input v-model="emailForm.email"
                                class="flex-1 border border-gray-300 rounded px-3 py-1.5 outline-none text-sm placeholder-gray-400   focus:ring-1 focus:ring-blue-300 transition-all"
                                type="text" placeholder="请输入新邮箱" />
                        </div>
                    </div>

                    <!-- 验证码 -->
                    <div class="flex items-center gap-6">
                        <label class="w-12 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">验证码</label>
                        <div class="flex flex-1">
                            <input type="text" v-model.number="emailForm.verifyCode"
                                class="flex-1 border border-gray-300 rounded-l  md:px-3 py-1.5 outline-none text-sm placeholder-gray-400   focus:ring-1 focus:ring-blue-300 transition-all border-r-0"
                                placeholder="请输入验证码" />
                            <button type="button" @click="sendEmailCode"
                                class=" text-white bg-blue-400 hover:bg-blue-600 px-5 py-1.5 text-xs font-medium rounded-r   cursor-pointer shrink-0">
                                获取验证码
                            </button>
                        </div>
                    </div>

                    <!-- 保存按钮 -->
                    <div class="flex gap-6 mt-4">
                        <div class="w-24 shrink-0"></div>
                        <button type="button" @click="handleSaveEmail"
                            class="flex-1 hover:scale-[1.05] text-white bg-linear-to-tr from-blue-300 to-blue-100   py-2 px-4 rounded text-sm font-medium   cursor-pointer shadow-sm text-center">
                            保存
                        </button>
                    </div>
                </div>
            </div>

            <div class="phone-setting w-full p-6">
                <!-- 头部标题 -->
                <div class="border-b border-gray-200 pb-4 mb-8">
                    <h2 class="text-xl font-bold text-gray-800">登录密码</h2>
                </div>

                <div class="flex flex-col gap-6 max-w-md mx-auto  py-4">
                    <div class="flex items-center gap-6">
                        <label class="w-18 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">原密码</label>
                        <input v-model="passwordForm.oldPassword"
                            class="flex-1 border border-gray-300 rounded px-3 py-1.5 outline-none text-sm placeholder-gray-400  focus:ring-1 focus:ring-blue-300 transition-all"
                            type="text" placeholder="请输入原密码" />
                    </div>

                    <div class="flex items-center gap-6">
                        <label class="w-18 md:w-24 text-right text-sm font-medium text-gray-600 shrink-0">新密码</label>
                        <div class="flex gap-2 flex-1">
                            <input v-model="passwordForm.newPassword"
                                class="flex-1 border border-gray-300 rounded px-3 py-1.5 outline-none text-sm placeholder-gray-400  focus:ring-1 focus:ring-blue-300 transition-all"
                                type="password" placeholder="请输入新密码" />
                        </div>
                    </div>

                    <!-- 保存按钮 -->
                    <div class="flex gap-6 mt-4">
                        <div class="w-24 shrink-0"></div>
                        <button type="button" @click="handleSavePassword"
                            class="flex-1 hover:scale-[1.05] text-white bg-linear-to-tr from-blue-300 to-blue-100   py-2 px-4 rounded text-sm font-medium   cursor-pointer shadow-sm text-center">
                            修改
                        </button>
                    </div>
                </div>
            </div>
            <div class="privacy w-full p-6">
                <div class="border-b border-gray-200 pb-4 mb-8">
                    <h2 class="text-xl font-bold text-gray-800">隐私</h2>
                </div>
                <div class="w-full h-18 flex items-center gap-8">
                    <div class="flex-1 h-12 justify-center items-center flex  bg-gray-400 rounded-md">我屏蔽的 {{ 0 }} 人
                    </div>
                    <div class="flex-1 h-12 justify-center items-center flex  bg-gray-400 rounded-md">我拉黑的 {{ 0 }} 人
                    </div>
                </div>
                <div class="w-full m-4">
                    <div class="flex items-center justify-between p-2">
                        <span class="">个性化推荐</span>
                        <div class="w-12 h-6 rounded-xl bg-linear-to-r  from-blue-200 via-blue-100 cursor-pointer to-blue-300 mr-2 flex items-center"
                            @click="canRecommend = !canRecommend">
                            <div class="w-6 h-6 rounded-full  transition-all duration-300 "
                                :class="canRecommend ? 'translate-x-6 bg-blue-400' : 'translate-x-0 bg-blue-300'">
                            </div>
                        </div>
                    </div>
                    <span class=" text-gray-400">开启个性化推荐,会推荐你感兴趣的内容</span>
                </div>
                <div class="w-full m-4">
                    <div class="flex items-center justify-between p-2">
                        <span class="">不显示已删除的帖子</span>
                        <div class="w-12 h-6 rounded-xl bg-linear-to-r  from-blue-200 via-blue-100 cursor-pointer to-blue-300 mr-2 flex items-center"
                            @click="hideDeletedPosts = !hideDeletedPosts">
                            <div class="w-6 h-6 rounded-full  transition-all duration-300 "
                                :class="hideDeletedPosts ? 'translate-x-6 bg-blue-400' : 'translate-x-0 bg-blue-300'">
                            </div>
                        </div>
                    </div>
                    <span class=" text-gray-400">不再在主页显示已经删除的帖子</span>
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts" setup>
import { useUserInfoStore } from '@/stores/userInfo';
import { ref, reactive, computed, onMounted, watch } from 'vue';
import { UserAPI } from '@/services/user';
import { log } from '@/utils/log';

const userStore = useUserInfoStore();
const user = computed(() => userStore.user);
const canRecommend = ref(true)
const hideDeletedPosts = ref(false)
const initializing = ref(true)

// 加载初始隐私偏好
onMounted(async () => {
    try {
        await userStore.fetchSettings();
        if (userStore.settings) {
            canRecommend.value = userStore.settings.customizationRecommend === 1;
            hideDeletedPosts.value = userStore.settings.showDelPost === 0;
        }
    } catch (error: any) {
        console.error('加载隐私设置失败:', error);
    } finally {
        initializing.value = false;
    }
});

// 监听状态变动自动同步至后端
watch([canRecommend, hideDeletedPosts], async ([newRecommend, newHideDeleted]) => {
    if (initializing.value) return;
    try {
        const res = await UserAPI.updateSettings({
            customizationRecommend: newRecommend ? 1 : 0,
            showDelPost: newHideDeleted ? 0 : 1
        });
        if (res.code === 1) {
            // 同步本地 Pinia 状态
            userStore.settings = {
                customizationRecommend: newRecommend ? 1 : 0,
                showDelPost: newHideDeleted ? 0 : 1
            };
        }
    } catch (error: any) {
        log.error(error.message || '隐私设置更新失败');
    }
});

// 登录手机绑定表单
const phoneForm = reactive({
    phoneNumber: '',
    verifyCode: null as number | null
})

// 邮箱绑定表单
const emailForm = reactive({
    email: '',
    verifyCode: null as number | null
})

// 密码修改表单
const passwordForm = reactive({
    oldPassword: '',
    newPassword: ''
})

/**
 * 模拟发送手机验证码
 */
const sendPhoneCode = () => {
    if (!phoneForm.phoneNumber) {
        log.warning('请输入手机号码');
        return;
    }
    log.success('手机验证码发送成功（模拟）');
}

/**
 * 模拟发送邮箱验证码
 */
const sendEmailCode = () => {
    if (!emailForm.email) {
        log.warning('请输入新邮箱地址');
        return;
    }
    log.success('邮箱验证码发送成功（模拟）');
}

/**
 * 提交手机绑定保存
 */
const handleSavePhone = async () => {
    if (!phoneForm.phoneNumber || phoneForm.verifyCode === null) {
        log.warning('请填写新手机号码和验证码');
        return;
    }
    try {
        const res = await UserAPI.bindPhone({
            phoneNumber: phoneForm.phoneNumber,
            verifyCode: phoneForm.verifyCode
        });
        if (res.code === 1) {
            log.success('手机号绑定成功');
            // 更新前端本地 Store
            if (userStore.user) {
                userStore.user.phone = phoneForm.phoneNumber;
            }
            phoneForm.phoneNumber = '';
            phoneForm.verifyCode = null;
        }
    } catch (error: any) {
        log.error(error.message || '手机号绑定失败');
    }
}

/**
 * 提交邮箱绑定保存
 */
const handleSaveEmail = async () => {
    if (!emailForm.email || emailForm.verifyCode === null) {
        log.warning('请填写新邮箱地址和验证码');
        return;
    }
    try {
        const res = await UserAPI.updateProfile({
            email: emailForm.email,
            verifyCode: emailForm.verifyCode.toString()
        });
        if (res.code === 1) {
            log.success('邮箱绑定成功');
            if (userStore.user) {
                userStore.user.email = emailForm.email;
            }
            emailForm.email = '';
            emailForm.verifyCode = null;
        }
    } catch (error: any) {
        log.error(error.message || '邮箱绑定失败');
    }
}

/**
 * 提交密码修改
 */
const handleSavePassword = async () => {
    if (!passwordForm.oldPassword || !passwordForm.newPassword) {
        log.warning('请输入原密码和新密码');
        return;
    }
    try {
        const res = await UserAPI.updatePassword({
            oldPassword: passwordForm.oldPassword,
            newPassword: passwordForm.newPassword
        });
        if (res.code === 1) {
            log.success('密码修改成功');
            passwordForm.oldPassword = '';
            passwordForm.newPassword = '';
        }
    } catch (error: any) {
        log.error(error.message || '密码修改失败');
    }
}
</script>

<style scoped>
input {
    outline: none;
}
</style>