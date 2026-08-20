<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <!-- 头部工具栏 -->
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">帖子内容管理</h3>
        <p class="text-sm text-slate-500 mt-1">审核社区帖子、查看元信息详情、切换展示状态及组合筛选内容。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-input v-model="filterForm.keyword" placeholder="搜索帖子 ID/标题/内容..." clearable class="w-64" @keyup.enter="fetchData" />
        <el-button type="primary" class="admin-btn-primary" @click="openFilterDialog = true">
          高级筛选
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <!-- 帖子 ID -->
        <el-table-column prop="id" label="帖子 ID" min-width="180">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-700 font-semibold select-all hover:text-blue-600 cursor-pointer" @click="openDetailModal(row)">
              {{ row.id }}
            </span>
          </template>
        </el-table-column>

        <!-- 发布作者 ID -->
        <el-table-column label="发布作者 ID" min-width="190">
          <template #default="{ row }">
            <div class="flex items-center gap-2.5">
              <el-avatar :size="28" class="bg-gradient-to-tr from-blue-500 to-indigo-600 text-white font-bold text-xs shrink-0">
                {{ String(row.creatorId || '?').slice(0, 1) }}
              </el-avatar>
              <span class="font-mono text-xs text-slate-700 font-medium select-all truncate">
                {{ row.creatorId || '-' }}
              </span>
            </div>
          </template>
        </el-table-column>

        <!-- 帖子标题 / 预览 -->
        <el-table-column label="帖子标题 / 预览" min-width="260">
          <template #default="{ row }">
            <div class="space-y-1 py-1">
              <div class="font-semibold text-slate-800 hover:text-blue-600 cursor-pointer truncate transition-colors" @click="openDetailModal(row)">
                {{ row.title }}
              </div>
              <div class="text-xs text-slate-500 line-clamp-1 leading-relaxed" v-html="row.content || '无正文文本内容' "></div>
            </div>
          </template>
        </el-table-column>

        <!-- 互动数据 -->
        <el-table-column label="互动数据" width="180">
          <template #default="{ row }">
            <div class="text-xs text-slate-600 space-y-1">
              <div class="flex items-center gap-2">
                <span>浏览: <strong class="text-slate-800 font-semibold">{{ row.viewCount ?? 0 }}</strong></span>
                <span>点赞: <strong class="text-blue-600 font-semibold">{{ row.likeCount ?? 0 }}</strong></span>
              </div>
              <div class="flex items-center gap-2">
                <span>回复: <strong class="text-slate-800 font-semibold">{{ row.replyCount ?? 0 }}</strong></span>
                <span>热度: <strong class="text-amber-600 font-semibold">{{ row.hotScore ?? 0 }}</strong></span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 审核 / 状态 -->
        <el-table-column label="审核 / 状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" effect="light" size="small" class="font-medium">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 发布时间 -->
        <el-table-column label="发布时间" min-width="160">
          <template #default="{ row }">
            <span class="text-xs text-slate-500 font-mono">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>

        <!-- 操作栏 -->
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1.5 flex-wrap">
              <!-- 查看详情按钮 (对接 /a/post/detail) -->
              <el-button size="small" type="primary" plain @click="openDetailModal(row)">
                详情
              </el-button>

              <!-- 审核通过 (bypass) -->
              <el-button v-if="Number(row.status) === 7" size="small" type="success" plain @click="handleBypass(row)">
                通过
              </el-button>

              <!-- 审核驳回 (bypassNot) -->
              <el-button v-if="Number(row.status) === 7" size="small" type="warning" plain @click="openRejectModal(row)">
                驳回
              </el-button>

              <!-- 封禁 / 解封 (ban / unban) -->
              <el-button
                v-if="Number(row.status) === 3"
                size="small"
                type="info"
                plain
                @click="handleUnban(row)"
              >
                解封
              </el-button>
              <el-button
                v-else-if="Number(row.status) === 1"
                size="small"
                type="danger"
                plain
                @click="handleBan(row)"
              >
                封禁
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

    <!-- 帖子元信息详情悬浮框 Modal (对齐 /a/post/detail) -->
    <el-dialog v-model="detailDialogVisible" title="帖子元信息详情" width="740px" destroy-on-close class="rounded-2xl">
      <div v-loading="detailLoading" class="space-y-6 text-slate-800 py-2 px-1">
        <template v-if="detailPost">
          <!-- 1. 头部作者与时间状态 -->
          <div class="flex items-center justify-between border-b border-slate-100 pb-5">
            <div class="flex items-center gap-3.5">
              <el-avatar :size="46" :src="detailPost.publisher?.avatar" class="bg-gradient-to-tr from-blue-500 to-indigo-600 text-white font-bold shrink-0 shadow-xs">
                {{ String(detailPost.publisher?.nick || detailPost.creatorId || '?').slice(0, 1) }}
              </el-avatar>
              <div class="space-y-1">
                <div class="font-bold text-slate-900 text-base flex items-center gap-2">
                  <span>{{ detailPost.publisher?.nick || '发布作者' }}</span>
                  <span class="font-mono text-xs text-slate-400 font-normal bg-slate-100 px-2 py-0.5 rounded-md select-all">ID: {{ detailPost.creatorId }}</span>
                </div>
                <div class="text-xs text-slate-500 flex items-center gap-2">
                  <span>发布于 {{ formatTime(detailPost.createTime) }}</span>
                  <span v-if="detailPost.publisher?.ip" class="text-slate-300">·</span>
                  <span v-if="detailPost.publisher?.ip">IP: {{ detailPost.publisher.ip }}</span>
                </div>
              </div>
            </div>
            <el-tag :type="getStatusTag(detailPost.status)" size="large" class="rounded-lg px-3 py-1 font-medium">
              {{ getStatusText(detailPost.status) }}
            </el-tag>
          </div>

          <!-- 2. 帖子 ID & 标题 -->
          <div class="space-y-2 pt-1">
            <div class="flex items-center gap-2">
              <span class="text-xs font-semibold text-slate-400 uppercase tracking-wider">帖子 ID</span>
              <span class="font-mono text-xs text-slate-500 select-all font-medium bg-slate-50 border border-slate-200/60 px-2 py-0.5 rounded">{{ detailPost.id }}</span>
            </div>
            <h2 class="text-xl font-extrabold text-slate-900 leading-snug tracking-tight">{{ detailPost.title }}</h2>
          </div>

          <!-- 3. 完整正文内容 -->
          <div class="p-5 rounded-xl bg-slate-50/90 border border-slate-200/70 text-slate-700 text-sm leading-relaxed whitespace-pre-wrap max-h-64 overflow-y-auto" v-html="detailPost.content || '无正文内容'">
          </div>

          <!-- 4. 媒体信息 (图片全屏预览网格) -->
          <div class="space-y-3 pt-1">
            <div class="flex items-center justify-between">
              <span class="text-xs font-bold text-slate-600 uppercase tracking-wider">
                媒体附件照片 ({{ normalizedMediaList.length }})
              </span>
              <span v-if="normalizedMediaList.length > 0" class="text-xs text-slate-400">点击照片可查看原图大图</span>
            </div>
            <div v-if="normalizedMediaList.length > 0" class="grid grid-cols-3 gap-3.5">
              <el-image
                v-for="(img, idx) in normalizedMediaList"
                :key="img.id || idx"
                :src="img.url"
                :preview-src-list="previewUrlList"
                :initial-index="idx"
                :preview-teleported="true"
                :hide-on-click-modal="true"
                fit="cover"
                class="w-full h-36 rounded-xl border border-slate-200 shadow-xs cursor-pointer hover:opacity-90 transition-all hover:scale-[1.02] duration-200"
              />
            </div>
            <div v-else class="text-xs text-slate-400 bg-slate-50 p-4 rounded-xl text-center border border-slate-100">暂无媒体图文附件</div>
          </div>

          <!-- 5. 互动底栏数据 -->
          <div class="grid grid-cols-4 gap-4 p-4 rounded-xl bg-slate-50 border border-slate-200/70 text-center">
            <div class="space-y-1">
              <div class="text-xs text-slate-400">浏览量</div>
              <div class="font-bold text-slate-800 text-base font-mono">{{ detailPost.viewCount ?? 0 }}</div>
            </div>
            <div class="space-y-1 border-l border-slate-200/60">
              <div class="text-xs text-slate-400">点赞数</div>
              <div class="font-bold text-blue-600 text-base font-mono">{{ detailPost.likeCount ?? 0 }}</div>
            </div>
            <div class="space-y-1 border-l border-slate-200/60">
              <div class="text-xs text-slate-400">回复数</div>
              <div class="font-bold text-indigo-600 text-base font-mono">{{ detailPost.replyCount ?? 0 }}</div>
            </div>
            <div class="space-y-1 border-l border-slate-200/60">
              <div class="text-xs text-slate-400">收藏数</div>
              <div class="font-bold text-amber-600 text-base font-mono">{{ detailPost.collectCount ?? 0 }}</div>
            </div>
          </div>
        </template>
      </div>
      <template #footer>
        <div class="flex justify-end pt-2">
          <el-button size="large" class="px-6 rounded-lg" @click="detailDialogVisible = false">关闭</el-button>
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

    <!-- 搭配多条件高级筛选弹窗 -->
    <el-dialog v-model="openFilterDialog" title="高级组合筛选帖子" width="680px">
      <div class="grid grid-cols-2 gap-4">
        <el-input v-model="filterForm.keyword" placeholder="标题/内容关键字" clearable />
        <el-input v-model="filterForm.creatorId" placeholder="创作者 ID (文本/字符串)" clearable />
        <el-input v-model="filterForm.postId" placeholder="帖子 ID (文本/字符串)" clearable />

        <el-select v-model="filterForm.status" placeholder="审核状态" clearable>
          <el-option label="正常/审核通过 (1)" value="1" />
          <el-option label="已删除 (2)" value="2" />
          <el-option label="已封禁/被屏蔽 (3)" value="3" />
          <el-option label="被举报 (4)" value="4" />
          <el-option label="草稿 (6)" value="6" />
          <el-option label="待审核 (7)" value="7" />
          <el-option label="审核未通过 (8)" value="8" />
        </el-select>

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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { PostAPI, type AdminPostVO, type PostVO, type PostQueryPayload } from '@/services/post'

