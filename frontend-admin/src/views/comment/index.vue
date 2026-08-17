<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">评论管理</h3>
        <p class="text-sm text-slate-500 mt-1">审核用户评论内容、处理违规举报及一键切换审核通过状态。</p>
      </div>

      <div class="flex items-center gap-3">
        <!-- 按举报 / 被举报状态筛选 -->
        <el-select v-model="filterReportStatus" placeholder="举报状态筛选" clearable class="w-48" @change="fetchData">
          <el-option label="全部评论" :value="undefined" />
          <el-option label="未被举报" :value="0" />
          <el-option label="已被举报 (待处理)" :value="1" />
          <el-option label="举报已处理" :value="2" />
        </el-select>

        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="评论 ID" width="90" />

        <el-table-column label="评论者" min-width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-avatar :src="row.userAvatar || undefined" :size="28">
                {{ (row.userName || '?').slice(0, 1) }}
              </el-avatar>
              <div class="truncate text-sm font-medium text-slate-800">{{ row.userName }} (ID:{{ row.userId }})</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论内容" min-width="260">
          <template #default="{ row }">
            <div class="space-y-1">
              <div class="text-sm text-slate-800 font-normal">{{ row.content }}</div>
              <div v-if="row.postTitle" class="text-xs text-slate-400 truncate">
                源自帖子: <span class="text-slate-600 font-medium">《{{ row.postTitle }}》</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="举报状态" min-width="150">
          <template #default="{ row }">
            <div v-if="row.reportStatus === 1" class="space-y-1">
              <el-tag type="danger" effect="dark" size="small">
                被举报 {{ row.reportCount }} 次
              </el-tag>
              <div v-if="row.reportReason" class="text-xs text-rose-500 truncate" :title="row.reportReason">
                原因: {{ row.reportReason }}
              </div>
            </div>
            <div v-else-if="row.reportStatus === 2">
              <el-tag type="info" size="small">举报已忽略/处理</el-tag>
            </div>
            <div v-else>
              <span class="text-xs text-slate-400">无举报</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="审核通过状态" width="140">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常显示' : '已屏蔽违规' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" min-width="160">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ row.createTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <!-- 审核通过状态 Toggle -->
              <el-button
                size="small"
                :type="row.status === 1 ? 'warning' : 'success'"
                plain
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? '屏蔽评论' : '解除屏蔽' }}
              </el-button>

              <el-button v-if="row.reportStatus === 1" size="small" type="info" text @click="handleIgnoreReport(row)">
                忽略举报
              </el-button>

              <el-button size="small" type="danger" text @click="handleDelete(row)">删除</el-button>
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
import { ElMessageBox } from 'element-plus'
import { CommentAPI, type CommentItem, type CommentQueryPayload } from '@/services/comment'

const list = ref<CommentItem[]>([])
const loading = ref(false)
const total = ref(0)
const pagination = reactive({ page: 1, size: 10 })
const filterReportStatus = ref<number | undefined>(undefined)

const fetchData = async () => {
  loading.value = true
  try {
    const payload: CommentQueryPayload = {
      page: pagination.page,
      size: pagination.size,
      reportStatus: filterReportStatus.value
    }
    const res = await CommentAPI.getCommentList(payload)
    list.value = res.data?.list || [
      { id: 801, postId: 501, postTitle: '分享一个超实用 Spring Boot Starter 开发指南', userId: 10090, userName: '极客小李', userAvatar: '', content: '写得太棒了，正好解决了我们的解耦问题！', reportStatus: 0, reportCount: 0, status: 1, createTime: '2026-08-16 10:25' },
      { id: 802, postId: 501, postTitle: '分享一个超实用 Spring Boot Starter 开发指南', userId: 10091, userName: '喷子用户', userAvatar: '', content: '垃圾文章，毫无技术含量！', reportStatus: 1, reportCount: 3, reportReason: '言语攻击/人身攻击', status: 1, createTime: '2026-08-16 10:30' },
      { id: 803, postId: 502, postTitle: '【推广】点击免费领取大额优惠券', userId: 10092, userName: '广告机器人', userAvatar: '', content: '加V送福利微信123456...', reportStatus: 1, reportCount: 8, reportReason: '垃圾广告', status: 0, createTime: '2026-08-16 11:10' }
    ]
    total.value = res.data?.total || list.value.length
  } catch {
    // 拦截器捕获
  } finally {
    loading.value = false
  }
}

// 审核通过状态 Toggle
const handleToggleStatus = async (row: CommentItem) => {
  const nextStatus = row.status === 1 ? 0 : 1
  const actionText = nextStatus === 0 ? '屏蔽' : '解除屏蔽'
  ElMessageBox.confirm(`确定要${actionText}该评论吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await CommentAPI.toggleCommentStatus(row.id, nextStatus)
      fetchData()
    } catch {
      // 拦截器捕获
    }
  }).catch(() => {})
}

const handleIgnoreReport = async (row: CommentItem) => {
  try {
    await CommentAPI.ignoreReport(row.id)
    fetchData()
  } catch {
    // 拦截器捕获
  }
}

const handleDelete = (row: CommentItem) => {
  ElMessageBox.confirm(`确定要彻底删除该评论吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await CommentAPI.deleteComment(row.id)
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
