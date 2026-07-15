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

export interface MemberAdminSummary {
  id: number
  phone: string
  wechatOpenid: string
  wechatUnionid: string
  wechatBindTime: string | null
  nickname: string
  fullName: string
  status: string
  waybillCount: number
  lastLoginAt: string | null
  createdAt: string
}

export interface MemberAdminDetail extends MemberAdminSummary {
  avatarUrl: string
  remark: string
  updatedAt: string
  boundWaybillIds: number[]
  waybills: MemberWaybillSummary[]
}

export interface MemberAdminSavePayload {
  phone: string
  password: string
  nickname: string
  fullName: string
  avatarUrl: string
  status: string
  remark: string
  waybillIds: number[]
}
