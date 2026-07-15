import { request } from '@/utils/http'
import type { LoginResponse } from '@/types/common'
import type {
  MemberLoginPayload,
  MemberPasswordChangePayload,
  MemberProfile,
  MemberProfileUpdatePayload,
  MemberRegisterPayload,
  MemberWaybillDetail,
  MemberWaybillSummary,
  MemberWechatBindPayload,
  MemberWechatCompletePayload,
  MemberWechatLoginPayload,
  MemberWechatLoginResult,
} from '@/types/member'

export function registerMember(payload: MemberRegisterPayload) {
  return request<LoginResponse>({
    url: '/api/member/auth/register',
    method: 'POST',
    data: payload,
    auth: false,
  })
}

export function loginMember(payload: MemberLoginPayload) {
  return request<LoginResponse>({
    url: '/api/member/auth/login',
    method: 'POST',
    data: payload,
    auth: false,
  })
}

export function loginMemberWithWechat(payload: MemberWechatLoginPayload) {
  return request<MemberWechatLoginResult>({
    url: '/api/member/auth/wechat-login',
    method: 'POST',
    data: payload,
    auth: false,
  })
}

export function completeMemberWechatLogin(payload: MemberWechatCompletePayload) {
  return request<MemberWechatLoginResult>({
    url: '/api/member/auth/wechat-complete',
    method: 'POST',
    data: payload,
    auth: false,
  })
}

export function fetchMemberProfile() {
  return request<MemberProfile>({
    url: '/api/member/profile',
  })
}

export function updateMemberProfile(payload: MemberProfileUpdatePayload) {
  return request<MemberProfile>({
    url: '/api/member/profile',
    method: 'PUT',
    data: payload,
  })
}

export function changeMemberPassword(payload: MemberPasswordChangePayload) {
  return request<void>({
    url: '/api/member/profile/password',
    method: 'PUT',
    data: payload,
  })
}

export function bindMemberWechat(payload: MemberWechatBindPayload) {
  return request<MemberProfile>({
    url: '/api/member/profile/wechat',
    method: 'PUT',
    data: payload,
  })
}

export function unbindMemberWechat() {
  return request<MemberProfile>({
    url: '/api/member/profile/wechat',
    method: 'DELETE',
  })
}

export function fetchMemberWaybills() {
  return request<MemberWaybillSummary[]>({
    url: '/api/member/waybills',
  })
}

export function fetchMemberWaybillDetail(id: number) {
  return request<MemberWaybillDetail>({
    url: `/api/member/waybills/${id}`,
  })
}
