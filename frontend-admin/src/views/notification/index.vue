<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">系统通知公告管理</h3>
        <p class="text-sm text-slate-500 mt-1">创建全员广播通知、特定用户类型推送及系统维护公告，支持条件筛选与状态撤回。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="filterKeyword" placeholder="搜索通知标题/内容..." clearable class="w-64" @keyup.enter="fetchData" />
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 发布通知
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="通知 ID" width="90" />

        <el-table-column prop="title" label="通知标题" min-width="180">
          <template #default="{ row }">
            <span class="font-semibold text-slate-800">{{ row.title }}</span>
          </template>
        </el-table-column>

        <el-table-column label="通知类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getNoticeTypeTag(row.noticeType)">
              {{ getNoticeTypeName(row.noticeType) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="接收人群范围" width="140">
          <template #default="{ row }">
            <span class="text-sm text-slate-700">{{ getTargetTypeName(row.targetType) }}</span>
            <span v-if="row.targetUserId" class="text-xs text-slate-400 block">UID: {{ row.targetUserId }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="content" label="通知正文内容" min-width="240">
          <template #default="{ row }">
            <div class="text-xs text-slate-600 line-clamp-2">{{ row.content }}</div>
          </template>
        </el-table-column>

        <el-table-column label="发布状态" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已推送到站' : '已撤回作废' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发送者 / 时间" min-width="170">
          <template #default="{ row }">
            <div class="text-xs text-slate-500">
              <div>发布者: {{ row.senderName }}</div>
              <div>时间: {{ row.sendTime }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-button v-if="row.status === 1" size="small" type="warning" plain @click="handleRevoke(row)">
                撤回通知
              </el-button>
              <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- CRUD 对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑通知' : '发布系统通知'" width="560px">
      <el-form :model="form" label-width="110px" class="space-y-4">
        <el-form-item label="通知标题" required>
          <el-input v-model="form.title" placeholder="如：系统升级与停机维护公告" />
        </el-form-item>
        <el-form-item label="通知分类" required>
          <el-select v-model="form.noticeType" class="w-full">
            <el-option label="系统通知" value="SYSTEM" />
            <el-option label="活动通知" value="ACTIVITY" />
            <el-option label="维护公告" value="MAINTENANCE" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收范围" required>
          <el-select v-model="form.targetType" class="w-full">
            <el-option label="全体用户广播 (ALL)" value="ALL" />
            <el-option label="仅 VIP 会员" value="MEMBER_ONLY" />
            <el-option label="指定单用户" value="SPECIFIC_USER" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.targetType === 'SPECIFIC_USER'" label="目标用户 ID" required>
          <el-input-number v-model="form.targetUserId" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="通知正文内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="详细通知消息文案..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">确认发送</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { NotificationAPI, type NotificationItem } from '@/services/notification'

const list = ref<NotificationItem[]>([])
const loading = ref(false)
const filterKeyword = ref('')
const dialogVisible = ref(false)

const form = reactive<Partial<NotificationItem>>({
  id: undefined,
  title: '',
  content: '',
  targetType: 'ALL',
  noticeType: 'SYSTEM',
  status: 1
})

const getNoticeTypeName = (type: string) => {
  if (type === 'SYSTEM') return '系统通知'
  if (type === 'ACTIVITY') return '营销活动'
  return '维护公告'
}

const getNoticeTypeTag = (type: string) => {
  if (type === 'SYSTEM') return 'primary'
  if (type === 'ACTIVITY') return 'success'
  return 'warning'
}

const getTargetTypeName = (type: string) => {
  if (type === 'ALL') return '全体用户广播'
  if (type === 'MEMBER_ONLY') return '仅VIP会员'
  return '指定单用户'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await NotificationAPI.getNotificationList({ keyword: filterKeyword.value, page: 1, size: 20 })
    list.value = res.data?.list || [
      { id: 701, title: 'STP 社区 2.0 系统升级维护公告', content: '为了提供更好的服务体验，系统将于今晚 02:00 进行例行维护升级...', targetType: 'ALL', noticeType: 'MAINTENANCE', status: 1, senderName: '系统管理员', sendTime: '2026-08-16 12:00', createTime: '2026-08-16 12:00' },
      { id: 702, title: '尊贵VIP专属权益升级通知', content: '尊敬的 VIP 用户，您的专属成长积分系数已提高...', targetType: 'MEMBER_ONLY', noticeType: 'SYSTEM', status: 1, senderName: '运营团队', sendTime: '2026-08-15 09:00', createTime: '2026-08-15 09:00' }
    ]
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  Object.assign(form, { id: undefined, title: '', content: '', targetType: 'ALL', noticeType: 'SYSTEM', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: NotificationItem) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    if (form.id) {
      await NotificationAPI.updateNotification(form)
    } else {
      await NotificationAPI.createNotification(form)
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleRevoke = (row: NotificationItem) => {
  ElMessageBox.confirm(`确定要撤回通知 [${row.title}] 吗？`, '撤回确认', { type: 'warning' }).then(async () => {
    try {
      await NotificationAPI.revokeNotification(row.id)
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

const handleDelete = (row: NotificationItem) => {
  ElMessageBox.confirm(`确定要彻底删除该通知记录吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await NotificationAPI.deleteNotification(row.id)
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
