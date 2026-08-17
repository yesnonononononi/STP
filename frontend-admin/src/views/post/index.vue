<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <!-- 头部工具栏 -->
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">帖子内容管理</h3>
        <p class="text-sm text-slate-500 mt-1">审核社区帖子、查看元信息详情、切换展示状态及组合筛选内容。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="filterForm.keyword" placeholder="搜索帖子标题/内容..." clearable class="w-64" @keyup.enter="fetchData" />
        <el-button type="primary" class="admin-btn-primary" @click="openFilterDialog = true">
          高级筛选
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="帖子 ID" width="90" />

        <el-table-column label="发布作者" min-width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-avatar :src="row.authorAvatar || undefined" :size="28">
                {{ (row.authorName || '?').slice(0, 1) }}
              </el-avatar>
              <div class="truncate text-sm font-medium text-slate-800">{{ row.authorName }} (ID:{{ row.userId }})</div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="帖子标题 / 预览" min-width="240">
          <template #default="{ row }">
            <div class="space-y-1">
              <div class="font-semibold text-slate-800 hover:text-blue-600 cursor-pointer truncate" @click="openDetailModal(row)">
                {{ row.title }}
              </div>
              <div class="text-xs text-slate-500 line-clamp-1">{{ row.content }}</div>
              <div v-if="row.topicName" class="inline-block mt-1">
                <el-tag size="small" type="info"># {{ row.topicName }}</el-tag>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="互动数据" width="160">
          <template #default="{ row }">
            <div class="text-xs text-slate-600 space-y-0.5">
              <div>浏览: <span class="font-medium text-slate-800">{{ row.viewCount }}</span></div>
              <div>点赞: <span class="font-medium text-slate-800">{{ row.likeCount }}</span> | 评论: <span class="font-medium text-slate-800">{{ row.commentCount }}</span></div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="审核状态" width="130">
          <template #default="{ row }">
            <div class="space-y-1">
              <el-tag :type="getStatusTag(row.status)">
                {{ getStatusText(row.status) }}
              </el-tag>
              <div v-if="row.status === 0 && row.rejectReason" class="text-xs text-rose-500 truncate max-w-[110px]" :title="row.rejectReason">
                因: {{ row.rejectReason }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="发布时间" min-width="160">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ row.createTime }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <!-- 查看详情按钮 -->
              <el-button size="small" type="primary" plain @click="openDetailModal(row)">
                详情
              </el-button>

              <!-- 审核通过 Toggle -->
              <el-button v-if="row.status !== 1" size="small" type="success" plain @click="handlePass(row)">
                审核通过
              </el-button>

              <!-- 审核不通过（弹出填写驳回理由弹窗） -->
              <el-button v-if="row.status !== 0" size="small" type="danger" plain @click="openRejectModal(row)">
                驳回
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

    <!-- 帖子元信息详情悬浮框 Modal -->
    <el-dialog v-model="detailDialogVisible" title="帖子元信息详情" width="680px" destroy-on-close>
      <div v-if="detailPost" class="space-y-5 text-slate-800">
        <!-- 1. 头部作者与时间状态 -->
        <div class="flex items-center justify-between border-b border-slate-100 pb-4">
          <div class="flex items-center gap-3">
            <el-avatar :src="detailPost.authorAvatar || undefined" :size="42">
              {{ (detailPost.authorName || '?').slice(0, 1) }}
            </el-avatar>
            <div>
              <div class="font-bold text-slate-900 text-base">{{ detailPost.authorName }}</div>
              <div class="text-xs text-slate-500">User ID: {{ detailPost.userId }} · 发布于 {{ detailPost.createTime }}</div>
            </div>
          </div>
          <el-tag :type="getStatusTag(detailPost.status)">
            {{ getStatusText(detailPost.status) }}
          </el-tag>
        </div>

        <!-- 2. 帖子标题与话题标签 -->
        <div class="space-y-2">
          <h2 class="text-lg font-bold text-slate-900">{{ detailPost.title }}</h2>
          <div class="flex items-center gap-2 flex-wrap">
            <el-tag v-if="detailPost.topicName" type="primary" effect="light" size="small">
              # {{ detailPost.topicName }}
            </el-tag>
            <el-tag v-for="tag in (detailPost.tags || [])" :key="tag" type="info" size="small">
              {{ tag }}
            </el-tag>
          </div>
        </div>

        <!-- 3. 完整正文内容 -->
        <div class="p-4 rounded-lg bg-slate-50 border border-slate-200/80 text-slate-700 text-sm leading-relaxed whitespace-pre-wrap max-h-60 overflow-y-auto">
          {{ detailPost.content }}
        </div>

        <!-- 4. 媒体信息 (图片全屏预览网格) -->
        <div class="space-y-2">
          <span class="text-xs font-semibold text-slate-500 uppercase tracking-wider block">
            媒体附件照片 ({{ (detailPost.images || []).length }})
          </span>
          <div v-if="detailPost.images && detailPost.images.length > 0" class="grid grid-cols-3 gap-3">
            <el-image
              v-for="(img, idx) in detailPost.images"
              :key="idx"
              :src="img"
              :preview-src-list="detailPost.images"
              :initial-index="idx"
              fit="cover"
              class="w-full h-28 rounded-lg border border-slate-200 shadow-xs cursor-pointer hover:opacity-90 transition-opacity"
            />
          </div>
          <div v-else class="text-xs text-slate-400 bg-slate-50 p-3 rounded-lg text-center">暂无媒体图文附件</div>
        </div>

        <!-- 5. 互动底栏数据 -->
        <div class="flex items-center justify-around p-3 rounded-lg bg-blue-50/50 border border-blue-100 text-xs text-slate-600">
          <div>浏览量: <span class="font-bold text-blue-600 text-sm ml-1">{{ detailPost.viewCount }}</span></div>
          <div>点赞数: <span class="font-bold text-blue-600 text-sm ml-1">{{ detailPost.likeCount }}</span></div>
          <div>评论数: <span class="font-bold text-blue-600 text-sm ml-1">{{ detailPost.commentCount }}</span></div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 驳回理由悬浮弹窗 -->
    <el-dialog v-model="rejectDialogVisible" title="填写不通过理由" width="460px">
      <div class="space-y-3">
        <p class="text-sm text-slate-600">请选择或输入驳回帖子 <strong>[ {{ activePost?.title }} ]</strong> 的理由：</p>
        <el-select v-model="quickReason" placeholder="快捷常用理由" class="w-full" @change="onQuickReasonSelect">
          <el-option label="包含违规违法/敏感内容" value="包含违规违法/敏感内容" />
          <el-option label="广告营销/垃圾推广" value="广告营销/垃圾推广" />
          <el-option label="涉嫌抄袭侵权" value="涉嫌抄袭侵权" />
          <el-option label="低俗不雅内容" value="低俗不雅内容" />
        </el-select>
        <el-input
          v-model="rejectReasonInput"
          type="textarea"
          :rows="4"
          placeholder="请输入详细的驳回不通过理由..."
        />
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="danger" :disabled="!rejectReasonInput.trim()" @click="confirmReject">
            确认驳回
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 搭配多条件高级筛选弹窗 (参考用户管理) -->
    <el-dialog v-model="openFilterDialog" title="高级组合筛选帖子" width="680px">
      <div class="grid grid-cols-2 gap-4">
        <el-input v-model="filterForm.keyword" placeholder="标题/内容关键字" clearable />
        <el-input v-model="filterForm.topicName" placeholder="话题名称" clearable />

        <el-select v-model="filterForm.status" placeholder="审核状态" clearable>
          <el-option label="审核通过 (正常)" :value="1" />
          <el-option label="已驳回 (不通过)" :value="0" />
          <el-option label="待审核" :value="2" />
        </el-select>

        <el-input v-model.number="filterForm.authorId" placeholder="作者 User ID" clearable />

        <div class="flex items-center gap-2">
          <el-input-number v-model="filterForm.minLikes" :min="0" placeholder="最低点赞数" class="w-full" />
        </div>
        <div class="flex items-center gap-2">
          <el-input-number v-model="filterForm.minComments" :min="0" placeholder="最低评论数" class="w-full" />
        </div>
      </div>

      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="resetFilter">重置</el-button>
          <el-button type="primary" @click="applyFilter">应用筛选</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { PostAPI, type PostItem, type PostQueryPayload } from '@/services/post'

