# 停车管理平台

Spring Boot 3.5.6 + JDK 17 + MyBatis-Plus + Sa-Token + 微信支付 V3，中间件通过 Docker 运行，预留 langchain4j AI 能力。

## 快速开始

```bash
# 1. 启动中间件（MySQL 3306 / Redis 6379）
docker compose up -d

# （可选）启动 Ollama 供后续 langchain4j 使用
docker compose --profile ai up -d

# 2. 启动应用
mvn spring-boot:run
```

数据库首次启动自动执行 `docker/mysql/init/01-schema.sql` 完成建表。

## 角色设计

| 角色 | 说明 | 存储表 |
|---|---|---|
| SUPER_ADMIN | 超级管理：创建车场、开通普通管理员、分配车场 | admin_user |
| ADMIN | 普通管理：查看/管理被分配的车场（信息、车辆、套餐、临停规则） | admin_user |
| USER | 小程序用户：微信登录，扫码绑定车场、添加/查看车辆、续费套餐 | wx_user |

初始超管账号：`admin / Admin@123456`（首次登录后请修改）。

## 功能清单

### 鉴权（双端分离）
- 后台：账号密码登录（BCrypt），角色 SUPER_ADMIN / ADMIN
- 小程序：code2session 登录，首次登录自动注册 `wx_user`
- Sa-Token 登录拦截（`/api/**`），放行登录/支付回调/道闸回调/扫码校验

### 超级管理（SUPER_ADMIN）
- 开通/禁用普通管理员账号
- 创建/编辑/删除车场（名称、位置、车位数、绑定上限、三档套餐价格、临停规则）
- 把车场分配给普通管理员（一个管理员可分配多个车场，全量覆盖式）

### 管理端（SUPER_ADMIN / ADMIN，数据隔离）
- 我的车场列表 + 详情（含绑定车辆数统计）
- 车场二维码内容生成（车场ID + MD5 防伪签名）
- 车场车辆列表、强制解绑

### 用户端（小程序 USER）
- 扫码校验车场（公开接口 `/api/lot/scan`）
- 扫码添加车辆（默认绑定扫码车场；车牌已存在则绑定；受 bind_limit 限制）
- 我的车辆、车辆详情（绑定人、最新套餐、到期时间、VALID/EXPIRED/NONE）
- 解绑：本人解绑自己；车主可解绑他人；车主解绑自己时自动转移车主；无绑定人时车辆作废
- 订阅续费：服务端按车场套餐价格计价 → 微信 JSAPI 支付 → 回调顺延

### 套餐订阅与续费顺延
- 绑定人（含非车主）均可续费
- 顺延规则：`new_expire = max(当前到期, now) + 套餐时长`，车辆级锁防并发重复计算
- 订阅记录查询（我的 / 按车辆）

### 临停（道闸/车牌识别回调，车场签名鉴权）
- 入场 `/api/temp/entry`、出场计费 `/api/temp/exit`
- 计费规则：免费时长 → 首时段 → 每小时 → 单日封顶 × 天数
- 缴费：`/api/pay/order`（bizType=TEMP_PARKING），支付成功回调更新已支付

### 支付（微信支付 V3 / 小程序 JSAPI）
- 服务端计价（不接受前端金额）、订单状态 CREATED→PENDING→PAID/CLOSED
- 回调 SDK 验签 + 幂等处理
- 支付成功业务流转：套餐顺延 / 临停置为已支付

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/login | 后台登录 |
| POST | /api/auth/wx-login | 小程序登录 |
| POST/GET/PUT/DELETE | /api/super/** | 超管：管理员、车场、分配 |
| GET | /api/lot-admin/my, /api/lot-admin/{lotId} | 管理端车场 |
| GET | /api/lot-admin/{lotId}/qrcode | 车场二维码 |
| GET | /api/lot/scan | 用户扫码校验（公开） |
| POST | /api/vehicle/add | 添加车辆 |
| GET | /api/vehicle/my, /api/vehicle/{id} | 我的车辆/详情 |
| POST | /api/vehicle/unbind | 解绑 |
| GET | /api/vehicle/lot/{lotId}/list | 管理端车辆列表 |
| POST | /api/subscription/order | 续费下单（返回支付参数） |
| GET | /api/subscription/my, /api/subscription/vehicle/{id} | 订阅记录 |
| POST | /api/pay/order | 统一下单（套餐/临停） |
| POST | /api/pay/notify | 微信回调（公开） |
| POST | /api/temp/entry, /api/temp/exit | 道闸进出场（公开+签名） |

## 目录结构

```
src/main/java/com/parking
├── common/        # 统一响应、异常
├── config/        # Sa-Token 配置、微信配置
├── controller/    # Auth / SuperAdmin / ParkingLot / Vehicle / Subscription / Pay / TempParking
├── service/       # 业务层
├── mapper/        # MyBatis-Plus
├── entity/        # 实体
└── dto/           # 请求/响应对象
```

## 已知边界（后续迭代）
- 多节点部署时续费锁需换 Redis 分布式锁
- 到期提醒、退款、对账未实现
- 道闸放行通知待硬件对接
- langchain4j AI 能力预留（pom 注释 + Ollama profile）

## 管理后台前端（parking-admin-web）

Vue 3 + Vite + Element Plus，对应后端管理端接口。

```bash
cd parking-admin-web
npm install
npm run dev        # http://localhost:5173，/api 代理到后端 8080
npm run build      # 产物输出 dist/
```

页面：
- 登录（后台账号，admin / Admin@123456）
- 概览：车场卡片（车位数、绑定车辆数、套餐、临停规则）
- 车场管理：列表、新建/编辑（基本信息+套餐+临停规则）、删除、二维码内容（超管）
- 车辆管理：按车场查看车辆、套餐状态、解绑
- 管理员管理（仅超管可见）：开通账号、禁用/启用、分配车场

部署：`npm run build` 后将 `dist/` 交给 Nginx，并将 `/api` 反向代理到后端 8080。

## 本地联调开关（无需真实微信凭证）

`application.yml -> parking`：

| 开关 | 作用 | 上线前 |
|---|---|---|
| `wechat-mini.mock: true` | 跳过 code2session，按 code 生成固定 openid（`mock_xxx`），小程序可直接登录 | 改 false |
| `wechat-pay.mock: true` | 跳过真实微信支付：下单返回 `packageVal=mock:<orderId>`，小程序调 `/api/pay/mock/notify` 直接完成支付，业务流转（订阅顺延/临停已支付）与真实回调同一条代码路径 | 改 false |

冒烟脚本示例（8081 测试实例）：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081 --parking.wechat-mini.mock=true"

# 1 管理员登录
curl -X POST localhost:8081/api/auth/login -H 'Content-Type: application/json' -d '{"username":"admin","password":"Admin@123456"}'
# 2 小程序 mock 登录
curl -X POST localhost:8081/api/auth/wx-login -H 'Content-Type: application/json' -d '{"code":"any-code"}'
# 3 扫码绑车 / 续费下单 / 模拟支付 / 查订阅
```

已验证通过：超管建场 → 二维码 → 公开扫码 → mock 绑车 → 续费下单（服务端计价 200 元）→ 模拟支付 → 到期 +1 月 →
再次续费顺延至 +2 月 → 临停入场/出场计费/缴费。
