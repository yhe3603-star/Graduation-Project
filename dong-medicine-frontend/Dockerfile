# 多阶段构建

# 构建阶段
FROM node:20 AS builder

WORKDIR /build

RUN npm config set registry https://registry.npmmirror.com && \
    npm config set fetch-timeout 600000 && \
    npm config set fetch-retry-mintimeout 20000 && \
    npm config set fetch-retry-maxtimeout 120000

ARG VITE_API_BASE_URL=/api
ARG VITE_KKFILEVIEW_URL=/kkfileview
ARG VITE_KKFILEVIEW_FILE_HOST=http://backend:8080

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_KKFILEVIEW_URL=$VITE_KKFILEVIEW_URL
ENV VITE_KKFILEVIEW_FILE_HOST=$VITE_KKFILEVIEW_FILE_HOST

COPY package*.json ./

RUN npm ci --legacy-peer-deps --prefer-offline || \
    (echo "npm ci failed, trying npm install..." && \
     npm install --legacy-peer-deps) || \
    (echo "Retrying with clean cache..." && \
     npm cache clean --force && \
     npm install --legacy-peer-deps)

COPY . .

ENV NODE_OPTIONS="--max-old-space-size=1024"

RUN npm run build || \
    (echo "Build failed, retrying..." && \
     npm run build)

RUN if [ ! -d "dist" ] || [ ! "$(ls -A dist 2>/dev/null)" ]; then \
        echo "ERROR: Build output is empty!"; \
        exit 1; \
    fi

# 运行阶段
FROM nginx:1.25-alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /build/dist /usr/share/nginx/html

RUN mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

USER nginx

CMD ["nginx", "-g", "daemon off;"]
