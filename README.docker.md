# 玄枢 · 盲派八字命理平台 —— Docker 部署说明

将本项目打包为可部署到服务器的 Docker 镜像，使用 Docker Compose 一键编排
`后端(Spring Boot) + 前端(Vue/nginx)` 两个服务；数据库使用外部远程 MySQL，不在本 compose 内启动容器。

## 目录结构（新增/变更文件）

```
mingli-platform/
├── docker-compose.yml        # 编排：backend / frontend 两个服务（远程 MySQL）
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
- 准备一台**外部远程 MySQL 8**（自建或云数据库 RDS），并提前创建好 `mingli` 库、
  授予应用账号建表权限（后端启动时会执行 `schema.sql` / `data.sql` 自动建表）。

### 2. 配置环境变量（必填）
```bash
cp .env.example .env
# 编辑 .env，填入远程 MySQL 连接信息（DB_HOST / DB_PORT / DB_NAME / DB_USERNAME / DB_PASSWORD）
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
2. **数据库在外部远程 MySQL**：本 compose 不再包含 `mysql` 服务，请提前在远程 MySQL（自建 / 云数据库 RDS）中
   创建好 `mingli` 库并授予应用账号建表权限；后端启动时 `schema.sql`/`data.sql` 为幂等脚本
   （`IF NOT EXISTS` + `CREATE DATABASE IF NOT EXISTS`），不会清空字典表与 `bazi_record` 历史记录。
3. **启动顺序**：`backend` 直接连接远程 MySQL，不再依赖本地 `mysql` 容器健康检查；
   若远程库暂不可达，应用仍可启动并完成排盘与推理（规则字典回退内置默认），仅历史记录功能不可用。
4. **内存限制**：后端 `ENTRYPOINT` 固定 `-Xmx512m -Xms128m`。
   在内存受限的云主机上，JVM 默认会申请约 4GB 堆导致启动失败，此处已规避。
5. **敏感信息外部化**：Spring 数据源的 `DB_HOST`/`DB_PORT`/`DB_NAME`/`DB_USERNAME`/`DB_PASSWORD`
   全部由 `.env` 经 compose 注入，源码不含明文；生产务必通过 `.env` 提供真实远程库信息与强密码。

## 常用运维命令

```bash
docker compose ps                 # 查看服务状态
docker compose logs -f backend    # 跟踪后端日志
docker compose restart frontend   # 重启前端
docker compose down               # 停止服务
docker compose down -v            # 停止并删除匿名卷（本部署无持久数据卷，等效于 down）
```

## 安全建议（生产环境）

- 修改 `.env` 中的 `DB_PASSWORD` 为强密码。
- 远程 MySQL 的访问安全由云厂商安全组 / 服务器防火墙控制，务必仅允许应用所在主机 IP 访问 3306，避免数据库暴露公网。
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
  git commit -m "玄枢 平台：远程 MySQL + Docker Compose 部署"
  git remote add origin <你的仓库地址>
  git push -u origin main
  ```
- 服务器拉取后执行 `cp .env.example .env` 并填入真实远程库信息，再 `docker compose up -d --build`。
