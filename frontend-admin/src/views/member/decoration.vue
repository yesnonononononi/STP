<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">会员个性装饰</h3>
        <p class="text-sm text-slate-500 mt-1">管理会员专属头像框、个人主页卡片背景及挂件皮肤。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 新增装饰
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 列表展示卡片 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="ID" width="90" />

        <el-table-column label="预览" width="100">
          <template #default="{ row }">
            <div class="w-12 h-12 rounded-lg bg-slate-100 border border-slate-200 flex items-center justify-center overflow-hidden">
              <img v-if="row.previewUrl" :src="row.previewUrl" class="w-full h-full object-cover" />
              <span v-else class="text-xs text-slate-400">无图</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="装饰名称" min-width="160">
          <template #default="{ row }">
            <span class="font-semibold text-slate-800">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column label="装饰类型" width="130">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ getTypeName(row.type) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="requiredLevel" label="解锁所需等级" min-width="140">
          <template #default="{ row }">
            <span class="text-slate-700 font-medium">VIP {{ row.requiredLevel }} 以上</span>
          </template>
        </el-table-column>

        <el-table-column prop="validDays" label="天数限制" width="130">
          <template #default="{ row }">
            <span>{{ row.validDays === -1 ? '永久有效' : `${row.validDays} 天` }}</span>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? '已上架' : '已下架' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" plain @click="handleToggleStatus(row)">
                {{ row.status === 1 ? '下架' : '上架' }}
              </el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑装饰' : '新增装饰'" width="520px">
      <el-form :model="form" label-width="110px" class="space-y-4">
        <el-form-item label="装饰名称" required>
          <el-input v-model="form.name" placeholder="如：璀璨星空头像框" />
        </el-form-item>
        <el-form-item label="装饰类型" required>
          <el-select v-model="form.type" class="w-full">
            <el-option label="头像框" value="AVATAR_FRAME" />
            <el-option label="主页卡片背景" value="CARD_BG" />
            <el-option label="专属挂件" value="PENDANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="预览图 URL">
          <el-input v-model="form.previewUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="要求最低VIP" required>
          <el-input-number v-model="form.requiredLevel" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="有效天数">
          <el-input-number v-model="form.validDays" :min="-1" placeholder="-1为永久" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSave">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox } from 'element-plus'
import { MemberAPI, type MemberDecorationItem } from '@/services/member'

const list = ref<MemberDecorationItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)

const form = reactive<Partial<MemberDecorationItem>>({
  id: undefined,
  name: '',
  type: 'AVATAR_FRAME',
  previewUrl: '',
  requiredLevel: 1,
  validDays: 30,
  status: 1
})

const getTypeName = (type?: string) => {
  if (type === 'AVATAR_FRAME') return '头像框'
  if (type === 'CARD_BG') return '主页背景'
  if (type === 'PENDANT') return '挂件'
  return '未知'
}

const getTypeTag = (type?: string) => {
  if (type === 'AVATAR_FRAME') return 'primary'
  if (type === 'CARD_BG') return 'success'
  return 'warning'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await MemberAPI.getDecorationList()
    list.value = res.data || [
      { id: 101, name: '王者金辉头像框', type: 'AVATAR_FRAME', previewUrl: '', requiredLevel: 2, validDays: 30, status: 1, createTime: '2026-01-01' },
      { id: 102, name: '深邃赛博卡片背景', type: 'CARD_BG', previewUrl: '', requiredLevel: 3, validDays: -1, status: 1, createTime: '2026-01-01' }
    ]
  } catch {
    // 拦截器捕获
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  Object.assign(form, { id: undefined, name: '', type: 'AVATAR_FRAME', previewUrl: '', requiredLevel: 1, validDays: 30, status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: MemberDecorationItem) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await MemberAPI.saveDecoration(form)
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器捕获
  }
}

const handleToggleStatus = async (row: MemberDecorationItem) => {
  const nextStatus = row.status === 1 ? 0 : 1
  try {
    await MemberAPI.toggleDecorationStatus(row.id, nextStatus)
    fetchData()
  } catch {
    // 拦截器捕获
  }
}

const handleDelete = (row: MemberDecorationItem) => {
  ElMessageBox.confirm(`确定删除装饰 [${row.name}] 吗？`, '提示', { type: 'warning' }).then(async () => {
    try {
      await MemberAPI.deleteDecoration(row.id)
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
