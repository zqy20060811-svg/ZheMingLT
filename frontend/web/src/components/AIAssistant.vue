<template>
  <div class="ai-assistant">
    <div class="ai-header" @click="toggleExpand">
      <div class="ai-icon">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"/>
        </svg>
      </div>
      <span class="ai-title">AI 助手</span>
      <span class="ai-toggle">{{ isExpanded ? '▼' : '▶' }}</span>
    </div>

    <div v-if="isExpanded" class="ai-content">
      <!-- 回复建议 -->
      <div v-if="type === 'reply' && suggestions.length > 0" class="ai-section">
        <h4>💡 回复建议</h4>
        <div class="suggestions-list">
          <div
            v-for="(suggestion, index) in suggestions"
            :key="index"
            class="suggestion-item"
            @click="useSuggestion(suggestion)"
          >
            {{ suggestion }}
          </div>
        </div>
      </div>

      <!-- 内容总结 -->
      <div v-if="type === 'summary' && summary" class="ai-section">
        <h4>📝 内容总结</h4>
        <p class="summary-text">{{ summary }}</p>
      </div>

      <!-- 标签推荐 -->
      <div v-if="type === 'tags' && recommendedTags.length > 0" class="ai-section">
        <h4>🏷️ 推荐标签</h4>
        <div class="tags-list">
          <span
            v-for="tag in recommendedTags"
            :key="tag"
            class="tag-item"
            @click="selectTag(tag)"
          >
            {{ tag }}
          </span>
        </div>
      </div>

      <!-- 标题建议 -->
      <div v-if="type === 'title' && suggestedTitle" class="ai-section">
        <h4>📌 标题建议</h4>
        <p class="title-suggestion" @click="useTitle(suggestedTitle)">
          {{ suggestedTitle }}
        </p>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="ai-loading">
        <span class="loading-spinner"></span>
        <span>AI 思考中...</span>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="ai-error">
        {{ error }}
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { aiService } from '@/services/aiService'

const props = defineProps({
  type: {
    type: String,
    default: 'reply', // reply, summary, tags, title
    validator: (value) => ['reply', 'summary', 'tags', 'title'].includes(value)
  },
  content: {
    type: String,
    default: ''
  },
  title: {
    type: String,
    default: ''
  },
  existingComments: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['select-suggestion', 'select-tag', 'use-title'])

const isExpanded = ref(false)
const loading = ref(false)
const error = ref('')

const suggestions = ref([])
const summary = ref('')
const recommendedTags = ref([])
const suggestedTitle = ref('')

const toggleExpand = () => {
  isExpanded.value = !isExpanded.value
  if (isExpanded.value && !hasData()) {
    loadAIContent()
  }
}

const hasData = () => {
  switch (props.type) {
    case 'reply':
      return suggestions.value.length > 0
    case 'summary':
      return !!summary.value
    case 'tags':
      return recommendedTags.value.length > 0
    case 'title':
      return !!suggestedTitle.value
    default:
      return false
  }
}

const loadAIContent = async () => {
  if (!props.content && props.type !== 'title') return

  loading.value = true
  error.value = ''

  try {
    switch (props.type) {
      case 'reply':
        const replyRes = await aiService.generateReplySuggestions(
          props.content,
          props.existingComments
        )
        if (replyRes.code === 200) {
          suggestions.value = replyRes.data
        }
        break

      case 'summary':
        const summaryRes = await aiService.summarizePost(props.content)
        if (summaryRes.code === 200) {
          summary.value = summaryRes.data
        }
        break

      case 'tags':
        const tagsRes = await aiService.recommendTags(props.title, props.content)
        if (tagsRes.code === 200) {
          recommendedTags.value = tagsRes.data
        }
        break

      case 'title':
        const titleRes = await aiService.suggestTitle(props.content)
        if (titleRes.code === 200) {
          suggestedTitle.value = titleRes.data
        }
        break
    }
  } catch (err) {
    error.value = 'AI 服务暂时不可用'
    console.error('AI 服务错误:', err)
  } finally {
    loading.value = false
  }
}

const useSuggestion = (suggestion) => {
  emit('select-suggestion', suggestion)
}

const selectTag = (tag) => {
  emit('select-tag', tag)
}

const useTitle = (title) => {
  emit('use-title', title)
}

// 监听内容变化，重置数据
watch(() => props.content, () => {
  suggestions.value = []
  summary.value = ''
  recommendedTags.value = []
  suggestedTitle.value = ''
  if (isExpanded.value) {
    loadAIContent()
  }
})
</script>

<style scoped>
.ai-assistant {
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  border-radius: 12px;
  overflow: hidden;
  margin: 16px 0;
}

.ai-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: linear-gradient(135deg, var(--primary-color), var(--secondary-color));
  color: white;
  cursor: pointer;
  transition: opacity 0.2s;
}

.ai-header:hover {
  opacity: 0.9;
}

.ai-icon {
  width: 24px;
  height: 24px;
}

.ai-icon svg {
  width: 100%;
  height: 100%;
}

.ai-title {
  flex: 1;
  font-weight: 600;
}

.ai-toggle {
  font-size: 12px;
}

.ai-content {
  padding: 16px;
}

.ai-section {
  margin-bottom: 16px;
}

.ai-section:last-child {
  margin-bottom: 0;
}

.ai-section h4 {
  margin: 0 0 12px 0;
  color: var(--text-primary);
  font-size: 14px;
}

.suggestions-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.suggestion-item {
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  line-height: 1.5;
}

.suggestion-item:hover {
  background: var(--primary-color);
  color: white;
}

.summary-text {
  margin: 0;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  font-size: 14px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.tags-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  padding: 6px 12px;
  background: var(--bg-secondary);
  border-radius: 16px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.tag-item:hover {
  background: var(--primary-color);
  color: white;
}

.title-suggestion {
  margin: 0;
  padding: 12px;
  background: var(--bg-secondary);
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.title-suggestion:hover {
  background: var(--primary-color);
  color: white;
}

.ai-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: var(--text-secondary);
}

.loading-spinner {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border-color);
  border-top-color: var(--primary-color);
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.ai-error {
  padding: 12px;
  background: #fee;
  color: #c33;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}
</style>
