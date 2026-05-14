import { http } from './http'
import { unwrapResponse, withQuery } from './shared'
import type { ApiResponse } from './shared'
import type { MemberAdminDetail, MemberAdminSavePayload, MemberAdminSummary, MemberWaybillSummary } from '../types/member'

interface MemberWaybillSummaryApiModel {
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

interface MemberAdminSummaryApiModel {
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

interface MemberAdminDetailApiModel extends MemberAdminSummaryApiModel {
  avatarUrl: string
  remark: string
  updatedAt: string
  boundWaybillIds: number[]
  waybills: MemberWaybillSummaryApiModel[]
}

function toWaybillSummary(model: MemberWaybillSummaryApiModel): MemberWaybillSummary {
  return {
    id: model.id,
    mainTrackingNo: model.mainTrackingNo,
    referenceNo: model.referenceNo || '',
    customerName: model.customerName,
    destinationCountry: model.destinationCountry,
    destinationCity: model.destinationCity || '',
    currentStatus: model.currentStatus,
    currentNode: model.currentNode || '',
    updatedAt: model.updatedAt,
  }
}

function toSummary(model: MemberAdminSummaryApiModel): MemberAdminSummary {
  return {
    id: model.id,
    phone: model.phone,
    wechatOpenid: model.wechatOpenid || '',
    wechatUnionid: model.wechatUnionid || '',
    wechatBindTime: model.wechatBindTime,
    nickname: model.nickname || '',
    fullName: model.fullName || '',
    status: model.status,
    waybillCount: model.waybillCount,
    lastLoginAt: model.lastLoginAt,
    createdAt: model.createdAt,
  }
}

function toDetail(model: MemberAdminDetailApiModel): MemberAdminDetail {
  return {
    ...toSummary(model),
    avatarUrl: model.avatarUrl || '',
    remark: model.remark || '',
    updatedAt: model.updatedAt,
    boundWaybillIds: model.boundWaybillIds ?? [],
    waybills: (model.waybills ?? []).map(toWaybillSummary),
  }
}

export function createEmptyMemberPayload(): MemberAdminSavePayload {
  return {
    phone: '',
    password: '',
    nickname: '',
    fullName: '',
    avatarUrl: '',
    status: 'active',
    remark: '',
    waybillIds: [],
  }
}

export async function fetchMembers(params?: { keyword?: string; status?: string }): Promise<MemberAdminSummary[]> {
  const response = await http.get<ApiResponse<MemberAdminSummaryApiModel[]>>('/admin/members', withQuery(params))
  return unwrapResponse(response).map(toSummary)
}

export async function fetchMember(id: number): Promise<MemberAdminDetail> {
  return toDetail(unwrapResponse(await http.get<ApiResponse<MemberAdminDetailApiModel>>(`/admin/members/${id}`)))
}

export async function createMember(payload: MemberAdminSavePayload): Promise<MemberAdminDetail> {
  return toDetail(unwrapResponse(await http.post<ApiResponse<MemberAdminDetailApiModel>>('/admin/members', payload)))
}

export async function updateMember(id: number, payload: MemberAdminSavePayload): Promise<MemberAdminDetail> {
  return toDetail(unwrapResponse(await http.put<ApiResponse<MemberAdminDetailApiModel>>(`/admin/members/${id}`, payload)))
}

export async function updateMemberStatus(id: number, status: string): Promise<MemberAdminDetail> {
  return toDetail(unwrapResponse(await http.put<ApiResponse<MemberAdminDetailApiModel>>(`/admin/members/${id}/status`, { status })))
}
