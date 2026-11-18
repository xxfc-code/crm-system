<template>
  <div class="follow">
    <div class="editor">
      <el-input v-model="content" type="textarea" :rows="3" resize="none" placeholder="记录一次沟通，例如：电话沟通，客户关注私有化部署…" />
      <div class="editor-foot">
        <el-date-picker
          v-model="nextTime"
          type="datetime"
          size="small"
          placeholder="下次跟进时间"
          value-format="YYYY-MM-DDTHH:mm:ss"
          style="width: 180px"
        />
        <el-button type="primary" size="small" :loading="saving" @click="submit">添加记录</el-button>
      </div>
    </div>

    <div v-if="list.length" class="timeline">
      <div v-for="f in list" :key="f.id" class="item">
        <div class="avatar">{{ initial(f.createByName) }}</div>
        <div class="main">
          <div class="head">
            <span class="who">{{ f.createByName }}</span>
            <span class="when num">{{ shortTime(f.createTime) }}</span>
          </div>
          <p class="text">{{ f.content }}</p>
          <div v-if="f.nextTime" class="next">下次跟进 · {{ shortTime(f.nextTime) }}</div>
        </div>
      </div>
    </div>
    <el-empty v-else description="还没有跟进记录" :image-size="64" />
  </div>
</template>

<script setup>
import { onMounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { initial, shortTime } from '@/utils/format'

const props = defineProps({
  bizType: { type: String, required: true },
  bizId: { type: [Number, String], required: true }
})

const list = ref([])
const content = ref('')
const nextTime = ref('')
const saving = ref(false)

async function load() {
  if (!props.bizId) return
  list.value = await request.get('/follow/list', { params: { bizType: props.bizType, bizId: props.bizId } })
}

async function submit() {
  if (!content.value.trim()) return
  saving.value = true
  try {
    await request.post('/follow', {
      bizType: props.bizType,
      bizId: props.bizId,
      content: content.value,
      nextTime: nextTime.value || null
    })
    content.value = ''
    nextTime.value = ''
    ElMessage.success('已记录')
    load()
  } finally {
    saving.value = false
  }
}

watch(() => props.bizId, load)
onMounted(load)
</script>

<style scoped>
.editor {
  padding: 12px;
  background: #FDFAFB;
  border: 1px solid var(--line);
  border-radius: 8px;
}

.editor-foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.timeline {
  margin-top: 20px;
  position: relative;
}

.item {
  display: flex;
  gap: 12px;
  padding: 0 0 20px 0;
  position: relative;
}

/* 竖向连接线 */
.item::before {
  content: '';
  position: absolute;
  left: 11px;
  top: 26px;
  bottom: 0;
  width: 1px;
  background: var(--line);
}

.item:last-child::before {
  display: none;
}

.item .main {
  flex: 1;
  padding-top: 2px;
}

.head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.who {
  font-size: 13px;
  color: var(--text);
  font-weight: 500;
}

.when {
  font-size: 12px;
  color: var(--text-3);
}

.text {
  margin-top: 6px;
  font-size: 13px;
  color: var(--text-2);
  line-height: 1.65;
  white-space: pre-wrap;
  word-break: break-word;
}

.next {
  display: inline-block;
  margin-top: 8px;
  padding: 2px 8px;
  font-size: 12px;
  color: var(--warn);
  background: #fdf3ea;
  border-radius: 4px;
}
</style>
