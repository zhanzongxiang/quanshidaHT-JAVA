export interface AdminMemberPrealert {
  id: number
  memberId: number
  memberNo: string
  username: string
  prealertNo: string
  trackingNo: string
  courierCode: string
  warehouseCode: string
  goodsName: string
  packageCount: number
  estimatedWeight: number | null
  remark: string
  status: string
  createdAt: string
}

export interface AdminMemberPackage {
  id: number
  memberId: number
  memberNo: string
  username: string
  packageNo: string
  trackingNo: string
  goodsName: string
  warehouseCode: string
  packageCount: number
  weightKg: number | null
  packageStatus: string
  issueFlag: boolean
  issueType: string | null
  issueNote: string | null
  warehouseInAt: string | null
}

export interface AdminMemberShipment {
  id: number
  memberId: number
  memberNo: string
  username: string
  shipmentNo: string
  shipmentStatus: string
  packageCount: number
  totalWeight: number | null
  remark: string
  createdAt: string
  packageIds: number[]
}

export interface AdminMemberOrder {
  id: number
  memberId: number
  memberNo: string
  username: string
  shipmentId: number | null
  orderNo: string
  orderStatus: string
  paymentStatus: string
  amount: number
  currencyCode: string
  remark: string
  createdAt: string
}

export interface AdminPackageInboundPayload {
  warehouseCode: string
  packageCount: number | null
  weightKg: number | null
  remark: string
}

export interface AdminShipmentQuotePayload {
  amount: number
  remark: string
}
