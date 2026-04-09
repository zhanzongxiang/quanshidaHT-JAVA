import type { ContactSettings, FooterSettings, NavigationSettings } from '../types/site-settings'

const NAVIGATION_KEY = 'site_navigation_settings'
const FOOTER_KEY = 'site_footer_settings'
const CONTACT_KEY = 'site_contact_settings'

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T
}

function readStorage<T>(key: string, fallback: T): T {
  const raw = localStorage.getItem(key)
  if (!raw) {
    localStorage.setItem(key, JSON.stringify(fallback))
    return clone(fallback)
  }

  try {
    return {
      ...clone(fallback),
      ...JSON.parse(raw),
    }
  } catch {
    localStorage.setItem(key, JSON.stringify(fallback))
    return clone(fallback)
  }
}

const defaultNavigationSettings: NavigationSettings = {
  brandName: 'TEX Express',
  logoUrl: '',
  items: [
    { id: 'nav-1', label: '首页', path: '/', enabled: true },
    { id: 'nav-2', label: '台湾专线', path: '/services/taiwan', enabled: true },
    { id: 'nav-3', label: '非洲专线', path: '/services/africa', enabled: true },
    { id: 'nav-4', label: '国际快递', path: '/services/express', enabled: true },
    { id: 'nav-5', label: '新闻资讯', path: '/news', enabled: true },
    { id: 'nav-6', label: '关于我们', path: '/about', enabled: true },
    { id: 'nav-7', label: '联系我们', path: '/contact', enabled: true },
  ],
}

const defaultFooterSettings: FooterSettings = {
  companyTitle: '物流通',
  companyDescription: '专业的物流服务提供商',
  phone: '400-123-4567',
  email: 'info@logistics.com',
  quickLinks: '关于我们\n新闻资讯\n联系我们',
  followUs: '微信公众号：物流通',
  copyright: '© 2024 物流通. 保留所有权利',
}

const defaultContactSettings: ContactSettings = {
  title: '联系我们',
  subtitle: '欢迎咨询运输方案、报价和合作问题',
  phone: '400-123-4567',
  email: 'info@logistics.com',
  address: '深圳市南山区科技园',
  wechat: 'QSD-Logistics',
}

export async function fetchNavigationSettings(): Promise<NavigationSettings> {
  return readStorage(NAVIGATION_KEY, defaultNavigationSettings)
}

export async function saveNavigationSettings(payload: NavigationSettings): Promise<NavigationSettings> {
  localStorage.setItem(NAVIGATION_KEY, JSON.stringify(payload))
  return clone(payload)
}

export async function fetchFooterSettings(): Promise<FooterSettings> {
  return readStorage(FOOTER_KEY, defaultFooterSettings)
}

export async function saveFooterSettings(payload: FooterSettings): Promise<FooterSettings> {
  localStorage.setItem(FOOTER_KEY, JSON.stringify(payload))
  return clone(payload)
}

export async function fetchContactSettings(): Promise<ContactSettings> {
  return readStorage(CONTACT_KEY, defaultContactSettings)
}

export async function saveContactSettings(payload: ContactSettings): Promise<ContactSettings> {
  localStorage.setItem(CONTACT_KEY, JSON.stringify(payload))
  return clone(payload)
}
