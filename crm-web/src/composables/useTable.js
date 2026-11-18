import { reactive, ref } from 'vue'
import request from '@/utils/request'

/**
 * 列表页通用逻辑：分页、查询、刷新
 * 用法：const { list, total, query, load, search } = useTable('/lead', { status: null })
 */
export function useTable(url, defaults = {}) {
  const loading = ref(false)
  const list = ref([])
  const total = ref(0)
  const query = reactive({ pageNum: 1, pageSize: 10, ...defaults })

  async function load() {
    loading.value = true
    try {
      const data = await request.get(`${url}/list`, { params: query })
      list.value = data.list
      total.value = data.total
    } finally {
      loading.value = false
    }
  }

  function search() {
    query.pageNum = 1
    load()
  }

  return { loading, list, total, query, load, search }
}
