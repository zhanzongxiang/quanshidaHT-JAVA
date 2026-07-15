export interface MemberRegisterPayload {
  phone: string
  password: string
  nickname: string
  fullName: string
}

export interface MemberLoginPayload {
  phone: string
  password: string
}

export interface MemberPasswordChangePayload {
  currentPassword: string
  newPassword: string
}

export interface MemberWechatLoginPayload {
  code: string
  phone?: string
  nickname?: string
  fullName?: string
  replaceBinding?: boolean
}

export interface MemberWechatCompletePayload {
  bindTicket: string
  phone: string
  nickname?: string
  fullName?: string
  replaceBinding?: boolean
}

export interface MemberWechatLoginResult {
  accessToken: string | null
  tokenType: string | null
  phoneCompletionRequired: boolean
  bindTicket: string | null
}

export interface MemberProfile {
  id: number
  phone: string
  wechatOpenid: string
  wechatUnionid: string
  wechatBindTime: string | null
  nickname: string
  fullName: string
  avatarUrl: string
  status: string
  registerSource: string
  lastLoginAt: string | null
  lastLoginIp: string
  passwordUpdatedAt: string | null
  createdAt: string
}

export interface MemberProfileUpdatePayload {
  nickname: string
  fullName: string
  avatarUrl: string
}

export interface MemberWechatBindPayload {
  code: string
  replaceBinding?: boolean
}

export interface WaybillLeg {
  legNo: number | null
  legType: string
  carrierName: string
  trackingNo: string
  fromNode: string
  toNode: string
  legStatus: string
  transferFlag: boolean
  departureTime: string | null
  arrivalTime: string | null
  remark: string
}

export interface WaybillEvent {
  legId: number | null
  sortNo: number | null
  eventTime: string
  eventStatus: string
  eventDescription: string
  eventLocation: string
  visibleToCustomer: boolean
}

export interface MemberWaybillSummary {
  id: number
  mainTrackingNo: string
  referenceNo: string
  customerName: string
  destinationCountry: string
  destinationCity: string
  currentStatus: string
  currentNode: string
  updatedAt: string
}

export interface MemberWaybillDetail extends MemberWaybillSummary {
  originWarehouse: string
  cargoDescription: string
  packageCount: number
  weightKg: number
  legs: WaybillLeg[]
  events: WaybillEvent[]
}
