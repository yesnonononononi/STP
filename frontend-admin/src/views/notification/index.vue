<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">系统通知公告管理</h3>
        <p class="text-sm text-slate-500 mt-1">创建全员广播通知、指定用户推送及系统维护公告，支持发布、撤回与删除管理。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="filterKeyword" placeholder="搜索通知正文内容..." clearable class="w-64" @keyup.enter="fetchData" />
        <el-select v-model="filterNoticeType" placeholder="通知类型" clearable class="w-40" @change="fetchData">
          <el-option label="全部类型" :value="undefined" />
          <el-option label="系统通知" :value="1" />
          <el-option label="活动营销" :value="2" />
          <el-option label="维护公告" :value="3" />
        </el-select>
        <el-checkbox v-model="filterExcludeDeleted" @change="fetchData" class="font-medium text-slate-700">过滤已删除通知</el-checkbox>

        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 新建通知
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="通知 ID" width="110" />

        <el-table-column label="发布发起人" width="140">
          <template #default="{ row }">
            <span class="font-mono text-sm text-slate-700">UID: {{ row.fromUserId || '系统' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="通知类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getNoticeTypeTag(row.type)">
              {{ getNoticeTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="接收用户范围" width="150">
          <template #default="{ row }">
            <span v-if="row.associateUser" class="text-xs font-mono text-blue-600">指定用户: {{ row.associateUser }}</span>
            <span v-else class="text-xs text-slate-600">全员广播</span>
          </template>
        </el-table-column>

        <el-table-column label="通知正文 / 配图" min-width="260">
          <template #default="{ row }">
            <div class="text-xs text-slate-700 font-normal line-clamp-2 mb-1">{{ row.content }}</div>
            <div v-if="row.images && row.images.length" class="flex gap-1 overflow-x-auto">
              <el-image
                v-for="(img, i) in row.images"
                :key="i"
                :src="img"
                :preview-src-list="row.images"
                class="w-10 h-10 rounded border object-cover shrink-0 cursor-pointer"
                fit="cover"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column label="发布状态" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">已推送到站</el-tag>
            <el-tag v-else-if="row.status === 2" type="warning">已撤回</el-tag>
            <el-tag v-else-if="row.status === 3" type="error">已删除</el-tag>
            <el-tag v-else type="info">待发布/草稿</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布 / 创建时间" min-width="180">
          <template #default="{ row }">
            <div class="text-xs text-slate-500 space-y-0.5">
              <div>发布: {{ formatDate(row.publicTime) }}</div>
              <div>创建: {{ formatDate(row.createTime) }}</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button v-if="row.status !== 1" size="small" type="success" plain @click="handlePublish(row)">
                发布到站
              </el-button>
              <el-button v-if="row.status === 1" size="small" type="warning" plain @click="handleRevoke(row)">
                撤回通知
              </el-button>

              <el-button v-if="row.status !== 3" size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页栏 -->
      <div class="flex justify-end mt-6">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="fetchData"
          @size-change="fetchData"
        />
      </div>
    </div>

    <!-- 创建对话框 -->
    <el-dialog v-model="dialogVisible" title="新建系统通知" width="580px" destroy-on-close>
      <el-form :model="form" label-width="110px" class="space-y-4">
        <el-form-item label="通知分类" required>
          <el-select v-model="form.noticeType" class="w-full">
            <el-option label="系统通知" :value="1" />
            <el-option label="营销活动" :value="2" />
            <el-option label="维护公告" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="接收范围" required>
          <el-select v-model="form.targetType" class="w-full">
            <el-option label="全体用户广播" :value="1" />
            <el-option label="指定单用户" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="form.targetType === 2" label="目标用户 ID" required>
          <el-input-number v-model="form.targetUserId" :min="1" class="w-full" placeholder="输入用户 ID" />
        </el-form-item>

        <!-- 图片上传（系统专用 Bucket） -->
        <el-form-item label="通知配图">
          <div class="space-y-2 w-full">
            <el-upload
              action=""
              :http-request="customUpload"
              :show-file-list="false"
              accept="image/*"
            >
              <el-button type="primary" plain size="small" :loading="uploading">
                + 上传图片 (Bucket: system)
              </el-button>
            </el-upload>
            <div v-if="uploadedImages.length > 0" class="flex flex-wrap gap-2 mt-2">
              <div v-for="(img, idx) in uploadedImages" :key="idx" class="relative group w-16 h-16 rounded border border-slate-200 overflow-hidden shadow-2xs">
                <img :src="img" class="w-full h-full object-cover" />
                <span @click="removeImage(idx)" class="absolute top-0 right-0 bg-rose-500 text-white text-xs px-1 cursor-pointer opacity-80 hover:opacity-100 transition-opacity">×</span>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item label="通知正文内容" required>
          <el-input v-model="form.content" type="textarea" :rows="4" placeholder="请输入详细的通知文案内容..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :disabled="!form.content.trim()" @click="handleCreate">确认创建</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { NotificationAPI, type AdminNotificationVO, type CreateNotificationPayload } from '@/services/notification'
import { CommonAPI } from '@/services/common/api'
import { formatDate } from '@/utils/formatDate'

const list = ref<AdminNotificationVO[]>([])
const loading = ref(false)
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })
const filterKeyword = ref('')
const filterNoticeType = ref<number | undefined>(undefined)
const filterExcludeDeleted = ref<boolean>(true)
const dialogVisible = ref(false)

const uploadedImages = ref<string[]>([])
const uploading = ref(false)

const form = reactive<CreateNotificationPayload>({
  title: '',
  content: '',
  noticeType: 1,
  targetType: 1,
  targetUserId: undefined,
  imageUrls: []
})

const customUpload = async (options: { file: File }) => {
  uploading.value = true
  try {
    const res = await CommonAPI.upload(options.file, 'system')
    if (res && res.data && res.data.url) {
      uploadedImages.value.push(res.data.url)
      ElMessage.success('配图已成功上传至 system bucket')
    } else {
      ElMessage.error(res?.errMsg || '图片上传失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '图片上传异常')
  } finally {
    uploading.value = false
  }
}

const removeImage = (index: number) => {
  uploadedImages.value.splice(index, 1)
}

const getNoticeTypeName = (type?: number) => {
  if (type === 1) return '系统通知'
  if (type === 2) return '营销活动'
  if (type === 3) return '维护公告'
  return `类型: ${type ?? '-'}`
}

const getNoticeTypeTag = (type?: number) => {
  if (type === 1) return 'primary'
  if (type === 2) return 'success'
  if (type === 3) return 'warning'
  return 'info'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await NotificationAPI.getNotificationList({
      keyword: filterKeyword.value || undefined,
      noticeType: filterNoticeType.value,
      excludeDeleted: filterExcludeDeleted.value,
      page: pagination.page,
      size: pagination.size
    })
    if (res && res.data) {
      list.value = res.data.data || []
      total.value = Number(res.data.total) || 0
    }
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  uploadedImages.value = []
  Object.assign(form, {
    title: '系统通知',
    content: '',
    noticeType: 1,
    targetType: 1,
    targetUserId: undefined,
    imageUrls: []
  })
  dialogVisible.value = true
}

const handleCreate = async () => {
  if (!form.content.trim()) {
    ElMessage.warning('请输入通知内容')
    return
  }
  try {
    form.imageUrls = [...uploadedImages.value]
    await NotificationAPI.createNotification(form)
    ElMessage.success('通知创建成功')
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handlePublish = (row: AdminNotificationVO) => {
  ElMessageBox.confirm(`确定要发布通知 (ID: ${row.id}) 到站吗？`, '发布确认', { type: 'success' }).then(async () => {
    try {
      await NotificationAPI.publishNotification(row.id)
      ElMessage.success('通知发布成功')
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

const handleRevoke = (row: AdminNotificationVO) => {
  ElMessageBox.confirm(`确定要撤回通知 (ID: ${row.id}) 吗？`, '撤回确认', { type: 'warning' }).then(async () => {
    try {
      await NotificationAPI.revokeNotification(row.id)
      ElMessage.success('通知已撤回')
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

const handleDelete = (row: AdminNotificationVO) => {
  ElMessageBox.confirm(`确定要删除通知记录 (ID: ${row.id}) 吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await NotificationAPI.deleteNotification(row.id)
      ElMessage.success('删除成功')
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
