# 多阶段构建

# 构建阶段
FROM node:20-alpine AS builder

WORKDIR /build

# 配置npm国内镜像源
RUN npm config set registry https://registry.npmmirror.com

# 构建参数
ARG VITE_API_BASE_URL=http://localhost:8080/api
ARG VITE_KKFILEVIEW_URL=/kkfileview
ARG VITE_KKFILEVIEW_FILE_HOST=http://backend:8080

# 设置环境变量
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_KKFILEVIEW_URL=$VITE_KKFILEVIEW_URL
ENV VITE_KKFILEVIEW_FILE_HOST=$VITE_KKFILEVIEW_FILE_HOST

# 复制package.json
COPY package*.json ./

# 安装依赖
RUN npm ci --legacy-peer-deps

# 复制源代码
COPY . .

# 构建生产版本
RUN npm run build

# 运行阶段
FROM nginx:alpine

# 配置Alpine国内镜像源
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.aliyun.com/g' /etc/apk/repositories

# 安装必要工具
RUN apk add --no-cache tzdata

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 复制nginx配置
COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

# 从构建阶段复制构建产物
COPY --from=builder /build/dist /usr/share/nginx/html

# 暴露端口
EXPOSE 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

# 启动nginx
CMD ["nginx", "-g", "daemon off;"]
