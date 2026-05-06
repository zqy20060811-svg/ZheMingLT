import request from '@/utils/request'

export const aiService = {
  // 生成回复建议
  generateReplySuggestions(postContent, existingComments = []) {
    return request.post('/ai/reply-suggestions', {
      postContent,
      existingComments
    })
  },

  // 总结帖子内容
  summarizePost(content) {
    return request.post('/ai/summarize', { content })
  },

  // 推荐标签
  recommendTags(title, content) {
    return request.post('/ai/recommend-tags', { title, content })
  },

  // 检测敏感内容
  checkSensitiveContent(content) {
    return request.post('/ai/check-sensitive', { content })
  },

  // 生成标题建议
  suggestTitle(content) {
    return request.post('/ai/suggest-title', { content })
  }
}

export default aiService
