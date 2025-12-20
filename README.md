# 🛒 Group Buy Platform (拼团购物交易平台)

- 在线接口文档：[点击查看 API 文档](group-buy-plataform.apifox.cn)  
- API 文档: docs/api文档
- 支付宝沙箱支付账户： tpgvta9856@sandbox.com
- 支付宝沙箱登录密码&支付密码：111111


## 一. 项目背景 (Project Background)

本项目不仅仅是一个电商业务系统，更是一次**全链路工程化实践**。在开发过程中，我并没有止步于 CRUD，而是从架构师的视角出发，解决了以下核心挑战：

* **复杂链路编排**：拼团业务涉及下单、库存锁定、风控校验、支付、结算、成团通知等复杂流程。为了解耦业务逻辑，我自主研发并集成了 **DAGFlow**（有向无环图任务编排框架），将线性的业务逻辑转化为可编排的图结构。
* **DevOps 与敏捷交付**：项目严格遵循 DevOps 理念。我利用 **GitHub Actions** 搭建了完整的 CI/CD 流水线，实现了“代码提交 -> 自动构建 Docker 镜像 -> 自动部署到云服务器”的无缝闭环。这保证了项目始终处于 **MVP (Minimum Viable Product)** 的可交付状态，任何特性的更新都能通过在线 API 实时体验。
* **高可用与动态治理**：通过引入自研的 **DCC (Dynamic Config Center)** 动态配置中心，系统具备了运行时的降级、熔断以及**流量染色**能力（如：为特权用户开启模拟成团通道），无需重启即可干预线上行为。

## 二。项目目标 (Project Goals)

## 三。 项目进展 (Progress)

## 四. 功能演示视频 (Demo Video)

本视频完整记录了用户在真实网络环境下，与部署在云端的 MVP 系统进行交互的全过程。

> **演示视频连接**： [点击观看项目完整演示 (MP4)](docs/url/demo_video_link.txt)  
> *(注：视频文件较大，请点击链接跳转观看或下载)*

**演示核心看点：**
1.  **全链路交易**：从微信扫码登录 -> 商品浏览 -> 下单锁定 -> 模拟支付 -> 结算成团 -> 接收 SSE 实时成团通知。
2.  **DCC 动态干预**：
    * 演示了通过 DCC 控制台修改配置，系统**无需重启**即刻生效。
    * 展示了利用 **特殊 Token** 结合 DCC 白名单，在单人操作下模拟“多人拼团成功”的场景（流量染色/特权通道）。
3.  **无感认证**：展示了 Token 过期后的自动刷新机制，用户体验流畅。

## 五. 技术栈 (Tech Stack)

| 领域 | 技术/组件 | 说明 |
| :--- | :--- | :--- |
| **核心框架** | Spring Boot 2.7.x | 基础脚手架 |
| **流程编排** | **DAGFlow (Self-Research)** | **自研组件**，用于下单、结算等长链路编排 |
| **架构设计** | DDD (Domain-Driven Design) | 领域驱动设计，划分 Trade, Order, Activity, Auth 等领域 |
| **数据存储** | MySQL + MyBatis | 核心业务数据持久化 |
| **缓存/中间件**| Redis (Redisson) | 分布式锁、DCC配置存储、缓存加速 |
| **异步/通知** | SSE (Server-Sent Events) | 实现服务端主动推送（扫码登录、成团通知），轻量级替代 WebSocket |
| **身份认证** | JWT + 微信 OAuth2 | 实现无感刷新 Token (7天免登录) |
| **DevOps** | Docker + Docker Compose | 容器化部署 |
| **CI/CD** | **GitHub Actions** | 自动化构建与部署流水线 |


## 🧩 六. 系统实现

### 4.1 全链路交互流程图 (System Interaction)

为了实现从登录到成团的完整闭环，系统采用了 **事件驱动** 与 **流程编排** 相结合的架构。下图展示了用户与各个微服务模块的交互全景：

*(此处建议插入 Mermaid 渲染图或指向 docs/images/system_architecture.png)*

### 4.2 微信公众号扫码登录 + 7天免登录 (WeChat OAuth2 + JWT)

本系统采用 Ticket 票据机制 解耦了前端设备与微信回调事件，利用 Redis 作为状态中转站，结合 双 Token (Access + Refresh) 策略，实现了从“扫码登录”到“长期无感认证”的完整闭环。

#### 1. **Ticket 机制 (关联上下文)**：

- 前端请求登录时，后端生成一个全局唯一的 `ticket`，并以此获取微信二维码。
- 这个 `ticket` 是连接 "前端设备" 与 "用户微信扫码事件" 的唯一桥梁。

