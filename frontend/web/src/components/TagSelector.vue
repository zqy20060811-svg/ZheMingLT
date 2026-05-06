<template>
  <div class="tag-selector" :class="{ 'dark-mode': isDarkMode }">
    <div class="selected-tags">
      <span
        v-for="(tag, index) in modelValue"
        :key="index"
        class="tag-item"
        :style="{ backgroundColor: tagColors[index % tagColors.length] }"
      >
        {{ tag }}
        <i class="bi bi-x" @click="removeTag(index)"></i>
      </span>
      <input
        v-if="modelValue.length < maxTags"
        v-model="inputValue"
        type="text"
        class="tag-input"
        :placeholder="placeholder"
        @keyup.enter="addTag"
        @keyup.space="addTag"
        @keyup.188="addTag"
        @blur="addTag"
      />
    </div>
    <div class="tag-hints">
      <span class="hint-text">按回车或空格添加标签</span>
      <span class="tag-count">{{ modelValue.length }}/{{ maxTags }}</span>
    </div>
    <!-- 热门标签推荐 -->
    <div v-if="hotTags.length > 0" class="hot-tags">
      <span class="hot-label">推荐：</span>
      <span
        v-for="tag in hotTags"
        :key="tag.id"
        class="hot-tag"
        @click="selectHotTag(tag.name)"
      >
        {{ tag.name }}
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { get } from '@/utils/request'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  maxTags: {
    type: Number,
    default: 5
  },
  placeholder: {
    type: String,
    default: '添加标签...'
  }
})

const emit = defineEmits(['update:modelValue'])

const isDarkMode = ref(localStorage.getItem('darkMode') === 'true')
const inputValue = ref('')
const hotTags = ref([])

const tagColors = [
  '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FECA57',
  '#FF9FF3', '#54A0FF', '#48DBFB', '#1DD1A1', '#FFC048'
]

// 加载热门标签
async function loadHotTags() {
  try {
    const res = await get('/tags/hot?limit=8')
    if (res.code === 200) {
      hotTags.value = res.data || []
    }
  } catch (error) {
    console.error('加载热门标签失败', error)
  }
}

function addTag() {
  const value = inputValue.value.trim().replace(/[,，]/g, '')
  if (value && !props.modelValue.includes(value) && props.modelValue.length < props.maxTags) {
    emit('update:modelValue', [...props.modelValue, value])
  }
  inputValue.value = ''
}

function removeTag(index) {
  const newTags = [...props.modelValue]
  newTags.splice(index, 1)
  emit('update:modelValue', newTags)
}

function selectHotTag(tagName) {
  if (!props.modelValue.includes(tagName) && props.modelValue.length < props.maxTags) {
    emit('update:modelValue', [...props.modelValue, tagName])
  }
}

onMounted(() => {
  loadHotTags()
})
</script>

<style scoped>
.tag-selector {
  background: rgba(255, 255, 255, 0.8);
  border-radius: 12px;
  padding: 12px;
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.dark-mode .tag-selector {
  background: rgba(30, 30, 46, 0.8);
}

.tag-selector:focus-within {
  border-color: #667eea;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.selected-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  min-height: 40px;
  align-items: center;
}

.tag-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border-radius: 20px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  animation: tagIn 0.3s ease;
}

@keyframes tagIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.tag-item i {
  cursor: pointer;
  font-size: 14px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.tag-item i:hover {
  opacity: 1;
}

.tag-input {
  flex: 1;
  min-width: 100px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #333;
  outline: none;
  padding: 6px;
}

.dark-mode .tag-input {
  color: #fff;
}

.tag-input::placeholder {
  color: #999;
}

.tag-hints {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}

.hot-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid rgba(0, 0, 0, 0.1);
}

.dark-mode .hot-tags {
  border-top-color: rgba(255, 255, 255, 0.1);
}

.hot-label {
  font-size: 12px;
  color: #999;
  margin-right: 4px;
}

.hot-tag {
  padding: 4px 10px;
  border-radius: 12px;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.hot-tag:hover {
  background: #667eea;
  color: white;
}
</style>