const list = ref<AdminPostVO[]>([])
const loading = ref(false)
const total = ref(0)

const pagination = reactive({ page: 1, size: 10 })
const openFilterDialog = ref(false)

const filterForm = reactive<{
  keyword?: string
  status?: string | number
  creatorId?: string
  postId?: string
  minLikes?: number
  minComments?: number
}>({})

// 详情悬浮框弹窗及动态详情接口状态 (/a/post/detail)
const detailDialogVisible = ref(false)
const detailLoading = ref(false)
const detailPost = ref<PostVO | null>(null)

// 驳回弹窗控制
const rejectDialogVisible = ref(false)
const activePost = ref<AdminPostVO | null>(null)
const rejectReasonInput = ref('')
const quickReason = ref('')

// 对齐后端 PostStatus.java: 1-NORMAL, 2-DELETED, 3-BLOCKED, 4-REPORTED, 5-UNKNOWN, 6-DRAFT, 7-CHECK, 8-UNPASS
const getStatusText = (status?: string | number) => {
  const code = Number(status)
  if (code === 1) return '正常/审核通过'
  if (code === 2) return '已删除'
  if (code === 3) return '已封禁'
  if (code === 4) return '被举报'
  if (code === 6) return '草稿'
  if (code === 7) return '待审核'
  if (code === 8) return '审核未通过'
  return String(status ?? '未知')
}

