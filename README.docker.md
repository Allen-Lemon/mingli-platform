# 玄枢 · 盲派八字命理平台 —— Docker 部署说明

将本项目打包为可部署到服务器的 Docker 镜像，使用 Docker Compose 一键编排
`MySQL + 后端(Spring Boot) + 前端(Vue/nginx)` 三个服务。

## 目录结构（新增/变更文件）

```
mingli-platform/
├── docker-compose.yml        # 编排：mysql / backend / frontend 三个服务
├── .env.example              # 敏感配置样例（复制为 .env 使用）
├── mingli-backend/
│   ├── Dockerfile            # 多阶段：Maven 编译 → JRE 运行
│   └── .dockerignore
└── mingli-web/
    ├── Dockerfile            # 多阶段：Node 构建 dist → nginx 托管
    ├── nginx.conf            # 站点配置：SPA 回退 + /api 反向代理
    └── .dockerignore
```

## 部署步骤

### 1. 准备服务器环境
- 安装 Docker 与 Docker Compose v2（`docker compose` 子命令）。
- 将整个 `mingli-platform` 目录上传到服务器（或 `git clone`）。

### 2. 配置环境变量（可选但推荐）
```bash
cp .env.example .env
# 编辑 .env，修改 MYSQL_ROOT_PASSWORD 为强密码
```

### 3. 构建并启动
```bash
docker compose up -d --build
```
首次构建会下载基础镜像并编译后端，耗时数分钟；之后重启只需 `docker compose up -d`。

### 4. 验证
- 浏览器访问 `http://<服务器IP>/`，应看到排盘首页。
- 调用后端自检：`http://<服务器IP>/api/dict/all`，应返回规则字典数据。

## 关键设计说明

1. **/api 代理不剥离前缀**：后端控制器本身注册在 `/api` 下
   （`@RequestMapping("/api/dict")`、`"/api/bazi")`），因此 `nginx.conf` 的
   `proxy_pass http://backend:8080;` **结尾不带 `/`**，把 `/api/...` 原样转发。
   若误加 `/` 会导致所有接口 404。
2. **数据库自动建库**：`mysql` 服务通过 `MYSQL_DATABASE=mingli` 首次启动即建好库，
   后端 `schema.sql`/`data.sql` 为幂等脚本（`IF NOT EXISTS` + `CREATE DATABASE IF NOT EXISTS`），
   容器重启不会清空字典表，也不会清空 `bazi_record` 历史记录。
3. **启动顺序**：`backend` 用 `depends_on: mysql: condition: service_healthy`
   等待 MySQL 健康检查通过后再连库；MySQL 健康检查用 `mysqladmin ping`。
4. **内存限制**：后端 `ENTRYPOINT` 固定 `-Xmx512m -Xms128m`。
   在内存受限的云主机上，JVM 默认会申请约 4GB 堆导致启动失败，此处已规避。
5. **密码一致性**：`MYSQL_ROOT_PASSWORD` 通过 compose 变量同时注入 MySQL 与
   Spring 数据源，避免两处密码不一致连不上库。
6. **敏感信息全部外部化（已去除 H2，仅保留 MySQL）**：源码 `application.yml` 不再出现任何明文账号/密码，
   一律用 `${DB_HOST}` / `${MYSQL_ROOT_PASSWORD}` 等环境变量占位符；真实值在 `.env`
   （已被 `.gitignore` 忽略）中提供。compose 中默认值仅为占位 `changeme`，
   生产必须通过 `.env` 设置强密码，否则后端连不上库。

## 常用运维命令

```bash
docker compose ps                 # 查看服务状态
docker compose logs -f backend    # 跟踪后端日志
docker compose restart frontend   # 重启前端
docker compose down               # 停止（保留 mysql 数据卷）
docker compose down -v            # 停止并删除数据卷（慎用，会清空数据）
```

## 安全建议（生产环境）

- 修改 `.env` 中的 `MYSQL_ROOT_PASSWORD` 为强密码。
- 删除 `docker-compose.yml` 里 `mysql.ports` 的 `3306:3306` 映射，避免数据库暴露公网。
- 在服务器前置一层 HTTPS（如用 Caddy / Nginx 反代 + 证书），本 compose 仅提供 HTTP 80。
- 前端 80 端口若与服务器其他服务冲突，可改 `ports: - "8080:80"` 之类。

## 推送到 GitHub 前的敏感信息处理

- 仓库根 `.gitignore` 已忽略 `.env`（含真实密码）、`target/`、`node_modules/`、`dist/`、`.workbuddy/` 等，
  确认提交内容不会带出真实凭证。
- 提交前自检：仓库内**不应**出现 `.env` 真实文件；`application.yml` 仅有 `${...}` 占位符、无明文密码；
  `docker-compose.yml` 与 `.env.example` 仅含占位默认值（`changeme` / `mingli`）。
- 发布流程示例：
  ```bash
  git init            # 若尚未初始化
  git add .
  git commit -m "玄枢 平台：MySQL 单数据源 + Docker Compose 部署"
  git remote add origin <你的仓库地址>
  git push -u origin main
  ```
- 服务器拉取后执行 `cp .env.example .env` 并填入强密码，再 `docker compose up -d --build`。
