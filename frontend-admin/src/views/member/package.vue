<template>
  <div class="space-y-6 animate-fade-in text-slate-800">
    <div class="p-6 rounded-xl border border-slate-200 bg-white shadow-xs flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div>
        <h3 class="text-xl font-semibold text-slate-800">会员套餐配置</h3>
        <p class="text-sm text-slate-500 mt-1">配置会员售卖套餐（月卡、季卡、年卡）、定价、有效时长及库存数量。</p>
      </div>

      <div class="flex items-center gap-3">
        <el-button type="primary" class="admin-btn-primary" @click="handleOpenAdd">
          + 新增套餐
        </el-button>
        <el-button class="admin-btn-secondary" @click="fetchData">刷新</el-button>
      </div>
    </div>

    <!-- 数据列表 -->
    <div class="rounded-xl border border-slate-200 bg-white shadow-xs overflow-hidden p-6">
      <el-table v-loading="loading" :data="list" class="admin-table-custom" style="width: 100%">
        <el-table-column prop="id" label="套餐 ID" width="90" />

        <el-table-column prop="name" label="套餐名称" min-width="160">
          <template #default="{ row }">
            <span class="font-semibold text-slate-800">{{ row.name }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="price" label="售价" min-width="120">
          <template #default="{ row }">
            <span class="font-bold text-rose-600 font-mono">￥{{ row.price?.toFixed?.(2) ?? row.price }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="duration" label="有效时长" width="120">
          <template #default="{ row }">
            <el-tag type="info">{{ row.duration }} 天</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="typeName" label="关联类型" width="140">
          <template #default="{ row }">
            <span class="font-mono text-slate-600">{{ row.typeName || ('Type: ' + row.typeId) }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="stock" label="库存数量" width="120">
          <template #default="{ row }">
            <span class="font-mono" :class="row.stock < 50 ? 'text-amber-600 font-bold' : 'text-slate-700'">
              {{ row.stock }}
            </span>
          </template>
        </el-table-column>

        <el-table-column prop="discount" label="折扣比例" width="110">
          <template #default="{ row }">
            <span>{{ row.discount ? `${row.discount} 折` : '无' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="套餐描述" min-width="200" />

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

    <!-- 对话框 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑会员套餐' : '新增会员套餐'" width="540px" destroy-on-close>
      <el-form :model="form" label-width="120px" class="space-y-4">
        <el-form-item label="套餐名称" required>
          <el-input v-model="form.name" placeholder="如：VIP 连续包月套餐" />
        </el-form-item>
        <el-form-item label="售价" required>
          <el-input-number v-model="form.price" :min="0" :precision="2" class="w-full" />
        </el-form-item>
        <el-form-item label="有效天数" required>
          <el-input-number v-model="form.duration" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="会员类型" required>
          <el-input-number v-model="form.typeId" :min="1" class="w-full" />
        </el-form-item>
        <el-form-item label="库存数量" required>
          <el-input-number v-model="form.stock" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="折扣">
          <el-input-number v-model="form.discount" :min="0.1" :max="10" :precision="1" class="w-full" placeholder="如 8.5" />
        </el-form-item>
        <el-form-item label="排序优先级">
          <el-input-number v-model="form.priority" :min="0" class="w-full" />
        </el-form-item>
        <el-form-item label="套餐描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="详细套餐权益说明..." />
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
import { MemberAPI, type MemberPackageItem } from '@/services/member'

const list = ref<MemberPackageItem[]>([])
const loading = ref(false)
const dialogVisible = ref(false)

const form = reactive<MemberPackageItem>({
  id: undefined,
  name: '',
  price: 25.0,
  duration: 30,
  description: '',
  typeId: 1,
  stock: 999,
  discount: 10,
  priority: 1
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await MemberAPI.getPackageList()
    list.value = res.data || [
      { id: 1, name: 'VIP 连续包月套餐', price: 25.0, duration: 30, description: '自动续费，随时可退', typeId: 1, typeName: '月卡', stock: 9999, discount: 8.5, priority: 1, createTime: '2026-01-01' },
      { id: 2, name: 'VIP 年度至尊套餐', price: 198.0, duration: 365, description: '超值年度优惠，畅享全年特权', typeId: 2, typeName: '年卡', stock: 500, discount: 7.0, priority: 2, createTime: '2026-01-01' }
    ]
  } catch {
    // 拦截器处理
  } finally {
    loading.value = false
  }
}

const handleOpenAdd = () => {
  Object.assign(form, { id: undefined, name: '', price: 25.0, duration: 30, description: '', typeId: 1, stock: 999, discount: 10, priority: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: MemberPackageItem) => {
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleSave = async () => {
  try {
    await MemberAPI.savePackage(form)
    dialogVisible.value = false
    fetchData()
  } catch {
    // 拦截器处理
  }
}

const handleDelete = (row: MemberPackageItem) => {
  ElMessageBox.confirm(`确定删除套餐 [${row.name}] 吗？`, '警告', { type: 'warning' }).then(async () => {
    if (row.id) {
      await MemberAPI.deletePackage(row.id)
      fetchData()
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
})
</script>
