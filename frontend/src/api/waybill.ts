import { http } from './http'
import type { WaybillDetail, WaybillEvent, WaybillLeg, WaybillSavePayload, WaybillSummary } from '../types/waybill'

interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

interface WaybillSummaryApiModel {
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

interface WaybillLegApiModel {
  legNo: number
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

interface WaybillEventApiModel {
  legId: number | null
  sortNo: number
  eventTime: string
  eventStatus: string
  eventDescription: string
  eventLocation: string
  visibleToCustomer: boolean
}

interface WaybillDetailApiModel extends WaybillSummaryApiModel {
  referenceNo: string
  customerPhone: string
  originWarehouse: string
  cargoDescription: string
  packageCount: number
  weightKg: number | null
  remark: string
  createdAt: string
  legs: WaybillLegApiModel[]
  events: WaybillEventApiModel[]
}

function toLeg(model: WaybillLegApiModel): WaybillLeg {
  return {
    legNo: model.legNo,
    legType: model.legType,
    carrierName: model.carrierName || '',
    trackingNo: model.trackingNo,
    fromNode: model.fromNode || '',
    toNode: model.toNode || '',
    legStatus: model.legStatus || 'pending',
    transferFlag: Boolean(model.transferFlag),
    departureTime: model.departureTime || '',
    arrivalTime: model.arrivalTime || '',
    remark: model.remark || '',
  }
}

function toEvent(model: WaybillEventApiModel): WaybillEvent {
  return {
    legId: model.legId,
    sortNo: model.sortNo,
    eventTime: model.eventTime,
    eventStatus: model.eventStatus,
    eventDescription: model.eventDescription,
    eventLocation: model.eventLocation || '',
    visibleToCustomer: Boolean(model.visibleToCustomer),
  }
}

function toSummary(model: WaybillSummaryApiModel): WaybillSummary {
  return {
    id: model.id,
    mainTrackingNo: model.mainTrackingNo,
    customerName: model.customerName,
    destinationCountry: model.destinationCountry,
    destinationCity: model.destinationCity || '',
    routeType: model.routeType,
    currentStatus: model.currentStatus,
    currentNode: model.currentNode || '',
    updatedAt: model.updatedAt,
  }
}

function toDetail(model: WaybillDetailApiModel): WaybillDetail {
  return {
    ...toSummary(model),
    referenceNo: model.referenceNo || '',
    customerPhone: model.customerPhone || '',
    originWarehouse: model.originWarehouse || '',
    cargoDescription: model.cargoDescription || '',
    packageCount: model.packageCount,
    weightKg: model.weightKg,
    remark: model.remark || '',
    createdAt: model.createdAt,
    legs: model.legs.map(toLeg),
    events: model.events.map(toEvent),
  }
}

export function createEmptyWaybillLeg(legNo = 1): WaybillLeg {
  return {
    legNo,
    legType: 'customer_to_warehouse',
    carrierName: '',
    trackingNo: '',
    fromNode: '',
    toNode: '',
    legStatus: 'pending',
    transferFlag: false,
    departureTime: '',
    arrivalTime: '',
    remark: '',
  }
}

export function createEmptyWaybillEvent(sortNo = 1): WaybillEvent {
  return {
    legId: null,
    sortNo,
    eventTime: '',
    eventStatus: '',
    eventDescription: '',
    eventLocation: '',
    visibleToCustomer: true,
  }
}

export async function fetchWaybills(params?: { keyword?: string; status?: string }): Promise<WaybillSummary[]> {
  const { data } = await http.get<ApiResponse<WaybillSummaryApiModel[]>>('/waybills', { params })
  return data.data.map(toSummary)
}

export async function fetchWaybill(id: number): Promise<WaybillDetail> {
  const { data } = await http.get<ApiResponse<WaybillDetailApiModel>>(`/waybills/${id}`)
  return toDetail(data.data)
}

export async function createWaybill(payload: WaybillSavePayload): Promise<WaybillDetail> {
  const { data } = await http.post<ApiResponse<WaybillDetailApiModel>>('/waybills', payload)
  return toDetail(data.data)
}

export async function updateWaybill(id: number, payload: WaybillSavePayload): Promise<WaybillDetail> {
  const { data } = await http.put<ApiResponse<WaybillDetailApiModel>>(`/waybills/${id}`, payload)
  return toDetail(data.data)
}

export async function deleteWaybill(id: number): Promise<void> {
  await http.delete(`/waybills/${id}`)
}
