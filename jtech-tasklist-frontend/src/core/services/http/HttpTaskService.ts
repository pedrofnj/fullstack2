import { http } from './httpClient'
import type { ITaskService } from '../ITaskService'
import type { Task } from '@/core/models/task'
export class HttpTaskService implements ITaskService {
  listByListId(listId: string) {
    return http.get<Task[]>(`/tasklists/${listId}/tasks`).then((r) => r.data)
  }
  create(t: Omit<Task, 'id' | 'completed'> & { completed?: boolean }) {
    return http.post<Task>('/tasks', t).then((r) => r.data)
  }
  update(t: Task) {
    return http.put<Task>(`/tasks/${t.id}`, t).then((r) => r.data)
  }
  toggle(id: string, completed: boolean) {
    return http.patch<Task>(`/tasks/${id}/toggle`, { completed }).then((r) => r.data)
  }
  remove(id: string) {
    return http.delete(`/tasks/${id}`).then(() => {})
  }
}
