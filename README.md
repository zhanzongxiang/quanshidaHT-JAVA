# qsd Admin Backend

## 椤圭洰姒傚喌

杩欐槸 `qsd-gw` 瀹樼綉閰嶅鐨勫悗鍙扮鐞嗙郴缁熴€備竴鏈熶紭鍏堟敮鎸佸畼缃戝唴瀹硅繍钀ャ€佸鎴风嚎绱㈠拰杩愬崟杞ㄨ抗缁存姢锛屼笉浠ュ畬鏁?ERP 涓虹洰鏍囥€?
## 褰撳墠缁撴瀯

- `frontend`
  - Vue 3 + TypeScript + Vite + Vue Router + Pinia
  - UI 鏂规锛欵lement Plus + Tailwind CSS
  - 宸插疄鐜扮櫥褰曢〉銆佸悗鍙板竷灞€銆佽矾鐢卞畧鍗€佸姩鎬佽彍鍗曟敞鍏?  - 宸插疄鐜伴椤靛唴瀹圭鐞嗚〃鍗曪紝鏈湴淇濆瓨/鍙戝竷浣跨敤 localStorage mock
- `backend`
  - Spring Boot 3 + JWT + MyBatis-Plus + Flyway
  - 宸插疄鐜?`/api/health`銆乣/api/auth/login`銆乣/api/auth/me`

## 棣栭〉鍐呭绠＄悊

褰撳墠棣栭〉鍐呭绠＄悊浣嶄簬 `frontend/src/views/ContentView.vue`锛屾暟鎹粨鏋勫畾涔夊湪 `frontend/src/types/content.ts`锛屾湰鍦?mock 鍦?`frontend/src/api/content.ts`銆?
褰撳墠瑕嗙洊锛?
- Banner 鍥剧墖缁?- 杩愬崟杩借釜妯″潡
- 涓昏惀涓氬姟妯″潡
- 涓€绔欏紡鏈嶅姟娴佺▼妯″潡
- 鑱旂郴杞寲鍖?- SEO 璁剧疆

瑙勫垯锛?
- 涓昏惀涓氬姟妯″潡鏈夌嫭绔嬪紑鍏?- 涓昏惀涓氬姟鍙充晶鎬婚瑙堝彧鏄剧ず鏈€鍚庢坊鍔犵殑涓€涓笟鍔℃ā鍧?- 姣忎釜涓昏惀涓氬姟缂栬緫鍗＄墖鍐呴儴甯﹀眬閮ㄩ瑙?- 涓€绔欏紡鏈嶅姟娴佺▼鏈夌嫭绔嬪紑鍏炽€佹爣棰樸€佸壇鏍囬
- 涓€绔欏紡鏈嶅姟娴佺▼鏈€澶?7 涓楠?- 鍚庡彴鍙粴鍔ㄥ彸渚у唴瀹瑰尯锛屼笉婊氬姩鏁翠釜椤甸潰

## 鏈湴鍚姩

1. 鍚姩渚濊禆鏈嶅姟
   - `docker compose -f infra/docker-compose.yml up -d`
2. 鍚姩鍚庣
   - `mvn -f backend/pom.xml spring-boot:run`
3. 鍚姩鍓嶇
   - `npm --prefix frontend run dev`

濡傛灉淇敼浜?`frontend/vite.config.ts`銆乣frontend/postcss.config.js`銆乣frontend/tailwind.config.js` 鎴?`.env`锛岄渶瑕侀噸鍚墠绔紑鍙戞湇鍔°€?
## 2026-04-09 Preview Rules

- 首页内容管理右侧预览改为按模块逐块展示，左侧只负责填报。
- 右侧预览顺序与左侧模块顺序保持一致，便于对照编辑。
- 主营业务右侧总预览只显示最后新增的一项，且按钮文案固定为“查看更多”。

## 2026-04-09 Home Content API

- Added backend endpoints: `GET /api/content/home`, `PUT /api/content/home/draft`, `PUT /api/content/home/publish`.
- Added Flyway migration `backend/src/main/resources/db/migration/V3__create_site_content_page.sql`.
- Frontend `src/api/content.ts` has been switched from localStorage mock to real backend requests.

## 2026-04-09 Home Content Seed

- Added manual import SQL: `backend/sql/home_content_seed.sql`.
- Import this file after `site_content_page` has been created to preview homepage content immediately.

## 2026-04-09 Promise Section

- Removed all right-side preview modules from homepage content management.
- Added fixed six-item promise section editing with icon, title and subtitle fields.
- Updated backend seed SQL to include the promise section.

## 2026-04-09 News Module

- Removed the homepage contact conversion section from content management.
- Added news management frontend and backend with menu route `/news`.
- Added Vite LAN dev access and HMR-related env settings in `frontend/.env.development`.

## 2026-04-09 Block News Form

- News editing now uses block-based content instead of one raw content textarea.
- Supported block types are paragraph, heading, image and image caption.

## 2026-04-09 Menu Restructure

- Sidebar menus were restructured into grouped sections.
- Added routes for `/pages/home`, `/pages/service-lines`, `/news`, `/settings/navigation`, `/settings/footer`, `/settings/contact`.
- Run backend migration V5 and re-login to load the new menu tree.

## 2026-04-09 Menu Localization

- Added migration V6 to localize admin menus into Chinese.
- Service Lines page now provides real data cards and enters a dedicated editor for each line.

## 2026-04-09 Backend Startup

- Added backend/start-dev.ps1 and backend/start-dev.cmd for Windows development startup.
- The script forces backend startup to use JDK 21 and avoids Maven picking an older global JDK.
- If 8080 is occupied, run backend/start-dev.ps1 -Port 8081.
- Do not modify Flyway migrations that have already been applied to a database. Add a new migration instead.


## 2026-04-10 Service Line Editor

- Added backend endpoints GET /api/content/service-lines, GET /api/content/service-lines/{code}, PUT /api/content/service-lines/{code}/draft and PUT /api/content/service-lines/{code}/publish.
- Added frontend route /pages/service-lines/:code and a dedicated fixed-template editor page.
- Service Lines list page is now backed by real data instead of a placeholder explanation card.

## 2026-04-11 Contact Module

- Expanded the contact settings module into a richer fixed-form editor with hero, contact cards, office hours, promises and CTA content.
- Current contact module persistence still uses frontend local settings storage.
- Frontend development port is now 5174.


## 2026-04-11 Page Schema Alignment

- Calibrated backend /api/content/home responses to a schema-aligned form structure and kept frontend admin mapping compatibility.
- Calibrated backend /api/content/service-lines responses and the service-line editor to the service-line page schema.
- Homepage content management now includes news preview section fields required by the schema document.

