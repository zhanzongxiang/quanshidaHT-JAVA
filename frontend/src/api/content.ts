import type {
  HomeContentDocument,
  HomeContentForm,
  HomeServiceItem,
  HomeTrackingContent,
} from '../types/content'

const STORAGE_KEY = 'home_content_document'

type UnknownRecord = Record<string, unknown>

function createId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

function createDefaultServiceItem(
  id: string,
  name: string,
  description: string,
  link: string,
  iconUrl: string,
): HomeServiceItem {
  return {
    id,
    iconUrl,
    name,
    description,
    link,
  }
}

function createDefaultTracking(): HomeTrackingContent {
  return {
    enabled: true,
    title: '运单追踪',
    placeholder: '请输入运单号',
    buttonText: '查询',
  }
}

function createDefaultDocument(): HomeContentDocument {
  return {
    pageCode: 'home',
    status: 'draft',
    updatedAt: '2026-04-08 10:00',
    publishedAt: null,
    form: {
      hero: {
        enabled: true,
        images: [
          {
            id: 'banner-1',
            name: 'banner-truck-1.jpg',
            url: 'https://images.unsplash.com/photo-1494412651409-8963ce7935a7?auto=format&fit=crop&w=1400&q=80',
          },
          {
            id: 'banner-2',
            name: 'banner-plane-1.jpg',
            url: 'https://images.unsplash.com/photo-1570710891163-6d3b5c47248b?auto=format&fit=crop&w=1400&q=80',
          },
        ],
        title: '全球物流解决方案',
        subtitle: '为企业提供稳定、透明、可追踪的国际物流服务，覆盖海运、空运、清关与交付全链路。',
        primaryButtonText: '立即咨询',
        primaryButtonLink: '/contact',
        secondaryButtonText: '查看服务',
        secondaryButtonLink: '/services',
      },
      tracking: createDefaultTracking(),
      servicesSection: {
        enabled: true,
        title: '主营业务',
        description: '专注核心领域，提供超越期待的专业物流服务。',
        items: [
          createDefaultServiceItem(
            'service-1',
            '台湾专线',
            '高效、安全、经济的台湾海空运双清派送到门服务。',
            '/services/taiwan',
            'https://img.icons8.com/fluency-systems-regular/96/f97316/shipping-container.png',
          ),
          createDefaultServiceItem(
            'service-2',
            '非洲专线',
            '深耕非洲市场多年，提供可靠、覆盖广泛的门到门物流。',
            '/services/africa',
            'https://img.icons8.com/fluency-systems-regular/96/f97316/warehouse.png',
          ),
          createDefaultServiceItem(
            'service-3',
            '国际空运',
            '整合全球航线资源，提供快速、准时、安全的空运服务。',
            '/services/air',
            'https://img.icons8.com/fluency-systems-regular/96/f97316/paper-plane.png',
          ),
        ],
      },
      contact: {
        title: '获取专属物流方案',
        description: '保留一个简洁明确的转化区，承接首页咨询、报价与合作需求。',
        phone: '+86 400-800-1234',
        email: 'sales@qsd-logistics.com',
      },
      seo: {
        title: 'QSD Logistics | 国际物流与供应链解决方案',
        description: 'QSD 提供国际海运、空运、清关、仓配和门到门物流服务，帮助企业提升跨境交付效率。',
        keywords: '国际物流,海运,空运,清关,跨境运输,供应链',
      },
    },
  }
}

function isRecord(value: unknown): value is UnknownRecord {
  return typeof value === 'object' && value !== null
}

function readString(value: unknown, fallback = '') {
  return typeof value === 'string' ? value : fallback
}

function readBoolean(value: unknown, fallback = false) {
  return typeof value === 'boolean' ? value : fallback
}

function cloneDocument(document: HomeContentDocument): HomeContentDocument {
  return JSON.parse(JSON.stringify(document)) as HomeContentDocument
}

