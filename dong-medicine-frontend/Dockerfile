FROM node:20-alpine AS deps

WORKDIR /app

COPY package*.json ./

RUN npm ci --registry=https://registry.npmmirror.com --prefer-offline --no-audit

FROM node:20-alpine AS builder

WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

ARG VITE_API_BASE_URL=/api
ARG VITE_KKFILEVIEW_URL=/kkfileview
ARG VITE_KKFILEVIEW_FILE_HOST=http://localhost:8080

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL \
    VITE_KKFILEVIEW_URL=$VITE_KKFILEVIEW_URL \
    VITE_KKFILEVIEW_FILE_HOST=$VITE_KKFILEVIEW_FILE_HOST \
    NODE_ENV=production

RUN npm run build

FROM nginx:1.25-alpine

ENV TZ=Asia/Shanghai

RUN apk add --no-cache curl tzdata && \
    cp /usr/share/zoneinfo/$TZ /etc/localtime && \
    apk del tzdata

COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html

RUN mkdir -p /var/cache/nginx /var/log/nginx && \
    chown -R nginx:nginx /var/cache/nginx /var/log/nginx /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=5s --start-period=3s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

LABEL org.opencontainers.image.title="Dong Medicine Frontend" \
      org.opencontainers.image.description="侗乡医药数字展示平台前端服务"

USER nginx

CMD ["nginx", "-g", "daemon off;"]
