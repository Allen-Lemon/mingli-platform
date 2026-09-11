import axios from 'axios'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000
})

http.interceptors.response.use(
  (res) => res.data,
  (err) => {
    const msg = err?.response?.data?.msg || err.message || '网络错误'
    return Promise.reject(new Error(msg))
  }
)

export const analyze = (data) => http.post('/bazi/analyze', data)
export const getHistory = (params) => http.get('/bazi/history', { params })
export const getRecord = (id) => http.get(`/bazi/history/${id}`)
export const deleteRecord = (id) => http.delete(`/bazi/history/${id}`)
export const getStat = () => http.get('/bazi/stat')
export const getCities = () => http.get('/bazi/cities')
export const getDict = () => http.get('/dict/all')

export default http
