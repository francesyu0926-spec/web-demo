# 观点科技电子招投标交易平台 — 后端

基于《研发需求文档 V1.4》的 Spring Boot 3 后端工程。

## 技术栈

- Java 11 / Spring Boot 2.7（适配现有 JDK 11 环境）
- Spring Security + JWT（无状态鉴权，支持多角色切换）
- MyBatis-Plus 3.5（分页、逻辑删除）
- MySQL 8 / Redis / MinIO
- Knife4j (OpenAPI3) 接口文档

## 目录结构

```
docs/                设计文档（总览 / 数据库 / 接口）
sql/schema.sql       建库建表脚本（可直接执行）
src/main/java/com/guandian/bidding
├─ common/api        统一响应 R / ResultCode / PageResult
├─ common/exception  业务异常 + 全局异常处理
├─ config            MyBatis-Plus / OpenAPI / MinIO
├─ security          JWT 工具 / 过滤器 / SecurityConfig
└─ controller        PingController（健康检查）
```

## 快速开始

1. 准备 MySQL 8，执行建表脚本：
   ```bash
   mysql -u root -p < sql/schema.sql
   ```
2. 按需修改 `src/main/resources/application.yml` 中的数据库 / Redis 连接（生产环境用环境变量注入 `app.jwt.secret`）。
3. 启动：
   ```bash
   mvn spring-boot:run
   ```
4. 验证：
   - 健康检查：`GET http://localhost:8080/api/ping`
   - 接口文档：`http://localhost:8080/doc.html`

> 提示：为方便调试，未连接数据库 / Redis / MinIO 时服务仍可启动（Hikari `initialization-fail-timeout=-1`，MinIO 默认关闭）。

## 鉴权说明

- 登录后获得 JWT，请求头携带 `Authorization: Bearer <token>`。
- Token 内含 `userId / username / role(当前激活角色)`；角色切换后重新签发。
- 接口权限用 `@PreAuthorize("hasRole('MANAGER')")` 控制（角色码见 `docs/00-总览.md`）。

## 后续开发顺序

用户域（登录/注册/角色切换）→ 招标项目 → 报名缴费 → 投标加解密 → 开评标。详见 `docs/00-总览.md`。
