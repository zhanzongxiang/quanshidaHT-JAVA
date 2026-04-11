# qsd Admin Frontend

## 鎶€鏈爤

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Tailwind CSS

## 鏈湴寮€鍙?
- 瀹夎渚濊禆锛歚npm install`
- 鍚姩寮€鍙戠幆澧冿細`npm run dev`
- 鐢熶骇鏋勫缓锛歚npm run build`

## 棣栭〉鍐呭绠＄悊

- 椤甸潰浣嶇疆锛歚src/views/ContentView.vue`
- 鏁版嵁缁撴瀯锛歚src/types/content.ts`
- 鏈湴 mock锛歚src/api/content.ts`

褰撳墠鏀寔锛?
- Banner 鍥剧墖缁?- 杩愬崟杩借釜妯″潡
- 涓昏惀涓氬姟妯″潡
- 涓€绔欏紡鏈嶅姟娴佺▼妯″潡
- 鑱旂郴杞寲鍖?- SEO 璁剧疆

瑙勫垯锛?
- 涓昏惀涓氬姟妯″潡鏈夌嫭绔嬪紑鍏?- 涓昏惀涓氬姟缂栬緫鍗＄墖鍐呴儴鏈夊眬閮ㄩ瑙?- 鍙充晶涓昏惀涓氬姟鎬婚瑙堝彧鏄剧ず鏈€鍚庢坊鍔犵殑涓€涓ā鍧?- 涓€绔欏紡鏈嶅姟娴佺▼鏈夌嫭绔嬪紑鍏炽€佹爣棰樸€佸壇鏍囬
- 涓€绔欏紡鏈嶅姟娴佺▼鏈€澶?7 涓楠?- 鍚庡彴甯冨眬鍙厑璁稿彸渚у唴瀹瑰尯婊氬姩

## 2026-04-09 Preview Rules

- All previews in `src/views/ContentView.vue` stay in the right column.
- Right-side previews are split into independent cards by module.
- Preview card order follows the left-side form module order.
- Services preview shows only the latest service card and uses a fixed `查看更多` button label.

## 2026-04-09 Home Content API

- `src/api/content.ts` now requests `GET /api/content/home`.
- Draft save uses `PUT /api/content/home/draft`.
- Publish uses `PUT /api/content/home/publish`.
- Backend persistence is provided by `backend/src/main/resources/db/migration/V3__create_site_content_page.sql` and the new `content` module.

## 2026-04-09 Seed Data

- You can import `../backend/sql/home_content_seed.sql` to preload homepage content for UI verification.

## 2026-04-09 Promise Section

- `src/views/ContentView.vue` now keeps only form editing cards and no preview column.
- Added a fixed six-item promise section with icon, title and subtitle fields.
- `backend/sql/home_content_seed.sql` includes default promise data for quick verification.

## 2026-04-09 News Module

- Added `src/views/NewsView.vue` and `src/api/news.ts`.
- Added dynamic route component mapping for `News`.
- Dev server now listens on `0.0.0.0`; use the Network URL shown by Vite or `http://<your-lan-ip>:5174`.

## 2026-04-09 Block News Form

- `src/views/NewsView.vue` now edits ordered content blocks.
- `src/api/news.ts` serializes blocks into backend `content` and parses them back on read.

## 2026-04-09 Menu Restructure

- Admin sidebar now renders nested menu groups.
- Added frontend pages for Service Lines, Navigation Settings, Footer Settings and Contact Settings.
- Re-login is required after backend migration V5 so the new menu tree is visible.

## 2026-04-09 Menu Localization

- Admin menu labels are now localized by backend migration V6.

## 2026-04-10 Service Line Editor

- Added src/api/service-line.ts and src/types/service-line.ts for fixed-template line pages.
- Added src/views/ServiceLineEditorView.vue and route /pages/service-lines/:code.
- src/views/ServiceLinesView.vue now loads backend summaries and links into the editor.

## 2026-04-11 Contact Module

- Expanded src/views/ContactSettingsView.vue into a complete contact-page module editor.
- Contact settings now cover hero copy, contact cards, office hours, service promises and CTA buttons.
- Frontend dev port is standardized to 5174 in .env.development.


## 2026-04-11 Page Schema Alignment

- src/api/content.ts now maps between admin editing fields and the schema-aligned backend home page structure.
- src/views/ContentView.vue now exposes tracking extra texts, promise imageUrl and news preview section fields.
- src/views/ServiceLineEditorView.vue is aligned to the external service-line page schema fields.

