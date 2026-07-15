import { request } from '@/utils/http'
import type {
  MemberPayOrderSummary,
  MemberPaymentPreparePayload,
  MemberPaymentPrepareResponse,
} from '@/types/payment'

export function fetchMemberPayments() {
  return request<MemberPayOrderSummary[]>({
    url: '/api/member/payments',
  })
}

export function prepareMemberPayment(payload: MemberPaymentPreparePayload) {
  return request<MemberPaymentPrepareResponse>({
    url: '/api/member/payments/prepare',
    method: 'PUT',
    data: payload,
  })
}
