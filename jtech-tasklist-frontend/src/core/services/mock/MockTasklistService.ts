import type { ITasklistService } from '../ITasklistService'
import type { Tasklist } from '@/core/models/tasklist'
import { sget, sset, suuid } from '@/core/utils/storage'

const KEY = 'mock_tasklists' // array global de listas

export class MockTasklistService implements ITasklistService {
  private load() { return sget<Tasklist[]>(KEY, []) }
  private save(v: Tasklist[]) { sset(KEY, v) }

  async listByUser(userId: string) { return this.load().filter(l => l.userId === userId) }

  async create(userId: string, name: string) {
    const all = this.load()
    if (all.some(l => l.userId === userId && l.name.toLowerCase() === name.toLowerCase()))
      throw new Error('Já existe uma lista com esse nome.')
    const created = { id: suuid(), userId, name }
    this.save([...all, created])
    return created
  }

  async rename(id: string, name: string) {
    const all = this.load()
    const idx = all.findIndex(l => l.id === id)
    if (idx < 0) throw new Error('Lista não encontrada.')
    all[idx] = { ...all[idx], name }
    this.save(all)
    return all[idx]
  }

  async remove(id: string) {
    this.save(this.load().filter(l => l.id !== id))
    const TKEY = 'mock_tasks'
    const tasks = sget<any[]>(TKEY, [])
    sset(TKEY, tasks.filter(t => t.listId !== id))
  }
}
