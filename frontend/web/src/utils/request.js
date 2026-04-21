const BASE_URL = '/api'

// 获取token
function getToken() {
  return localStorage.getItem('token')
}

// 请求封装
async function request(url, options = {}) {
  const token = getToken()
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers
  }
  
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  
  try {
    const response = await fetch(`${BASE_URL}${url}`, {
      ...options,
      headers
    })
    
    const data = await response.json()
    
    // 处理401未授权
    if (data.code === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('userId')
      window.location.href = '/login'
      return data
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

export default request
