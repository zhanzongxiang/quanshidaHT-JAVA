import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import type { AdminMenu } from '../types/auth'

const LoginView = () => import('../views/LoginView.vue')
const ForbiddenView = () => import('../views/ForbiddenView.vue')
const NotFoundView = () => import('../views/NotFoundView.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const ContentView = () => import('../views/ContentView.vue')
const NewsView = () => import('../views/NewsView.vue')
const WaybillView = () => import('../views/WaybillView.vue')
const ServiceLinesView = () => import('../views/ServiceLinesView.vue')
const ServiceLineEditorView = () => import('../views/ServiceLineEditorView.vue')
const NavigationSettingsView = () => import('../views/NavigationSettingsView.vue')
const FooterSettingsView = () => import('../views/FooterSettingsView.vue')
const ContactSettingsView = () => import('../views/ContactSettingsView.vue')
const DictionarySettingsView = () => import('../views/DictionarySettingsView.vue')

const viewMap: Record<string, () => Promise<unknown>> = {
  Dashboard: DashboardView,
  Content: ContentView,
  HomeContent: ContentView,
  News: NewsView,
  Waybill: WaybillView,
  ServiceLines: ServiceLinesView,
  NavigationSettings: NavigationSettingsView,
  FooterSettings: FooterSettingsView,
  ContactSettings: ContactSettingsView,
  DictionarySettings: DictionarySettingsView,
}

export const DEFAULT_HOME_PATH = '/dashboard'

const staticRoutes: RouteRecordRaw[] = [
  { path: '/', redirect: DEFAULT_HOME_PATH },
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/403', name: 'forbidden', component: ForbiddenView, meta: { public: true } },
  {
    path: '/pages/service-lines/:code',
    component: AdminLayout,
    children: [
      {
        path: '',
        name: 'service-line-editor',
        component: ServiceLineEditorView,
        meta: {
          menuName: '线路模板编辑',
        },
      },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: NotFoundView },
]

export const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes,
})

let menusInjected = false

function convertMenusToRoutes(menus: AdminMenu[]): RouteRecordRaw[] {
  const parentRoute: RouteRecordRaw = {
    path: '/',
    component: AdminLayout,
    children: [],
  }

  function appendRoutes(menuList: AdminMenu[]) {
    for (const menu of menuList) {
      const view = viewMap[menu.component]
      if (view) {
        parentRoute.children!.push({
          path: menu.path.startsWith('/') ? menu.path.slice(1) : menu.path,
          name: `menu-${menu.id}`,
          component: view as never,
          meta: {
            menuName: menu.name,
          },
        })
      }

      if (menu.children.length > 0) {
        appendRoutes(menu.children)
      }
    }
  }

  appendRoutes(menus)
  return [parentRoute]
}

export function ensureMenuRoutes(menus: AdminMenu[]) {
  if (menusInjected) {
    return
  }

  const menuRoutes = convertMenusToRoutes(menus)
  for (const route of menuRoutes) {
    router.addRoute(route)
  }

  menusInjected = true
}

export function resetMenuRoutes() {
  menusInjected = false
}

router.beforeEach(async (to) => {
  const auth = useAuthStore()

  if (!auth.initialized) {
    await auth.initialize()
  }

  if (to.meta.public) {
    if (to.path === '/login' && auth.isAuthenticated) {
      return DEFAULT_HOME_PATH
    }
    return true
  }

  if (!auth.isAuthenticated) {
    return {
      path: '/login',
      query: { redirect: to.fullPath },
    }
  }

  if (!menusInjected && auth.me) {
    ensureMenuRoutes(auth.me.menus)
    return to.fullPath
  }

  if (to.name === 'not-found') {
    return true
  }

  return true
})
