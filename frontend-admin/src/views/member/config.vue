<template>
  <div class="space-y-6 animate-fade-in text-slate-800 max-w-4xl">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex justify-between items-center">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">会员系统全局配置</h3>
        <p class="text-sm text-slate-500 mt-1">设置会员消费积分比例、签到奖励、VIP 价格体系及退款审批策略。</p>
      </div>
      <el-button type="primary" class="admin-btn-primary" :loading="saving" @click="handleSave">
        保存配置
      </el-button>
    </div>

    <div class="rounded-xl border border-slate-200 bg-white shadow-xs p-8 space-y-6">
      <h4 class="text-base font-semibold text-slate-700 border-b border-slate-100 pb-3">1. 积分与签到规则</h4>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">消费赠送积分比例 (元/分)</label>
          <el-input-number v-model="config.pointsRate" :min="0.1" :precision="1" :step="0.5" class="w-full" />
          <span class="text-xs text-slate-400 mt-1 block">每消费 1 元获得的成长积分数</span>
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">每日签到基础积分</label>
          <el-input-number v-model="config.checkinBonus" :min="0" :step="5" class="w-full" />
          <span class="text-xs text-slate-400 mt-1 block">普通用户每日签到获取基础积分</span>
        </div>
      </div>

      <h4 class="text-base font-semibold text-slate-700 border-b border-slate-100 pb-3 pt-4">2. VIP 价格体系 (元)</h4>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">VIP 月卡标准价</label>
          <el-input-number v-model="config.vipMonthlyPrice" :min="0" :precision="2" class="w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">VIP 年卡优惠价</label>
          <el-input-number v-model="config.vipYearlyPrice" :min="0" :precision="2" class="w-full" />
        </div>
        <div>
          <label class="block text-sm font-medium text-slate-700 mb-2">连续包月折扣 (%)</label>
          <el-input-number v-model="config.autoRenewDiscount" :min="1" :max="100" class="w-full" />
        </div>
      </div>

      <h4 class="text-base font-semibold text-slate-700 border-b border-slate-100 pb-3 pt-4">3. 自动化运维策略</h4>
      <div class="flex items-center justify-between p-4 rounded-lg bg-slate-50 border border-slate-200/80">
        <div>
          <span class="text-sm font-medium text-slate-800 block">自动审核会员退款申请</span>
          <span class="text-xs text-slate-500">开启后，3天内且未消费任何权益的退款申请将直接由系统自动完成审核退款</span>
        </div>
        <el-switch v-model="config.autoApproveRefund" />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { MemberAPI, type MemberConfig } from '@/services/member'

const saving = ref(false)
const config = reactive<MemberConfig>({
  pointsRate: 1.0,
  checkinBonus: 10,
  vipMonthlyPrice: 25.0,
  vipYearlyPrice: 198.0,
  autoRenewDiscount: 85,
  autoApproveRefund: false
})

const fetchConfig = async () => {
  try {
    const res = await MemberAPI.getConfig()
    if (res.data) {
      Object.assign(config, res.data)
    }
  } catch {
    // 拦截器统一处理
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await MemberAPI.updateConfig(config)
  } catch {
    // 拦截器统一处理
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  fetchConfig()
})
</script>
