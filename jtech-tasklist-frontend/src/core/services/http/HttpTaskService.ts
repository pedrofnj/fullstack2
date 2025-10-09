import http from '@/core/services/http/httpClient'
import { useAuthStore } from '@/stores/useAuthStore'

function getUserIdHeader() {
  const user = useAuthStore().user
  return user ? { 'X-User-Id': user.id } : {}
}

export class HttpTaskService {

  async listByListId(listId: string) {
    const headers = getUserIdHeader()
    const { data } = await http.get(`/tasks/list/${listId}`, { headers })
    return data
  }

  async create(task: {
    userId: string
    listId: string
    title: string
    description?: string
    dueDate?: string
  }) {
    const { data } = await http.post('/tasks', task)
    return data
  }

  async update(task: {
    id: string
    userId: string
    listId: string
    title: string
    description?: string
    dueDate?: string
    completed?: boolean
  }) {
    const headers = getUserIdHeader()
    const { data } = await http.put(`/tasks/${task.id}`, task, { headers })
    return data
  }

  async toggle(id: string, completed: boolean) {
    const headers = getUserIdHeader()
    const { data } = await http.patch(`/tasks/${id}`, { completed }, { headers })
    return data
  }

  async remove(id: string) {
    const headers = getUserIdHeader()
    await http.delete(`/tasks/${id}`, { headers })
  }
}
