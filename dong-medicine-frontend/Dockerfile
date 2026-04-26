# 多阶段构建

# 构建阶段
FROM node:20 AS builder

WORKDIR /build

# 配置npm国内镜像源
RUN npm config set registry https://registry.npmmirror.com

# 构建参数
ARG VITE_API_BASE_URL=/api
ARG VITE_KKFILEVIEW_URL=/kkfileview
ARG VITE_KKFILEVIEW_FILE_HOST=http://backend:8080

# 设置环境变量
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_KKFILEVIEW_URL=$VITE_KKFILEVIEW_URL
ENV VITE_KKFILEVIEW_FILE_HOST=$VITE_KKFILEVIEW_FILE_HOST

# 复制package.json
COPY package*.json ./

# 安装依赖（带重试）
RUN npm ci --legacy-peer-deps || npm ci --legacy-peer-deps || npm install --legacy-peer-deps

# 复制源代码
COPY . .

# 构建生产版本（增加Node内存限制）
ENV NODE_OPTIONS="--max-old-space-size=1024"
RUN npm run build

# 运行阶段
FROM nginx:1.25-alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 创建nginx缓存目录并设置权限（必须在切换用户之前）
RUN mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

# 以非root用户运行
USER nginx

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
