import type { ContactSettings, FooterSettings, NavigationSettings } from '../types/site-settings'

const SITE_CONFIG_KEY = 'site_public_config'
const CONTACT_PAGE_KEY = 'site_contact_page_data'

interface StoredSiteConfig {
  logo: {
    image: string
    alt: string
    href: string
  }
  navItems: Array<{
    name: string
    path: string
  }>
  languages: Array<{
    title: string
    lang: string
  }>
  footer: {
    brandTitle: string
    brandDescription: string
    contactTitle: string
    phone: string
    email: string
    linksTitle: string
    links: Array<{
      title: string
      href: string
    }>
    socialTitle: string
    socialText: string
    copyright: string
  }
}

interface StoredContactPageData {
  hero: {
    badge: string
    title: string
    description: string
    tags: string[]
    primaryButton: {
      text: string
      href: string
    }
    secondaryButton: {
      text: string
      route: string
    }
    quickPanel: Array<{
      label: string
      value: string
    }>
    note: string
  }
  contactCards: Array<{
    title: string
    value: string
    meta: string
    description: string
    icon: string
    actionLabel: string
    href: string
  }>
  serviceWindows: Array<{
    label: string
    value: string
  }>
  visitChecklist: string[]
  mapInfo: {
    title: string
    description: string
    markers: Array<{
      title: string
      type: string
    }>
  }
  promises: string[]
  cta: {
    tag: string
    title: string
    description: string
    primaryButton: {
      text: string
      href: string
    }
    secondaryButton: {
      text: string
      route: string
    }
  }
}

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
    return JSON.parse(raw) as T
  } catch {
    localStorage.setItem(key, JSON.stringify(fallback))
    return clone(fallback)
  }
}

const defaultSiteConfig: StoredSiteConfig = {
  logo: {
    image: '',
    alt: 'TEX Express',
    href: '/',
  },
  navItems: [
    { name: '首页', path: '/' },
    { name: '台湾专线', path: '/taiwan' },
    { name: '非洲专线', path: '/feizhou' },
    { name: '国际快递', path: '/international' },
    { name: '新闻资讯', path: '/news' },
    { name: '关于我们', path: '/about' },
    { name: '联系我们', path: '/contact' },
  ],
  languages: [
    { title: '中文', lang: 'zh-CN' },
    { title: 'English', lang: 'en' },
  ],
  footer: {
    brandTitle: 'TEX Express',
    brandDescription: '聚焦跨境专线、国际快递和仓配协同的综合物流服务商。',
    contactTitle: '联系我们',
    phone: '400-123-4567',
    email: 'info@texexpress.com',
    linksTitle: '快捷链接',
    links: [
      { title: '关于我们', href: '/about' },
      { title: '新闻资讯', href: '/news' },
      { title: '联系我们', href: '/contact' },
    ],
    socialTitle: '关注我们',
    socialText: '微信公众号：TEX Express',
    copyright: '© 2026 TEX Express. All rights reserved.',
  },
}

