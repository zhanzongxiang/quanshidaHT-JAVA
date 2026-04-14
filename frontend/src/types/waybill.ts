export interface WaybillLeg {
  legNo: number
  legType: string
  carrierName: string
  trackingNo: string
  fromNode: string
  toNode: string
  legStatus: string
  transferFlag: boolean
  departureTime: string
  arrivalTime: string
  remark: string
}

export interface WaybillEvent {
  legId: number | null
  sortNo: number
  eventTime: string
  eventStatus: string
  eventDescription: string
  eventLocation: string
  visibleToCustomer: boolean
}

export interface WaybillSummary {
  id: number
  mainTrackingNo: string
  customerName: string
  destinationCountry: string
  destinationCity: string
  routeType: string
  currentStatus: string
  currentNode: string
  updatedAt: string
}

export interface WaybillDetail extends WaybillSummary {
  referenceNo: string
  customerPhone: string
  originWarehouse: string
  cargoDescription: string
  packageCount: number
  weightKg: number | null
  remark: string
  createdAt: string
  legs: WaybillLeg[]
  events: WaybillEvent[]
}

export interface WaybillSavePayload {
  mainTrackingNo: string
  referenceNo: string
  customerName: string
  customerPhone: string
  originWarehouse: string
  destinationCountry: string
  destinationCity: string
  routeType: string
  currentStatus: string
  currentNode: string
  cargoDescription: string
  packageCount: number
  weightKg: number | null
  remark: string
  legs: WaybillLeg[]
  events: WaybillEvent[]
}
