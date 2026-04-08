export interface HomeBannerImage {
  id: string
  name: string
  url: string
}

export interface HomeHeroContent {
  enabled: boolean
  images: HomeBannerImage[]
  title: string
  subtitle: string
  primaryButtonText: string
  primaryButtonLink: string
  secondaryButtonText: string
  secondaryButtonLink: string
}

export interface HomeTrackingContent {
  enabled: boolean
  title: string
  placeholder: string
  buttonText: string
}

export interface HomeServiceItem {
  id: string
  iconUrl: string
  name: string
  description: string
  link: string
}

export interface HomeServicesSection {
  enabled: boolean
  title: string
  description: string
  items: HomeServiceItem[]
}

export interface HomeContactContent {
  title: string
  description: string
  phone: string
  email: string
}

export interface HomeSeoContent {
  title: string
  description: string
  keywords: string
}

export interface HomeContentForm {
  hero: HomeHeroContent
  tracking: HomeTrackingContent
  servicesSection: HomeServicesSection
  contact: HomeContactContent
  seo: HomeSeoContent
}

export interface HomeContentDocument {
  pageCode: 'home'
  status: 'draft' | 'published'
  updatedAt: string
  publishedAt: string | null
  form: HomeContentForm
}