const getStatusTag = (status?: string | number) => {
  const code = Number(status)
  if (code === 1) return 'success'
  if (code === 3 || code === 8) return 'danger'
  if (code === 4 || code === 7) return 'warning'
  return 'info'
}

const formatTime = (time?: string) => {
  if (!time) return '-'
  const d = new Date(time)
  return Number.isNaN(d.getTime()) ? time : d.toLocaleString('zh-CN')
}

interface MediaItem {
  id?: number | string
  url: string
}

// 标准化提炼安全的媒体照片列表 (防崩防 [object Object])
const normalizedMediaList = computed<MediaItem[]>(() => {
  if (!detailPost.value) return []
  const raw = detailPost.value.mediaUrls || (detailPost.value as any).urls
  if (!raw) return []

  let listArr: any[] = []
  if (Array.isArray(raw)) {
    listArr = raw
  } else if (typeof raw === 'string' && raw.trim()) {
    try {
      const parsed = JSON.parse(raw)
      if (Array.isArray(parsed)) {
        listArr = parsed
      } else {
        listArr = raw.split(',').map(s => s.trim()).filter(Boolean)
      }
    } catch {
      listArr = raw.split(',').map(s => s.trim()).filter(Boolean)
    }
  }

  return listArr.map((item, index) => {
    if (typeof item === 'string') {
      return { id: index, url: item }
    }
    const url = item.imageUrl || item.url || (typeof item === 'object' ? String(item) : '')
    return { id: item.id || index, url }
  }).filter(item => item.url && item.url !== '[object Object]')
})

