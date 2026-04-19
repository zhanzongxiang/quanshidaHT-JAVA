export interface DictionaryItem {
  id: number
  dictType: string
  dictName: string
  itemLabel: string
  itemValue: string
  sortNo: number
  enabled: boolean
  builtin: boolean
  remark: string
  updatedAt: string | null
}

export interface DictionaryGroup {
  dictType: string
  dictName: string
  items: DictionaryItem[]
}

export interface DictionaryOption {
  label: string
  value: string
}

export interface DictionaryItemSavePayload {
  dictType: string
  dictName: string
  itemLabel: string
  itemValue: string
  sortNo: number
  enabled: boolean
  remark: string
}
