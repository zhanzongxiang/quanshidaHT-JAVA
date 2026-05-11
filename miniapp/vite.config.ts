import { defineConfig } from 'vite'
import type { PluginOption } from 'vite'
import uniModule from '@dcloudio/vite-plugin-uni'

type UniPluginFactory = () => PluginOption

const createUniPlugin = (
  typeof uniModule === 'function'
    ? uniModule
    : (uniModule as { default: UniPluginFactory }).default
) as UniPluginFactory

export default defineConfig({
  plugins: [createUniPlugin()],
})
