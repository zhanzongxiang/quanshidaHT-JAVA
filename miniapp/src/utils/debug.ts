const LAST_API_EVENT_KEY = 'miniapp_last_api_event'
const CHECKLIST_STATE_KEY = 'miniapp_checklist_state'

export interface LastApiEvent {
  type: 'success' | 'error'
  url: string
  method: string
  message: string
  statusCode?: number
  at: string
}

export function setLastApiEvent(event: LastApiEvent) {
  uni.setStorageSync(LAST_API_EVENT_KEY, event)
}

export function getLastApiEvent() {
  return (uni.getStorageSync(LAST_API_EVENT_KEY) || null) as LastApiEvent | null
}

export function clearLastApiEvent() {
  uni.removeStorageSync(LAST_API_EVENT_KEY)
}

export interface ChecklistEntryState {
  checked: boolean
  note: string
}

export type ChecklistState = Record<string, ChecklistEntryState>

function normalizeChecklistEntry(value: unknown): ChecklistEntryState {
  if (typeof value === 'boolean') {
    return {
      checked: value,
      note: '',
    }
  }

  if (value && typeof value === 'object') {
    const entry = value as Partial<ChecklistEntryState>
    return {
      checked: Boolean(entry.checked),
      note: typeof entry.note === 'string' ? entry.note : '',
    }
  }

  return {
    checked: false,
    note: '',
  }
}

export function getChecklistState() {
  const saved = uni.getStorageSync(CHECKLIST_STATE_KEY)

  if (!saved || typeof saved !== 'object') {
    return {} as ChecklistState
  }

  const normalized: ChecklistState = {}
  Object.entries(saved as Record<string, unknown>).forEach(([key, value]) => {
    normalized[key] = normalizeChecklistEntry(value)
  })
  return normalized
}

export function setChecklistState(state: ChecklistState) {
  uni.setStorageSync(CHECKLIST_STATE_KEY, state)
}

export function clearChecklistState() {
  uni.removeStorageSync(CHECKLIST_STATE_KEY)
}
