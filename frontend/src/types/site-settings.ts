export interface NavigationItemSetting {
  id: string
  label: string
  path: string
  enabled: boolean
}

export interface NavigationSettings {
  brandName: string
  logoUrl: string
  items: NavigationItemSetting[]
}

export interface FooterSettings {
  companyTitle: string
  companyDescription: string
  phone: string
  email: string
  quickLinks: string
  followUs: string
  copyright: string
}

export interface ContactSettings {
  title: string
  subtitle: string
  phone: string
  email: string
  address: string
  wechat: string
}