function formatNow() {
  return new Date().toLocaleString('zh-CN', {
    hour12: false,
  }).replace(/\//g, '-')
}

function normalizeImages(rawHero: UnknownRecord, fallback: HomeContentDocument['form']['hero']['images']) {
  const rawImages = Array.isArray(rawHero.images) ? rawHero.images : []
  const images = rawImages
    .map((item, index) => {
      if (!isRecord(item)) {
        return null
      }

      const url = readString(item.url)
      if (!url) {
        return null
      }

      return {
        id: readString(item.id, `banner-${index + 1}`),
        name: readString(item.name, `banner-${index + 1}.jpg`),
        url,
      }
    })
    .filter((item): item is HomeContentDocument['form']['hero']['images'][number] => Boolean(item))

  if (images.length > 0) {
    return images
  }

  const legacyImageUrl = readString(rawHero.imageUrl) || readString(rawHero.backgroundImageUrl)
  if (legacyImageUrl) {
    return [
      {
        id: createId('banner'),
        name: 'legacy-banner.jpg',
        url: legacyImageUrl,
      },
    ]
  }

  return JSON.parse(JSON.stringify(fallback)) as HomeContentDocument['form']['hero']['images']
}

function normalizeTracking(rawTracking: unknown, fallback: HomeTrackingContent): HomeTrackingContent {
  const source = isRecord(rawTracking) ? rawTracking : {}

  return {
    enabled: readBoolean(source.enabled, fallback.enabled),
    title: readString(source.title, fallback.title),
    placeholder: readString(source.placeholder, fallback.placeholder),
    buttonText: readString(source.buttonText, fallback.buttonText),
  }
}

function normalizeServiceItem(rawItem: unknown, fallback: HomeServiceItem, index: number): HomeServiceItem {
  const source = isRecord(rawItem) ? rawItem : {}

  return {
    id: readString(source.id, fallback.id || `service-${index + 1}`),
    iconUrl: readString(source.iconUrl ?? source.icon ?? source.imageUrl, fallback.iconUrl),
    name: readString(source.name ?? source.title, fallback.name),
    description: readString(source.description, fallback.description),
    link: readString(source.link ?? source.path ?? source.href, fallback.link),
  }
}

function normalizeServicesSection(rawForm: UnknownRecord, fallback: HomeContentForm['servicesSection']) {
  const rawSection = isRecord(rawForm.servicesSection) ? rawForm.servicesSection : {}
  const legacyHighlights = Array.isArray(rawForm.highlights) ? rawForm.highlights : []
  const rawItems = Array.isArray(rawSection.items) && rawSection.items.length > 0 ? rawSection.items : legacyHighlights

  const items = rawItems
    .map((item, index) => {
      const fallbackItem = fallback.items[index] ?? createDefaultServiceItem(
        createId('service'),
        '',
        '',
        '',
        '',
      )
      return normalizeServiceItem(item, fallbackItem, index)
    })
    .filter((item) => item.name || item.description || item.iconUrl || item.link)

  return {
    enabled: readBoolean(rawSection.enabled, fallback.enabled),
    title: readString(rawSection.title, fallback.title),
    description: readString(rawSection.description, fallback.description),
    items: items.length > 0 ? items : JSON.parse(JSON.stringify(fallback.items)) as HomeServiceItem[],
  }
}

function normalizeForm(rawForm: unknown, fallback: HomeContentForm): HomeContentForm {
  if (!isRecord(rawForm)) {
    return JSON.parse(JSON.stringify(fallback)) as HomeContentForm
  }

  const rawHero = isRecord(rawForm.hero) ? rawForm.hero : {}
  const rawContact = isRecord(rawForm.contact) ? rawForm.contact : {}
  const rawSeo = isRecord(rawForm.seo) ? rawForm.seo : {}

  return {
    hero: {
      enabled: readBoolean(rawHero.enabled, fallback.hero.enabled),
      images: normalizeImages(rawHero, fallback.hero.images),
      title: readString(rawHero.title, fallback.hero.title),
      subtitle: readString(rawHero.subtitle, fallback.hero.subtitle),
      primaryButtonText: readString(
        rawHero.primaryButtonText ?? rawHero.primaryCtaText ?? rawHero.ctaPrimaryText,
        fallback.hero.primaryButtonText,
      ),
      primaryButtonLink: readString(
        rawHero.primaryButtonLink ?? rawHero.primaryCtaLink ?? rawHero.ctaPrimaryLink,
        fallback.hero.primaryButtonLink,
      ),
      secondaryButtonText: readString(
        rawHero.secondaryButtonText ?? rawHero.secondaryCtaText ?? rawHero.ctaSecondaryText,
        fallback.hero.secondaryButtonText,
      ),
      secondaryButtonLink: readString(
        rawHero.secondaryButtonLink ?? rawHero.secondaryCtaLink ?? rawHero.ctaSecondaryLink,
        fallback.hero.secondaryButtonLink,
      ),
    },
    tracking: normalizeTracking(rawForm.tracking, fallback.tracking),
    servicesSection: normalizeServicesSection(rawForm, fallback.servicesSection),
    contact: {
      title: readString(rawContact.title, fallback.contact.title),
      description: readString(rawContact.description, fallback.contact.description),
      phone: readString(rawContact.phone, fallback.contact.phone),
      email: readString(rawContact.email, fallback.contact.email),
    },
    seo: {
      title: readString(rawSeo.title, fallback.seo.title),
      description: readString(rawSeo.description, fallback.seo.description),
      keywords: readString(rawSeo.keywords, fallback.seo.keywords),
    },
  }
}

function normalizeDocument(rawDocument: unknown): HomeContentDocument {
  const fallback = createDefaultDocument()
  if (!isRecord(rawDocument)) {
    return fallback
  }

  return {
    pageCode: 'home',
    status: rawDocument.status === 'published' ? 'published' : 'draft',
    updatedAt: readString(rawDocument.updatedAt, fallback.updatedAt),
    publishedAt: rawDocument.publishedAt == null ? null : readString(rawDocument.publishedAt, fallback.publishedAt ?? ''),
    form: normalizeForm(rawDocument.form, fallback.form),
  }
}

export async function fetchHomeContent(): Promise<HomeContentDocument> {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) {
    const initial = createDefaultDocument()
    localStorage.setItem(STORAGE_KEY, JSON.stringify(initial))
    return cloneDocument(initial)
  }

  try {
    const normalized = normalizeDocument(JSON.parse(raw))
    localStorage.setItem(STORAGE_KEY, JSON.stringify(normalized))
    return cloneDocument(normalized)
  } catch (error) {
    const fallback = createDefaultDocument()
    localStorage.setItem(STORAGE_KEY, JSON.stringify(fallback))
    return cloneDocument(fallback)
  }
}

export async function saveHomeContentDraft(form: HomeContentForm): Promise<HomeContentDocument> {
  const current = await fetchHomeContent()
  const next: HomeContentDocument = {
    ...current,
    status: 'draft',
    updatedAt: formatNow(),
    form: JSON.parse(JSON.stringify(form)) as HomeContentForm,
  }

  localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
  return cloneDocument(next)
}

export async function publishHomeContent(form: HomeContentForm): Promise<HomeContentDocument> {
  const current = await fetchHomeContent()
  const now = formatNow()
  const next: HomeContentDocument = {
    ...current,
    status: 'published',
    updatedAt: now,
    publishedAt: now,
    form: JSON.parse(JSON.stringify(form)) as HomeContentForm,
  }

  localStorage.setItem(STORAGE_KEY, JSON.stringify(next))
  return cloneDocument(next)
}
