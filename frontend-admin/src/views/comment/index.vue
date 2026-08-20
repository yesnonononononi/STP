<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">评论管理</h3>
        <p class="text-sm text-slate-500 mt-1">管理用户评论内容、处理违规举报及一键屏蔽/解除屏蔽评论。</p>
      </div>

      <div class="flex items-center gap-3">
        <!-- 关键词搜索 -->
        <el-input
          v-model="searchKeyword"
          placeholder="搜索评论内容"
          clearable
          class="w-56"
          @clear="fetchData"
          @keyup.enter="fetchData"
        />

        <!-- 评论状态筛选 -->
        <el-select v-model="filterStatus" placeholder="评论状态筛选" clearable class="w-44" @change="fetchData">
          <el-option label="全部状态" :value="undefined" />
          <el-option label="正常显示" :value="1" />
          <el-option label="已被举报" :value="2" />
          <el-option label="已屏蔽违规" :value="0" />
        </el-select>

        <el-button class="admin-btn-secondary" @click="fetchData">查询 / 刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="评论 ID" width="110" />
        <el-table-column prop="postId" label="帖子 ID" width="110" />
        <el-table-column prop="publisherId" label="发布者 ID" width="110" />

        <el-table-column label="评论内容" min-width="260">
          <template #default="{ row }">
            <div class="space-y-1">
              <div class="text-sm text-slate-800 font-normal">{{ row.content }}</div>
              <div v-if="row.ipLocation || row.clientType" class="text-xs text-slate-400">
                <span>{{ row.ipLocation || '未知属地' }}</span>
                <span v-if="row.clientType" class="ml-2">({{ row.clientType }})</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="举报状态 / 原因" min-width="180">
          <template #default="{ row }">
            <div v-if="row.status === 2 || row.reportReason" class="space-y-1">
              <el-tag type="danger" effect="dark" size="small">已被举报</el-tag>
              <div v-if="row.reportReason" class="text-xs text-rose-500 truncate" :title="row.reportReason">
                原因: {{ row.reportReason }}
              </div>
            </div>
            <div v-else>
              <span class="text-xs text-slate-400">无举报</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论状态" width="130">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">正常显示</el-tag>
            <el-tag v-else-if="row.status === 2" type="danger">被举报待处理</el-tag>
            <el-tag v-else-if="row.status === 0" type="warning">已屏蔽</el-tag>
            <el-tag v-else type="info">状态: {{ row.status }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" min-width="160">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ row.createTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button
                v-if="row.status !== 0"
                size="small"
                type="warning"
                plain
                @click="handleBan(row)"
              >
                屏蔽评论
              </el-button>
              <el-button
                v-else
                size="small"
                type="success"
                plain
                @click="handleUnban(row)"
              >
                解除屏蔽
              </el-button>

              <el-button
                v-if="row.status === 2 || row.reportReason"
                size="small"
                type="info"
                text
                @click="handleIgnoreReport(row)"
              >
                忽略举报
              </el-button>
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
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { CommentAPI, type AdminCommentVO, type CommentQueryPayload } from '@/services/comment'

const list = ref<AdminCommentVO[]>([])
const loading = ref(false)
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })
const searchKeyword = ref('')
const filterStatus = ref<number | undefined>(undefined)

const fetchData = async () => {
  loading.value = true
  try {
    const payload: CommentQueryPayload = {
      page: pagination.page,
      pageSize: pagination.size,
      keyword: searchKeyword.value || undefined,
      status: filterStatus.value,
    }
    const res = await CommentAPI.getCommentList(payload)
    if (res && res.data) {
      list.value = res.data.data || []
      total.value = Number(res.data.total) || 0
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleBan = (row: AdminCommentVO) => {
  ElMessageBox.confirm(`确定要屏蔽评论 (ID: ${row.id}) 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await CommentAPI.banComment(row.id)
      ElMessage.success('评论已屏蔽')
      fetchData()
    } catch {
      // 拦截器捕获
    }
  }).catch(() => {})
}

const handleUnban = (row: AdminCommentVO) => {
  ElMessageBox.confirm(`确定要解除屏蔽评论 (ID: ${row.id}) 吗？`, '提示', { type: 'info' }).then(async () => {
    try {
      await CommentAPI.unbanComment(row.id)
      ElMessage.success('已解除屏蔽')
      fetchData()
    } catch {
      // 拦截器捕获
    }
  }).catch(() => {})
}

const handleIgnoreReport = (row: AdminCommentVO) => {
  ElMessageBox.confirm(`确定要忽略对评论 (ID: ${row.id}) 的举报吗？`, '提示', { type: 'info' }).then(async () => {
    try {
      await CommentAPI.ignoreReport(row.id)
      ElMessage.success('已忽略该举报')
      fetchData()
    } catch {
      // 拦截器捕获
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
