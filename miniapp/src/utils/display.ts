export function formatPaymentStatus(status: string) {
  if (status === 'paid') return '支付成功'
  if (status === 'closed') return '已关闭'
  if (status === 'refunding') return '退款处理中'
  if (status === 'refunded') return '已退款'
  if (status === 'exception') return '异常'
  if (status === 'timeout') return '待确认'
  return '处理中'
}

export function formatWaybillStatus(status: string) {
  if (status === 'signed') return '已签收'
  if (status === 'delivered') return '已派送'
  if (status === 'in_transit') return '运输中'
  if (status === 'arrived') return '已到站'
  if (status === 'clearing') return '清关中'
  if (status === 'pending') return '待处理'
  if (status === 'exception') return '异常'
  return status || '未知'
}

export function formatWechatBindStatus(openid?: string | null) {
  return openid ? '已绑定微信' : '未绑定微信'
}

export function formatMemberStatus(status?: string | null) {
  if (status === 'active') return '正常'
  if (status === 'disabled') return '已停用'
  if (status === 'pending') return '待审核'
  return status || '未知'
}

export function maskIdentifier(value?: string | null) {
  if (!value) {
    return '暂无'
  }
  if (value.length <= 8) {
    return value
  }
  return `${value.slice(0, 4)}...${value.slice(-4)}`
}