const list = ref<PostItem[]>([])
const loading = ref(false)
const total = ref(0)

const pagination = reactive({ page: 1, size: 10 })
const openFilterDialog = ref(false)

const filterForm = reactive<{
  keyword?: string
  topicName?: string
  status?: number
  authorId?: number
  minLikes?: number
  minComments?: number
}>({})

// 详情悬浮框弹窗
const detailDialogVisible = ref(false)
const detailPost = ref<PostItem | null>(null)

// 驳回弹窗控制
const rejectDialogVisible = ref(false)
const activePost = ref<PostItem | null>(null)
const rejectReasonInput = ref('')
const quickReason = ref('')

const getStatusText = (status: number) => {
  if (status === 1) return '通过 (正常)'
  if (status === 0) return '已驳回'
  return '待审核'
}

const getStatusTag = (status: number) => {
  if (status === 1) return 'success'
  if (status === 0) return 'danger'
  return 'warning'
}

const openDetailModal = (row: PostItem) => {
  detailPost.value = row
  detailDialogVisible.value = true
}

const fetchData = async () => {
  loading.value = true
  try {
    const payload: PostQueryPayload = {
      page: pagination.page,
      size: pagination.size,
      ...filterForm
    }
    const res = await PostAPI.getPostList(payload)
    list.value = res.data?.list || [
      {
        id: 501,
        userId: 10086,
        authorName: '极客小张',
        authorAvatar: '',
        title: '分享一个超实用 Spring Boot Starter 开发指南',
        content: '今天探讨如何将常用公共组件解耦封装为自动装配的 Starter...\n1. 创建根聚合 Starter 工程\n2. 配置 AutoConfiguration\n3. 注册 AutoConfiguration.imports\n实现完全无侵入的监控增强！',
        topicName: 'Java后端',
        tags: ['SpringBoot', '架构设计', '解耦'],
        images: [
          'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500&auto=format&fit=crop&q=60',
          'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=500&auto=format&fit=crop&q=60'
        ],
        viewCount: 1280,
        likeCount: 96,
        commentCount: 34,
        status: 1,
        createTime: '2026-08-16 10:20'
      },
      {
        id: 502,
        userId: 10087,
        authorName: '测试违规用户',
        authorAvatar: '',
        title: '【推广】点击免费领取大额优惠券',
        content: '点击链接免费获得好礼...',
        topicName: '福利区',
        tags: ['广告'],
        images: [],
        viewCount: 45,
        likeCount: 0,
        commentCount: 1,
        status: 0,
        rejectReason: '包含违规违法/敏感内容',
        createTime: '2026-08-16 11:05'
      },
      {
        id: 503,
        userId: 10088,
        authorName: '前端小能手',
        authorAvatar: '',
        title: 'Vue 3 + Tailwind CSS 响应式布局实战总结',
        content: '记录管理端框架升级流程...',
        topicName: '前端开发',
        tags: ['Vue3', 'TailwindCSS'],
        images: ['https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=500&auto=format&fit=crop&q=60'],
        viewCount: 340,
        likeCount: 22,
        commentCount: 5,
        status: 2,
        createTime: '2026-08-16 14:00'
      }
    ]
    total.value = res.data?.total || list.value.length
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

// 审核通过 Toggle
const handlePass = async (row: PostItem) => {
  try {
    await PostAPI.togglePostStatus(row.id, 1)
    fetchData()
  } catch {
    // 拦截器处理
  }
}

// 打开驳回弹窗
const openRejectModal = (row: PostItem) => {
  activePost.value = row
  rejectReasonInput.value = ''
  quickReason.value = ''
  rejectDialogVisible.value = true
}

const onQuickReasonSelect = (val: string) => {
  rejectReasonInput.value = val
}

// 确认驳回
const confirmReject = async () => {
  if (!activePost.value || !rejectReasonInput.value.trim()) return
  try {
    await PostAPI.togglePostStatus(activePost.value.id, 0, rejectReasonInput.value.trim())
    rejectDialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleDelete = (row: PostItem) => {
  ElMessageBox.confirm(`确定要彻底删除帖子 [${row.title}] 吗？`, '删除确认', { type: 'error' }).then(async () => {
    try {
      await PostAPI.deletePost(row.id)
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

const applyFilter = () => {
  pagination.page = 1
  openFilterDialog.value = false
  fetchData()
}

const resetFilter = () => {
  Object.keys(filterForm).forEach(k => delete (filterForm as any)[k])
  pagination.page = 1
  openFilterDialog.value = false
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>
