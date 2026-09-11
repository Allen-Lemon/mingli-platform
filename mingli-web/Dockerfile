# 玄枢 · 前端（Vue3 + Vite）镜像 —— 多阶段构建
# 第 1 阶段用 Node 编译出 dist，第 2 阶段用 nginx 托管静态文件并做反向代理。

# ---- 1) 构建阶段 ----
FROM node:20-alpine AS build
WORKDIR /app
# 仅拷贝依赖描述，利用 Docker 层缓存（package-lock.json 不存在时忽略）
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build

# ---- 2) 运行阶段 ----
FROM nginx:alpine
# 用自定义主配置覆盖默认配置（含 pid /tmp 修复 + /api 反向代理 + SPA 回退）
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
