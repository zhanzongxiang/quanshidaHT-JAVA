# qsd Admin Backend TODO

## 鍐呭绠＄悊

- 灏嗛椤靛唴瀹圭鐞嗕粠鏈湴瀛樺偍 mock 鎺ュ叆姝ｅ紡鍚庣鎺ュ彛
- 鏄庣‘ Banner 鍥剧墖缁勭殑鎺ュ彛缁撴瀯
- 鏄庣‘杩愬崟杩借釜妯″潡寮€鍏充笌瀛楁
- 鏄庣‘涓昏惀涓氬姟妯″潡寮€鍏炽€佹ā鍧楅厤缃笌涓氬姟灏忔ā鍧楃殑鎺ュ彛缁撴瀯
- 鏄庣‘涓€绔欏紡鏈嶅姟娴佺▼妯″潡寮€鍏炽€佹爣棰樸€佸壇鏍囬鍜屾楠ょ粨鏋?- 璁捐涓氬姟鍥炬爣璧勬簮鐨勫瓨鍌ㄤ笌寮曠敤鏂瑰紡
- 鍥炲綊楠岃瘉涓昏惀涓氬姟鎬婚瑙堝彧鏄剧ず鏈€鍚庢坊鍔犳ā鍧?- 鍥炲綊楠岃瘉涓€绔欏紡鏈嶅姟娴佺▼姝ラ涓婇檺涓?7

## 鍩虹璁炬柦

- 鍥哄寲鍓嶅悗绔妧鏈爤鍜岀洰褰曠粨鏋?- 瀹屽杽缁熶竴璇锋眰灞傘€侀敊璇鐞嗗拰娑堟伅鍙嶉鏈哄埗
- 琛ラ綈 403銆?04銆佺┖鐘舵€併€佸姞杞界姸鎬侀〉闈?- 鍥炲綊楠岃瘉鍚庡彴甯冨眬鍙粴鍔ㄥ彸渚у唴瀹瑰尯

## 涓氬姟妯″潡

- 鏂伴椈绠＄悊
- 绱犳潗搴?- 绾跨储绠＄悊
- 杩愬崟绠＄悊
- 瀹㈡埛绠＄悊
- 鏃ュ織涓庤缃?
## 2026-04-09 Done

- 首页内容管理预览全部收敛到右侧。
- 右侧预览拆分为 Banner、运单查询、主营业务、一站式服务、联系转化、SEO 独立块。
- 主营业务预览固定显示“查看更多”按钮，不直接显示路由值。
- 主营业务预览仅显示最后新增模块，并占满预览块宽度。

## 2026-04-09 Done

- Added backend home content controller, service, mapper, entity, DTO and migration.
- Added endpoints for fetch, draft save and publish.
- Switched frontend home content API from localStorage to backend `/api/content/home*`.

## 2026-04-09 Done

- Added manual seed SQL file `backend/sql/home_content_seed.sql`.

## 2026-04-09 Done

- Removed homepage content preview blocks.
- Added fixed six-item promise section editing.
- Updated homepage seed SQL with promise section defaults.

## 2026-04-09 Done

- Removed contact section from homepage content management.
- Added news management page, API client, backend controller/service/mapper/entity and migration.
- Added Vite LAN host and HMR dev settings.

## 2026-04-09 Done

- Switched news editor from single textarea to block-based form.
- Added block parsing and serialization in `frontend/src/api/news.ts`.

## 2026-04-09 Done

- Added nested admin menu rendering.
- Added page management and global settings route pages.
- Added backend migration V5 for new menu structure.

## 2026-04-09 Done

- Added Chinese menu localization migration V6.
- Clarified Service Lines page as a template entry page instead of a completed editor.

## 2026-04-09 Done

- Added Windows backend startup scripts backend/start-dev.ps1 and backend/start-dev.cmd.
- Fixed Flyway startup failure by moving menu localization into V6 and keeping applied migrations immutable.

