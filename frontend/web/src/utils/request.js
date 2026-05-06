const BASE_URL = '/api'

// Token存储键名
const ACCESS_TOKEN_KEY = 'accessToken'
const REFRESH_TOKEN_KEY = 'refreshToken'

// 获取AccessToken
function getAccessToken() {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

// 获取RefreshToken
function getRefreshToken() {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

// 存储双Token
function setTokens(accessToken, refreshToken) {
  localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
}

// 清除Token
function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem('userId')
}

// 是否正在刷新Token
let isRefreshing = false
// 等待刷新Token的请求队列
let refreshSubscribers = []

// 订阅Token刷新
function subscribeTokenRefresh(callback) {
  refreshSubscribers.push(callback)
}

// 通知所有订阅者新Token
function onTokenRefreshed(newAccessToken) {
  refreshSubscribers.forEach(callback => callback(newAccessToken))
  refreshSubscribers = []
}

// 刷新Token
async function refreshAccessToken() {
  const refreshToken = getRefreshToken()
  
  if (!refreshToken) {
    clearTokens()
    window.location.href = '/login'
    return null
  }

  try {
    const response = await fetch(`${BASE_URL}/users/refresh`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Refresh-Token': `Bearer ${refreshToken}`
      }
    })
    
    const data = await response.json()
    
    if (data.code === 200 && data.data) {
      // 存储新的双Token
      setTokens(data.data.accessToken, data.data.refreshToken)
      return data.data.accessToken
    } else {
      // 刷新失败，清除Token并跳转到登录页
      clearTokens()
      window.location.href = '/login'
      return null
    }
  } catch (error) {
    console.error('刷新Token失败:', error)
    clearTokens()
    window.location.href = '/login'
    return null
  }
}

// 请求封装
async function request(url, options = {}) {
  let accessToken = getAccessToken()
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  }
  
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`
  }
  
  try {
    const response = await fetch(`${BASE_URL}${url}`, {
      ...options,
      headers
    })
    
    const data = await response.json()
    
    // 处理401001 Token过期（需要刷新）
    if (data.code === 401001) {
      // 如果正在刷新，将请求加入队列等待
      if (isRefreshing) {
        return new Promise((resolve) => {
          subscribeTokenRefresh((newAccessToken) => {
            // 使用新Token重试原请求
            headers['Authorization'] = `Bearer ${newAccessToken}`
            fetch(`${BASE_URL}${url}`, {
              ...options,
              headers
            })
              .then(res => res.json())
              .then(resolve)
          })
        })
      }
      
      isRefreshing = true
      
      // 尝试刷新Token
      const newAccessToken = await refreshAccessToken()
      
      isRefreshing = false
      
      if (newAccessToken) {
        // 通知所有等待的请求
        onTokenRefreshed(newAccessToken)
        
        // 使用新Token重试原请求
        headers['Authorization'] = `Bearer ${newAccessToken}`
        const retryResponse = await fetch(`${BASE_URL}${url}`, {
          ...options,
          headers
        })
        return await retryResponse.json()
      } else {
        return data
      }
    }
    
    // 处理401未授权（Token无效或已被拉黑）
    if (data.code === 401 || data.code === 401002 || data.code === 401003) {
      clearTokens()
      window.location.href = '/login'
      return data
    }
    
    // 检查响应头中是否有Token即将过期的标记
    const tokenExpiringSoon = response.headers.get('X-Token-Expiring-Soon')
    if (tokenExpiringSoon === 'true' && !isRefreshing) {
      // 在后台静默刷新Token
      refreshAccessToken().then(newAccessToken => {
        if (newAccessToken) {
          console.log('Token已自动刷新')
        }
      })
    }
    
    return data
  } catch (error) {
    console.error('请求失败:', error)
    return {
      code: 500,
      message: '网络请求失败',
      data: null
    }
  }
}

// GET请求
export function get(url, params = {}) {
  const queryString = new URLSearchParams(params).toString()
  const fullUrl = queryString ? `${url}?${queryString}` : url
  return request(fullUrl, { method: 'GET' })
}

// POST请求
export function post(url, data = {}) {
  return request(url, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

// PUT请求
export function put(url, data = {}) {
  return request(url, {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

// DELETE请求
export function del(url) {
  return request(url, { method: 'DELETE' })
}

// 文件上传请求
export async function upload(url, formData) {
  let accessToken = getAccessToken()
  
  const headers = {}
  
  if (accessToken) {
    headers['Authorization'] = `Bearer ${accessToken}`
  }
  
  try {
    const response = await fetch(`${BASE_URL}${url}`, {
      method: 'POST',
      headers,
      body: formData
    })
    
    const data = await response.json()
    
    // 处理401未授权
    if (data.code === 401 || data.code === 401001 || data.code === 401002 || data.code === 401003) {
      clearTokens()
      window.location.href = '/login'
      return data
    }
    
    return data
  } catch (error) {
    console.error('上传失败:', error)
    return {
      code: 500,
      message: '文件上传失败',
      data: null
    }
  }
}

// 导出Token管理方法
export { 
  getAccessToken, 
  getRefreshToken, 
  setTokens, 
  clearTokens,
  refreshAccessToken 
}

export default request