#### 2. **异步回调与状态翻转**：
- 用户扫码后，微信服务器回调 `WeixinPortalController`。
- 后端根据回调中的 `OpenID` 进行登录/注册处理，生成 `RefreshToken(RT)` 和 `AccessToken(AT)`。
- **关键点**：后端将这个 `RT` 作为键，`用户ID`作为值 写入 Redis，用于`AT`过期刷新
#### 3. **前端轮询 (Polling)**：
- 前端拿着 `ticket` 不断询问后端 "是否登录？" (`LoginController#checkLogin`)。
- 一旦 Redis 中读到了 `ticket`，轮询结束，用户登录成功,返回`AT`和`RT`。
#### 4. **7 天无感认证 (Silent Auth)**：
- 代码中引入了 `Access Token` (短效) + `Refresh Token` (长效，7天) 机制。
- 当 `Access Token` 过期时，前端利用 `Refresh Token` 换取新的凭证，用户全程无感知，无需再次扫码。

#### 5. 完整时序图
![Auth Sequence Diagram](docs/images/auth.png "Auth Sequence Diagram")

---
### 下单，锁单，支付，结算

---
## 📚 沉浸式体验模式 (Demo Mode)
为了解决非开发人员（或无法使用微信测试号的用户）无法体验系统核心功能的问题，本项目设计了一套基于 DCC（动态配置中心） 的体验模式。

📺 演示视频：您可以查看 [点击观看项目完整演示 (MP4)](docs/url/demo_video_link.txt)   了解完整的演示操作。

该模式允许管理员通过“后门密钥”动态开启体验开关，并下发体验令牌。体验者无需扫码，即可通过特定的 Token 模拟任意用户身份，完成下单、拼团等全链路测试。

### 🧪 详细实验流程

请按照以下步骤使用 Postman / APIFox 或在线文档进行体验：

**1. 开启模拟模式 (管理员权限)**

- 使用 **DCC 管理密钥** (`dccAdminSecret`) 访问 DCC 查询接口。
- 找到 `demoTokenSwitch` 配置项，将其值更新为 `1`。
    - *接口示例*: `GET /api/v1/gbm/dcc/update_config?key=demoTokenSwitch&value=1`
    - *Header*: `Authorization: Bearer admin-key-2024` (默认管理密钥)

**2. 获取体验凭证**

- 在 DCC 配置列表中查询以下两项关键参数：
    - `demoTokenSecret`: 体验专用 Token（例如：`experience-token-vip-888`）。
    - `demoUserId`: 当前模拟的用户 ID。

**3. 业务功能体验**

- 复制 `demoTokenSecret` 的值。
- 在访问任意业务接口（如商品列表、下单、拼团）时，将该值填入请求头的 `Authorization` 字段。
- **效果**：网关将自动放行，并将您识别为配置中的 `demoUserId` 用户。

**4. 高级玩法：多用户身份切换**

- 在测试“拼团”等需要多人交互的场景时，无需准备多个微信号。
- **操作**：保持体验 Token 不变，通过 DCC 接口动态修改 `demoUserId` 的值（改为另一个随机 ID）。
- **结果**：再次发起请求时，系统即认为您是另一名新用户。您可以通过此方式一个人完成“开团-换身份-参团”的完整流程。


### ⚙️ 动态配置项说明

本功能完全基于 DCC 实现热更新，无需重启服务即可实时生效。

| **配置项 Key**    | **默认值**                           | **说明**                                                     |
| ----------------- |-----------------------------------| ------------------------------------------------------------ |
| `demoTokenSwitch` | `0`                               | **总开关**。`0`-关闭，`1`-开启。开启后允许使用体验 Token 访问。 |
| `demoTokenSecret` | `Bearer experience-token-vip-888` | **体验令牌**。请求 Header 中 `Authorization` 携带的值。可随时修改以重置访问权限。 |
| `demoUserId`      | `7736...96`                       | **虚拟身份**。系统将把使用体验令牌的请求视为该 UserID 的操作。 |
| `dccAdminSecret`  | `Bearer admin-key-2024`           | **管理钥匙**。仅用于访问 DCC 接口修改上述配置，拥有最高权限，请妥善保管。 |

💡 提示：线上环境建议默认关闭此开关。如需临时演示，可使用 DCC 管理密钥调用配置接口将 demoTokenSwitch 设为 1，演示结束后立即设回 0 以确保系统安全。

### 🛠 实验全流程图解
下图展示了从管理员开启环境、获取体验凭证到用户模拟身份切换的完整操作闭环：
![](docs/images/demoMode.png)

