import { MockAuthService } from './mock/MockAuthService'
import { MockTasklistService } from './mock/MockTasklistService'
import { MockTaskService } from './mock/MockTaskService'
import { HttpAuthService } from './http/HttpAuthService'
import { HttpTasklistService } from './http/HttpTasklistService'
import { HttpTaskService } from './http/HttpTaskService'

const useMock = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true'

export const Services = {
  auth: useMock ? new MockAuthService() : new HttpAuthService(),
  tasklists: useMock ? new MockTasklistService() : new HttpTasklistService(),
  tasks: useMock ? new MockTaskService() : new HttpTaskService(),
} as const

export const usingMock = useMock