const previewUrlList = computed<string[]>(() => {
  return normalizedMediaList.value.map(item => item.url)
})

// 调起后端详情接口 /a/post/detail
const openDetailModal = async (row: AdminPostVO) => {
  detailDialogVisible.value = true
  detailLoading.value = true
  try {
    const res = await PostAPI.getPostDetail(row.id, Number(row.status))
    if (res.code === 1 && res.data) {
      detailPost.value = res.data
    } else {
      // 降级构建
      detailPost.value = {
        id: row.id,
        creatorId: row.creatorId,
        title: row.title,
        content: row.content,
        status: row.status,
        createTime: row.createTime,
        viewCount: row.viewCount,
        likeCount: row.likeCount,
        replyCount: row.replyCount,
        collectCount: row.collectCount
      }
    }
  } catch (err) {
    console.error('获取帖子详情失败:', err)
  } finally {
    detailLoading.value = false
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const payload: PostQueryPayload = {
      page: pagination.page,
      pageSize: pagination.size,
      keyword: filterForm.keyword?.trim() || undefined,
      status: filterForm.status,
      creatorId: filterForm.creatorId,
      postId: filterForm.postId,
      likeCount: filterForm.minLikes != null ? { min: filterForm.minLikes } : undefined,
      comment: filterForm.minComments != null ? { min: filterForm.minComments } : undefined,
    }
    const res = await PostAPI.getPostList(payload)
    if (res.code === 1 && res.data) {
      list.value = res.data.data || []
      total.value = Number(res.data.total) || 0
    }
  } catch {
    // 拦截器统一处理
  } finally {
    loading.value = false
  }
}

// 审核通过 (bypass)
const handleBypass = async (row: AdminPostVO) => {
  try {
    await PostAPI.bypass(row.id)
    ElMessage.success('审核通过成功')
    fetchData()
  } catch {
    // 拦截器处理
  }
}

// 打开驳回弹窗 (bypassNot)
const openRejectModal = (row: AdminPostVO) => {
  activePost.value = row
  rejectReasonInput.value = ''
  quickReason.value = ''
  rejectDialogVisible.value = true
}

const onQuickReasonSelect = (val: string) => {
  rejectReasonInput.value = val
}

// 确认驳回 (bypassNot)
const confirmReject = async () => {
  if (!activePost.value || !rejectReasonInput.value.trim()) return
  try {
    await PostAPI.bypassNot(activePost.value.id, rejectReasonInput.value.trim())
    ElMessage.success('已处理驳回')
    rejectDialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

// 封禁帖子 (ban)
const handleBan = async (row: AdminPostVO) => {
  ElMessageBox.confirm(`确定要封禁帖子 [${row.title}] 吗？`, '封禁确认', { type: 'warning' }).then(async () => {
    try {
      await PostAPI.ban(row.id)
      ElMessage.success('已成功封禁该帖子')
      fetchData()
    } catch {
      // 拦截器处理
    }
  }).catch(() => {})
}

// 解封帖子 (unban)
const handleUnban = async (row: AdminPostVO) => {
  ElMessageBox.confirm(`确定要解封帖子 [${row.title}] 吗？`, '解封确认', { type: 'info' }).then(async () => {
    try {
      await PostAPI.unban(row.id)
      ElMessage.success('已成功解封该帖子')
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