const defaultContactPageData: StoredContactPageData = {
  hero: {
    badge: '7 x 12 快速响应',
    title: '联系我们，获取更稳的跨境物流方案',
    description: '支持专线运输、国际快递、仓配协同与定制报价。把目的地、货量和时效发给我们，顾问会在 30 分钟内响应。',
    tags: ['专线运输', '国际快递', '仓配协同', '企业报价'],
    primaryButton: {
      text: '立即咨询顾问',
      href: '/contact/form',
    },
    secondaryButton: {
      text: '查看服务线路',
      route: '/services',
    },
    quickPanel: [
      { label: '业务热线', value: '400-123-4567' },
      { label: '商务邮箱', value: 'bd@texexpress.com' },
      { label: '服务时间', value: '周一至周五 09:00 - 21:00' },
    ],
    note: '如果你已经有装箱清单、SKU 或目的国要求，建议直接发给顾问，可减少来回确认时间。',
  },
  contactCards: [
    {
      title: '业务热线',
      value: '400-123-4567',
      meta: '工作日 09:00 - 21:00',
      description: '提供专线咨询、报价和异常跟进。',
      icon: 'mdi-phone-outline',
      actionLabel: '拨打电话',
      href: 'tel:4001234567',
    },
    {
      title: '商务邮箱',
      value: 'bd@texexpress.com',
      meta: '合作与投标',
      description: '适合发送合作需求、招投标资料和批量报价清单。',
      icon: 'mdi-email-outline',
      actionLabel: '发送邮件',
      href: 'mailto:bd@texexpress.com',
    },
    {
      title: '客户顾问微信',
      value: 'TEX-Express',
      meta: '工作时段在线',
      description: '适合补充品类、图片、清关要求和阶段性沟通。',
      icon: 'mdi-wechat',
      actionLabel: '复制微信号',
      href: 'weixin://dl/chat?TEX-Express',
    },
    {
      title: '深圳运营中心',
      value: '深圳市龙岗区坂田街道跨境物流产业园 A2 栋 5 层',
      meta: '支持预约到访',
      description: '建议提前 1 个工作日提交来访信息。',
      icon: 'mdi-map-marker-outline',
      actionLabel: '导航前往',
      href: '/contact#map',
    },
  ],
  serviceWindows: [
    { label: '工作日', value: '周一至周五 09:00 - 21:00' },
    { label: '周末', value: '周六至周日 10:00 - 18:00' },
    { label: '响应承诺', value: '紧急问题 30 分钟内响应，常规询价 2 小时内给出初步方案。' },
  ],
  visitChecklist: [
    '准备货物类型和目的地信息',
    '提供预计重量、体积和件数',
    '补充对时效、清关和派送的要求',
  ],
  mapInfo: {
    title: '地图与到访提示',
    description: '地图区建议显示深圳运营中心定位点，并补充停车和来访登记说明。',
    markers: [
      { title: '深圳运营中心', type: 'office' },
    ],
  },
  promises: [
    '报价透明，无隐藏附加费',
    '方案先评估时效和风险，再给报价',
    '异常件全程跟进并同步节点',
    '支持企业客户月结与定制对账',
  ],
  cta: {
    tag: '先发需求，我们再给方案',
    title: '把需求发过来，我们先判断线路再报价',
    description: '如果你已经有装箱清单、SKU 或目的国要求，建议直接发给顾问，可减少来回确认时间。',
    primaryButton: {
      text: '立即咨询顾问',
      href: '/contact/form',
    },
    secondaryButton: {
      text: '查看服务线路',
      route: '/services',
    },
  },
}

function readSiteConfig(): StoredSiteConfig {
  return readStorage(SITE_CONFIG_KEY, defaultSiteConfig)
}

function writeSiteConfig(config: StoredSiteConfig) {
  localStorage.setItem(SITE_CONFIG_KEY, JSON.stringify(config))
}

function readContactPageData(): StoredContactPageData {
  return readStorage(CONTACT_PAGE_KEY, defaultContactPageData)
}

function writeContactPageData(data: StoredContactPageData) {
  localStorage.setItem(CONTACT_PAGE_KEY, JSON.stringify(data))
}

function toNavigationSettings(config: StoredSiteConfig): NavigationSettings {
  return {
    brandName: config.logo.alt,
    logoUrl: config.logo.image,
    items: config.navItems.map((item, index) => ({
      id: `nav-${index + 1}`,
      label: item.name,
      path: item.path,
      enabled: true,
    })),
  }
}

function toFooterSettings(config: StoredSiteConfig): FooterSettings {
  return {
    companyTitle: config.footer.brandTitle,
    companyDescription: config.footer.brandDescription,
    phone: config.footer.phone,
    email: config.footer.email,
    quickLinks: config.footer.links.map((item) => item.title).join('\n'),
    followUs: config.footer.socialText,
    copyright: config.footer.copyright,
  }
}

function toContactSettings(data: StoredContactPageData): ContactSettings {
  const hotlineCard = data.contactCards[0]
  const emailCard = data.contactCards[1]
  const wechatCard = data.contactCards[2]
  const addressCard = data.contactCards[3]

  return {
    heroBadge: data.hero.badge,
    heroTitle: data.hero.title,
    heroSubtitle: data.hero.description,
    hotlineTitle: hotlineCard?.title ?? '',
    hotlineNumber: hotlineCard?.value ?? '',
    hotlineDescription: hotlineCard?.description ?? '',
    emailTitle: emailCard?.title ?? '',
    emailAddress: emailCard?.value ?? '',
    emailDescription: emailCard?.description ?? '',
    wechatTitle: wechatCard?.title ?? '',
    wechatId: wechatCard?.value ?? '',
    wechatDescription: wechatCard?.description ?? '',
    addressTitle: addressCard?.title ?? '',
    addressLine: addressCard?.value ?? '',
    addressDescription: addressCard?.description ?? '',
    officeHoursTitle: '服务时间',
    weekdayHours: data.serviceWindows[0]?.value ?? '',
    weekendHours: data.serviceWindows[1]?.value ?? '',
    responseCommitment: data.serviceWindows[2]?.value ?? '',
    visitNotice: data.visitChecklist.join('\n'),
    ctaTitle: data.cta.title,
    ctaSubtitle: data.cta.description,
    primaryButtonText: data.cta.primaryButton.text,
    primaryButtonLink: data.cta.primaryButton.href,
    secondaryButtonText: data.cta.secondaryButton.text,
    secondaryButtonLink: data.cta.secondaryButton.route,
    mapHint: data.mapInfo.description,
    servicePromises: data.promises.join('\n'),
  }
}

