# 校园二手交易市场

基于 Spring Boot 3 + Vue 3 + MyBatis Plus + MySQL 的校园二手物品交易平台，覆盖实验报告模板中的必选与部分可选功能。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2、MyBatis Plus、JWT、EasyCaptcha、BCrypt |
| 前端 | Vue 3、Vite、Element Plus、Pinia、Vue Router、Axios |
| 数据库 | MySQL 8 |

## 快速启动

### 1. 数据库（必做，否则接口会 500）

在 MySQL 中执行初始化脚本（会创建 `2023011345` 库及全部表）：

```bash
mysql -u root -p123456 < sql/init.sql
```

确认 `backend/src/main/resources/application.yml` 与本地 MySQL 一致，当前默认为：

- 库名：`2023011345`
- 用户：`root`
- 密码：`123456`

执行后可在浏览器访问 http://localhost:8080/api/health ，应返回 `{"status":"ok",...}`。

### 2. 后端

```bash
cd backend
mvn spring-boot:run
```

默认端口 `8080`。管理员账号：`admin` / `admin123`（首次启动自动初始化）。

### 3. 前端

```bash
cd frontend
npm install
npm run dev
```

访问 http://localhost:5173

## 项目结构

```
ershou/
├── sql/init.sql          # 数据库脚本
├── backend/              # Spring Boot 后端
├── frontend/             # Vue 3 前端
└── docs/功能说明.md       # 功能与文件说明（实验报告参考）
```

## 默认账号

- 管理员：`admin` / `admin123`
- 普通用户/商家：注册后需管理员审核通过方可登录

详细功能说明见 [docs/功能说明.md](docs/功能说明.md)。
