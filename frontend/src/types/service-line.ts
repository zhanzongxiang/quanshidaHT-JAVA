export type ServiceLineCode = 'taiwan' | 'feizhou' | 'international'

export interface ServiceLineSummary {
  lineCode: ServiceLineCode
  lineName: string
  linePath: string
  description: string
  status: 'draft' | 'published'
  updatedAt: string
  publishedAt: string | null
}

export interface ServiceLineMetric {
  id: string
  label: string
  value: string
}

export interface ServiceLineHighlightItem {
  id: string
  title: string
  description: string
  icon: string
}

export interface ServiceLineTextItem {
  id: string
  value: string
}

export interface ServiceLineForm {
  key: ServiceLineCode
  eyebrow: string
  title: string
  subtitle: string
  description: string
  heroImage: string
  heroTags: ServiceLineTextItem[]
  metrics: ServiceLineMetric[]
  highlightsTitle: string
  highlightsSubtitle: string
  highlights: ServiceLineHighlightItem[]
  processTitle: string
  processSubtitle: string
  processSteps: ServiceLineTextItem[]
  scopeTitle: string
  scopeSubtitle: string
  scopeImage: string
  scopeItems: ServiceLineTextItem[]
  supportTitle: string
  supportDescription: string
  supportItems: ServiceLineTextItem[]
  ctaTitle: string
  ctaSubtitle: string
}

export interface ServiceLineDocument {
  pageCode: string
  lineCode: ServiceLineCode
  lineName: string
  linePath: string
  status: 'draft' | 'published'
  updatedAt: string
  publishedAt: string | null
  form: ServiceLineForm
}

function createId(prefix: string) {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}

export function createTextItem(prefix: string, value = ''): ServiceLineTextItem {
  return {
    id: createId(prefix),
    value,
  }
}

export function createMetric(): ServiceLineMetric {
  return {
    id: createId('metric'),
    label: '',
    value: '',
  }
}

export function createHighlight(): ServiceLineHighlightItem {
  return {
    id: createId('highlight'),
    title: '',
    description: '',
    icon: '',
  }
}

export function createEmptyServiceLineForm(): ServiceLineForm {
  return {
    key: 'taiwan',
    eyebrow: '',
    title: '',
    subtitle: '',
    description: '',
    heroImage: '',
    heroTags: [createTextItem('hero-tag'), createTextItem('hero-tag')],
    metrics: [createMetric(), createMetric(), createMetric()],
    highlightsTitle: '',
    highlightsSubtitle: '',
    highlights: [createHighlight(), createHighlight(), createHighlight(), createHighlight()],
    processTitle: '',
    processSubtitle: '',
    processSteps: [createTextItem('process'), createTextItem('process'), createTextItem('process')],
    scopeTitle: '',
    scopeSubtitle: '',
    scopeImage: '',
    scopeItems: [createTextItem('scope'), createTextItem('scope'), createTextItem('scope')],
    supportTitle: '',
    supportDescription: '',
    supportItems: [createTextItem('support'), createTextItem('support'), createTextItem('support')],
    ctaTitle: '',
    ctaSubtitle: '',
  }
}
