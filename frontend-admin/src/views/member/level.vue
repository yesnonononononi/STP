<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <!-- 头部卡片 -->
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">会员等级配置</h3>
        <p class="text-sm text-slate-500 mt-1">配置各 VIP 等级梯度、最低充值门槛及特权列表说明。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 新增等级配置
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="配置 ID" width="140">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-600 font-medium">{{ row.id || '-' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="level" label="VIP 等级" width="110">
          <template #default="{ row }">
            <el-tag type="primary" class="font-bold">VIP {{ row.level }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="levelName" label="等级名称" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <img v-if="row.iconUrl" :src="row.iconUrl" class="w-5 h-5 object-contain" />
              <span class="font-semibold text-slate-800">{{ row.levelName }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="minRecharge" label="最低累计充值门槛" min-width="160">
          <template #default="{ row }">
            <span class="font-mono text-emerald-600 font-bold">￥{{ row.minRecharge?.toFixed?.(2) ?? row.minRecharge }}</span>
          </template>
        </el-table-column>

        <el-table-column label="VIP 专属特权" min-width="260">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1.5 py-1">
              <template v-if="parsePrivileges(row.privilegesJson).length > 0">
                <el-tag
                  v-for="(p, idx) in parsePrivileges(row.privilegesJson)"
                  :key="idx"
                  size="small"
                  :type="p.enabled ? 'success' : 'info'"
                  class="text-xs"
                >
                  {{ p.name }}<span v-if="p.description" class="opacity-75">({{ p.description }})</span>
                </el-tag>
              </template>
              <span v-else class="text-slate-400 text-xs">暂无配置特权</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="sortOrder" label="排序权重" width="110">
          <template #default="{ row }">
            <span class="font-mono text-slate-600">{{ row.sortOrder }}</span>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-button size="small" type="primary" plain @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="danger" plain @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑等级配置' : '新增等级配置'" width="680px" destroy-on-close>
      <el-form :model="form" label-width="110px" class="space-y-4">
        <!-- 基础配置属性 2 列排版 -->
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-4">
          <el-form-item label="VIP 等级" required class="mb-0">
            <el-input-number v-model="form.level" :min="1" :disabled="isEdit" class="w-full" />
          </el-form-item>
          <el-form-item label="等级名称" required class="mb-0">
            <el-input v-model="form.levelName" placeholder="如：黄金 VIP" />
          </el-form-item>
          <el-form-item label="最低充值门槛" required class="mb-0">
            <el-input-number v-model="form.minRecharge" :min="0" :precision="2" :step="100" class="w-full" />
          </el-form-item>
          <el-form-item label="排序权重" class="mb-0">
            <el-input-number v-model="form.sortOrder" :min="0" class="w-full" />
          </el-form-item>
          <el-form-item label="图标 URL" class="col-span-2 mb-0">
            <el-input v-model="form.iconUrl" placeholder="https://..." />
          </el-form-item>
        </div>

        <!-- 专属特权列表可视化配置 -->
        <div class="pt-4 border-t border-slate-100">
          <div class="flex items-center justify-between mb-3">
            <div class="flex items-center gap-2">
              <span class="text-sm font-semibold text-slate-800">VIP 专属特权列表</span>
              <el-tag size="small" type="info" round class="text-xs font-normal">共 {{ privilegesList.length }} 项</el-tag>
            </div>
            <el-button type="primary" plain size="small" class="rounded-lg" @click="addPrivilege">
              + 添加特权项
            </el-button>
          </div>

          <!-- 特权卡片列表容器 -->
          <div v-if="privilegesList.length > 0" class="space-y-3 max-h-[300px] overflow-y-auto pr-1">
            <div
              v-for="(item, index) in privilegesList"
              :key="index"
              class="group relative rounded-xl border border-slate-200/90 bg-slate-50/50 p-4 transition-all hover:bg-white hover:shadow-xs hover:border-slate-300 space-y-3"
            >
              <!-- 条目 Header：序号 + 状态 + 删除 -->
              <div class="flex items-center justify-between border-b border-slate-200/60 pb-2">
                <div class="flex items-center gap-2">
                  <span class="inline-flex items-center justify-center w-5 h-5 rounded-full bg-slate-200/80 text-slate-600 text-xs font-bold font-mono">
                    {{ index + 1 }}
                  </span>
                  <span class="text-xs font-medium text-slate-700">特权配置</span>
                </div>
                <div class="flex items-center gap-4">
                  <div class="flex items-center gap-1.5">
                    <span class="text-xs text-slate-500">启用状态</span>
                    <el-switch v-model="item.enabled" size="small" />
                  </div>
                  <el-button
                    type="danger"
                    link
                    size="small"
                    class="text-rose-500 hover:text-rose-600 font-normal text-xs"
                    @click="removePrivilege(index)"
                  >
                    删除
                  </el-button>
                </div>
              </div>

              <!-- 条目 Body：特权名称 + 特权描述 -->
              <div class="grid grid-cols-1 sm:grid-cols-2 gap-3">
                <div>
                  <label class="block text-xs font-medium text-slate-600 mb-1">特权名称 <span class="text-rose-500">*</span></label>
                  <el-input v-model="item.name" placeholder="如: 专享95折 / 专属挂件" clearable class="w-full" />
                </div>
                <div>
                  <label class="block text-xs font-medium text-slate-600 mb-1">特权描述 / 提示信息</label>
                  <el-input v-model="item.description" placeholder="如: 全场商品享 9.5 折优惠" clearable class="w-full" />
                </div>
              </div>
            </div>
          </div>

          <!-- 空状态提示 -->
          <div
            v-else
            class="flex flex-col items-center justify-center p-6 border-2 border-dashed border-slate-200 rounded-xl bg-slate-50/50 text-slate-400 hover:border-slate-300 cursor-pointer transition-colors"
            @click="addPrivilege"
          >
            <span class="text-sm text-slate-500 font-medium">暂无特权配置</span>
            <span class="text-xs text-slate-400 mt-1">点击此处添加此 VIP 等级的专属特权</span>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3 pt-2 border-t border-slate-100">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" class="px-6" @click="handleSave">保存配置</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { MemberAPI, type MemberLevelConfigItem, type PrivilegeItem } from '@/services/member'

const list = ref<MemberLevelConfigItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const privilegesList = ref<PrivilegeItem[]>([])

const form = reactive<{
  id?: number | string
  level: number
  levelName: string
  minRecharge: number
  iconUrl: string
  sortOrder: number
}>({
  id: undefined,
  level: 1,
  levelName: '',
  minRecharge: 0,
  iconUrl: '',
  sortOrder: 0
})

// 解析后端返回的特权结构（字符串/数组兼容）
const parsePrivileges = (raw: any): PrivilegeItem[] => {
  if (!raw) return []
  if (Array.isArray(raw)) return raw
  if (typeof raw === 'string') {
    try {
      const parsed = JSON.parse(raw)
      return Array.isArray(parsed) ? parsed : []
    } catch {
      return [{ name: raw, description: '', enabled: true }]
    }
  }
  return []
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await MemberAPI.getLevelConfigList()
    const rawData = res.data
    const arrayData = Array.isArray(rawData) ? rawData : (rawData as any)?.data
    list.value = Array.isArray(arrayData) ? arrayData : [
      {
        id: '1001',
        level: 1,
        levelName: '普通会员',
        minRecharge: 0,
        privilegesJson: [
          { name: '基础功能', description: '享平台基础体验', enabled: true }
        ],
        iconUrl: '',
        sortOrder: 1
      },
      {
        id: '1002',
        level: 2,
        levelName: '黄金 VIP',
        minRecharge: 200,
        privilegesJson: [
          { name: '9.5折优惠', description: '消费享95折', enabled: true },
          { name: '专属挂件', description: '可佩戴挂件', enabled: true }
        ],
        iconUrl: '',
        sortOrder: 2
      }
    ]
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  isEdit.value = false
  Object.assign(form, { id: undefined, level: 1, levelName: '', minRecharge: 0, iconUrl: '', sortOrder: 0 })
  privilegesList.value = [
    { name: '', description: '', enabled: true }
  ]
  dialogVisible.value = true
}

const handleEdit = (row: MemberLevelConfigItem) => {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    level: row.level,
    levelName: row.levelName,
    minRecharge: row.minRecharge,
    iconUrl: row.iconUrl || '',
    sortOrder: row.sortOrder || 0
  })
  privilegesList.value = parsePrivileges(row.privilegesJson)
  dialogVisible.value = true
}

const addPrivilege = () => {
  privilegesList.value.push({ name: '', description: '', enabled: true })
}

const removePrivilege = (index: number) => {
  privilegesList.value.splice(index, 1)
}

const handleSave = async () => {
  if (!form.levelName.trim()) {
    ElMessage.warning('请输入等级名称')
    return
  }
  try {
    const activePrivileges = privilegesList.value.filter(p => p.name.trim() !== '')
    const payload = {
      ...form,
      privilegesJson: JSON.stringify(activePrivileges)
    }
    if (isEdit.value) {
      await MemberAPI.updateLevelConfig(payload)
      ElMessage.success('更新等级配置成功')
    } else {
      await MemberAPI.saveLevelConfig(payload)
      ElMessage.success('保存等级配置成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleDelete = (row: MemberLevelConfigItem) => {
  ElMessageBox.confirm(`确定删除 VIP 等级 ${row.level} [${row.levelName}] 配置吗？`, '警告', { type: 'warning' }).then(async () => {
    try {
      await MemberAPI.deleteLevelConfig(row.level)
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
