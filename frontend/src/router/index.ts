import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import type { AdminMenu } from '../types/auth'

const LoginView = () => import('../views/LoginView.vue')
const ForbiddenView = () => import('../views/ForbiddenView.vue')
const NotFoundView = () => import('../views/NotFoundView.vue')
const AdminLayout = () => import('../layouts/AdminLayout.vue')
const DashboardView = () => import('../views/DashboardView.vue')
const ContentView = () => import('../views/ContentView.vue')

const viewMap: Record<string, () => Promise<unknown>> = {
  Dashboard: DashboardView,
  Content: ContentView,
}

const DEFAULT_HOME_PATH = '/dashboard'

const staticRoutes: RouteRecordRaw[] = [
  { path: '/', redirect: DEFAULT_HOME_PATH },
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/403', name: 'forbidden', component: ForbiddenView, meta: { public: true } },
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

  for (const menu of menus) {
    const view = viewMap[menu.component]
    if (!view) {
      continue
    }
    parentRoute.children!.push({
      path: menu.path.startsWith('/') ? menu.path.slice(1) : menu.path,
      name: `menu-${menu.id}`,
      component: view as never,
      meta: {
        menuName: menu.name,
      },
    })
  }

  return [parentRoute]
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
    const menuRoutes = convertMenusToRoutes(auth.me.menus)
    for (const route of menuRoutes) {
      router.addRoute(route)
    }
    menusInjected = true
    return to.fullPath
  }

  if (to.name === 'not-found') {
    return true
  }

  return true
})
