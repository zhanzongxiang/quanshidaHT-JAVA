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
  heroBadge: string
  heroTitle: string
  heroSubtitle: string
  hotlineTitle: string
  hotlineNumber: string
  hotlineDescription: string
  emailTitle: string
  emailAddress: string
  emailDescription: string
  wechatTitle: string
  wechatId: string
  wechatDescription: string
  addressTitle: string
  addressLine: string
  addressDescription: string
  officeHoursTitle: string
  weekdayHours: string
  weekendHours: string
  responseCommitment: string
  visitNotice: string
  ctaTitle: string
  ctaSubtitle: string
  primaryButtonText: string
  primaryButtonLink: string
  secondaryButtonText: string
  secondaryButtonLink: string
  mapHint: string
  servicePromises: string
}
