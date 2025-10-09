import { http } from './httpClient'
import type { ITasklistService } from '../ITasklistService'
import type { Tasklist } from '@/core/models/tasklist'
export class HttpTasklistService implements ITasklistService {
  listByUser(userId: string) {
    return http.get<Tasklist[]>(`/tasklists/user/${userId}`).then((r) => r.data)
  }
  create(userId: string, name: string) {
    return http.post<Tasklist>('/tasklists', { userId, name }).then((r) => r.data)
  }
  rename(id: string, name: string) {
    return http.put<Tasklist>(`/tasklists/${id}`, { name }).then((r) => r.data)
  }
  remove(id: string) {
    return http.delete(`/tasklists/${id}`).then(() => {})
  }
}