export async function fetchNavigationSettings(): Promise<NavigationSettings> {
  return toNavigationSettings(readSiteConfig())
}

export async function saveNavigationSettings(payload: NavigationSettings): Promise<NavigationSettings> {
  const config = readSiteConfig()
  config.logo.alt = payload.brandName
  config.logo.image = payload.logoUrl
  config.navItems = payload.items
    .filter((item) => item.enabled)
    .map((item) => ({
      name: item.label,
      path: item.path,
    }))
  writeSiteConfig(config)
  return clone(payload)
}

export async function fetchFooterSettings(): Promise<FooterSettings> {
  return toFooterSettings(readSiteConfig())
}

export async function saveFooterSettings(payload: FooterSettings): Promise<FooterSettings> {
  const config = readSiteConfig()
  config.footer = {
    brandTitle: payload.companyTitle,
    brandDescription: payload.companyDescription,
    contactTitle: '联系我们',
    phone: payload.phone,
    email: payload.email,
    linksTitle: '快捷链接',
    links: payload.quickLinks
      .split('\n')
      .map((item) => item.trim())
      .filter(Boolean)
      .map((item) => ({
        title: item,
        href: item.startsWith('/') ? item : '/#',
      })),
    socialTitle: '关注我们',
    socialText: payload.followUs,
    copyright: payload.copyright,
  }
  writeSiteConfig(config)
  return clone(payload)
}

export async function fetchContactSettings(): Promise<ContactSettings> {
  return toContactSettings(readContactPageData())
}

export async function saveContactSettings(payload: ContactSettings): Promise<ContactSettings> {
  const data: StoredContactPageData = {
    hero: {
      badge: payload.heroBadge,
      title: payload.heroTitle,
      description: payload.heroSubtitle,
      tags: [
        payload.hotlineTitle,
        payload.emailTitle,
        payload.wechatTitle,
      ].filter(Boolean),
      primaryButton: {
        text: payload.primaryButtonText,
        href: payload.primaryButtonLink,
      },
      secondaryButton: {
        text: payload.secondaryButtonText,
        route: payload.secondaryButtonLink,
      },
      quickPanel: [
        { label: payload.hotlineTitle, value: payload.hotlineNumber },
        { label: payload.emailTitle, value: payload.emailAddress },
        { label: payload.officeHoursTitle, value: payload.weekdayHours },
      ].filter((item) => item.label || item.value),
      note: payload.ctaSubtitle,
    },
    contactCards: [
      {
        title: payload.hotlineTitle,
        value: payload.hotlineNumber,
        meta: '电话咨询',
        description: payload.hotlineDescription,
        icon: 'mdi-phone-outline',
        actionLabel: '拨打电话',
        href: `tel:${payload.hotlineNumber.replace(/\s+/g, '')}`,
      },
      {
        title: payload.emailTitle,
        value: payload.emailAddress,
        meta: '邮件沟通',
        description: payload.emailDescription,
        icon: 'mdi-email-outline',
        actionLabel: '发送邮件',
        href: `mailto:${payload.emailAddress}`,
      },
      {
        title: payload.wechatTitle,
        value: payload.wechatId,
        meta: '即时沟通',
        description: payload.wechatDescription,
        icon: 'mdi-wechat',
        actionLabel: '复制微信号',
        href: 'weixin://dl/chat',
      },
      {
        title: payload.addressTitle,
        value: payload.addressLine,
        meta: '预约到访',
        description: payload.addressDescription,
        icon: 'mdi-map-marker-outline',
        actionLabel: '查看地图',
        href: '/contact#map',
      },
    ],
    serviceWindows: [
      { label: '工作日', value: payload.weekdayHours },
      { label: '周末', value: payload.weekendHours },
      { label: '响应承诺', value: payload.responseCommitment },
    ],
    visitChecklist: payload.visitNotice
      .split('\n')
      .map((item) => item.trim())
      .filter(Boolean),
    mapInfo: {
      title: payload.addressTitle,
      description: payload.mapHint,
      markers: [
        { title: payload.addressTitle, type: 'office' },
      ],
    },
    promises: payload.servicePromises
      .split('\n')
      .map((item) => item.trim())
      .filter(Boolean),
    cta: {
      tag: payload.ctaTitle,
      title: payload.ctaTitle,
      description: payload.ctaSubtitle,
      primaryButton: {
        text: payload.primaryButtonText,
        href: payload.primaryButtonLink,
      },
      secondaryButton: {
        text: payload.secondaryButtonText,
        route: payload.secondaryButtonLink,
      },
    },
  }

  writeContactPageData(data)
  return clone(payload)
}
