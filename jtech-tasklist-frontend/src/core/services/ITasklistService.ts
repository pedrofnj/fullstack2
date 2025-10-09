import type { Tasklist } from '@/core/models/tasklist'
export interface ITasklistService {
  listByUser(userId: string): Promise<Tasklist[]>
  create(userId: string, name: string): Promise<Tasklist>
  rename(id: string, name: string): Promise<Tasklist>
  remove(id: string): Promise<void>
}
