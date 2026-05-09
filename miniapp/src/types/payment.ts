export interface MemberPayOrderSummary {
  id: number
  orderNo: string
  merchantConfigId: number | null
  merchantName: string
  waybillId: number | null
  waybillTrackingNo: string
  channel: string
  amountTotal: number
  amountPaid: number
  status: string
  description: string
  paidAt: string | null
  createdAt: string
}

export interface MemberPaymentPreparePayload {
  waybillId: number
  amountTotal: number
  description: string
  channel: string
}

export interface MemberPaymentPrepareResponse {
  payOrderId: number
  orderNo: string
  status: string
  merchantConfigId: number | null
  merchantName: string
  appId: string
  timeStamp: string
  nonceStr: string
  packageValue: string
  signType: string
  paySign: string
}
