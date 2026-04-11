# qsd Admin Backend Plan

## 闃舵 0锛氬熀纭€澹冲瓙

- Vue 3 + TypeScript + Vite + Vue Router + Pinia + Element Plus + Tailwind CSS
- Java 21 + Spring Boot 3
- 鐧诲綍椤?- 鍚庡彴涓诲竷灞€
- 璺敱瀹堝崼
- 鍔ㄦ€佽彍鍗?- `/api` 寮€鍙戜唬鐞?
## 闃舵 1锛氳处鍙锋潈闄?
- 鐢ㄦ埛绠＄悊
- 瑙掕壊绠＄悊
- 鑿滃崟鏉冮檺閰嶇疆

## 闃舵 2锛氬伐浣滃彴

- 鏁版嵁姒傝鍗＄墖
- 寰呭姙浜嬮」
- 鏈€杩戞搷浣滆褰?
## 闃舵 3锛氬唴瀹圭鐞?
- 棣栭〉鍐呭绠＄悊琛ㄥ崟
- Banner 鍥剧墖缁勭鐞?- 杩愬崟杩借釜妯″潡閰嶇疆
- 涓昏惀涓氬姟妯″潡閰嶇疆
- 涓昏惀涓氬姟灏忔ā鍧楀浘鏍囥€佸悕绉般€佹弿杩般€佽矾鐢遍厤缃?- 涓€绔欏紡鏈嶅姟娴佺▼妯″潡閰嶇疆
- 鑱旂郴杞寲鍖洪厤缃?- SEO 閰嶇疆
- 鑽夌淇濆瓨涓庡彂甯冩祦绋?
楠屾敹鐐癸細

- 鍐呭绠＄悊椤靛畬鏁村睍绀?- 涓昏惀涓氬姟鎬婚瑙堝彧鏄剧ず鏈€鍚庢坊鍔犵殑涓€涓ā鍧?- 涓€绔欏紡鏈嶅姟娴佺▼鏈€澶氱淮鎶?7 涓楠?- 鍚庡彴鍙粴鍔ㄥ彸渚у唴瀹瑰尯

## 鍚庣画闃舵

- 鏂伴椈绠＄悊
- 绱犳潗搴?- 绾跨储绠＄悊
- 杩愬崟绠＄悊
- 瀹㈡埛绠＄悊
- 鏃ュ織涓庣郴缁熻缃?
## 2026-04-09 Preview Alignment

- 首页内容管理继续沿用左侧表单、右侧预览的双栏结构。
- 右侧预览按模块拆卡并与左侧模块顺序对齐。
- 主营业务总预览保留最后新增模块的满宽展示。

## 2026-04-09 Content Backend

- Persist homepage content JSON in backend table `site_content_page`.
- Keep current API scope to home page draft and publish operations.
- Continue using frontend form structure as the stored JSON document shape.

## 2026-04-09 Seed Data

- Provide a reusable SQL seed file for homepage content preview and manual testing.

## 2026-04-09 Promise Section

- Keep homepage content management as a pure form editing page without preview cards.
- Persist a fixed six-item promise section in the same homepage JSON document.

## 2026-04-09 News Module

- Continue homepage content management without the contact section.
- Start the first version of news management as a CRUD admin module.
- Keep dev environment reachable on LAN with hot reload enabled.

## 2026-04-09 Block News Form

- Keep backend news table unchanged and serialize block content into `content`.
- Continue evolving admin news editing around ordered content blocks.

## 2026-04-09 Menu Restructure

- Keep admin navigation aligned with page types instead of a single content menu.
- Use grouped menus for page management and global settings.

## 2026-04-09 Menu Localization

- Keep menu labels Chinese across both fresh migrations and upgraded databases.
- Treat Service Lines as a management entry page until the dedicated line template editor is implemented.

## 2026-04-09 Backend Startup

- Standardize backend local startup through backend/start-dev.ps1.
- Keep Java runtime fixed at 21 during development startup.
- Keep Flyway migration history append-only after a migration has been applied.


## 2026-04-10 Service Line Editor

- Service Lines has moved from entry placeholder to an editable fixed-template page module.
- Keep line-template editing constrained to content fields and repeated module items, not free-form layout assembly.

## 2026-04-11 Contact Module

- Keep global contact settings aligned with a full contact-page module instead of a minimal contact info form.
- Use 5174 as the default frontend development port.


## 2026-04-11 Page Schema Alignment

- Keep admin content persistence aligned with the external frontend page schema instead of ad hoc field groups.
- Service-line management should continue evolving around the normalized public page data contract.

