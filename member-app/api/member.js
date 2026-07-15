import { request } from '../utils/request'

export function login(payload) {
  return request({
    url: '/member-auth/login',
    method: 'POST',
    data: payload,
  })
}

export function fetchProfile() {
  return request({ url: '/member-auth/me' })
}

export function fetchAddresses() {
  return request({ url: '/member/addresses' })
}

export function createAddress(payload) {
  return request({
    url: '/member/addresses',
    method: 'POST',
    data: payload,
  })
}

export function updateAddress(id, payload) {
  return request({
    url: `/member/addresses/${id}`,
    method: 'PUT',
    data: payload,
  })
}

export function deleteAddress(id) {
  return request({
    url: `/member/addresses/${id}`,
    method: 'DELETE',
  })
}

export function fetchPrealerts() {
  return request({ url: '/member/prealerts' })
}

export function createPrealert(payload) {
  return request({
    url: '/member/prealerts',
    method: 'POST',
    data: payload,
  })
}

export function fetchInventoryPackages() {
  return request({ url: '/member/packages/inventory' })
}

export function fetchShipments() {
  return request({ url: '/member/shipments' })
}

export function createShipment(payload) {
  return request({
    url: '/member/shipments',
    method: 'POST',
    data: payload,
  })
}

export function fetchOrders() {
  return request({ url: '/member/orders' })
}

export function fetchFinanceRecords() {
  return request({ url: '/member/finance-records' })
}
