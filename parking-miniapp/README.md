# 停车管理 - 用户端小程序

uni-app + Vue3 + TypeScript + Pinia（工程结构与 yqb-miniapp 一致），对接 parking 后端。

## 功能
- 微信一键登录（code2session，首次自动注册）
- 扫码绑定车场：uni.scanCode 识别 `lotId:sign` 二维码 → 展示车场信息 → 输入车牌绑定
- 我的车辆：套餐状态、续费/订阅（微信 JSAPI 支付，服务端计价）、解绑、订阅记录
- 订阅记录：按车辆或全部
- 我的：资料、退出

## 使用

```bash
npm install
npm run dev:mp-weixin        # 开发，导入 dist/dev/mp-weixin
npm run build:mp-weixin      # 构建，导入 dist/build/mp-weixin
```

1. 微信开发者工具导入 `dist/dev/mp-weixin`，填入自己的小程序 appid（`project.config.json` / `manifest.json`）
2. `src/utils/request/config.ts` 的 `BASE_URL` 改为后端地址（真机调试用局域网 IP，并在开发者工具勾选"不校验合法域名"）
3. 后端 `application.yml` 的微信支付 `app-id` 必须与小程序 appid 一致，否则无法拉起支付

## 支付流程
订阅页选择套餐 → `POST /api/subscription/order`（服务端计价）→ 返回支付参数 → `uni.requestPayment` → 微信回调后端 → 订阅顺延生效
