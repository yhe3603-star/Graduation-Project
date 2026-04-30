FROM node:20-alpine AS builder

WORKDIR /app

COPY package*.json ./

RUN npm ci --registry=https://registry.npmmirror.com

COPY . .

ARG VITE_API_BASE_URL=/api
ARG VITE_KKFILEVIEW_URL=/kkfileview
ARG VITE_KKFILEVIEW_FILE_HOST=http://localhost:8080

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL
ENV VITE_KKFILEVIEW_URL=$VITE_KKFILEVIEW_URL
ENV VITE_KKFILEVIEW_FILE_HOST=$VITE_KKFILEVIEW_FILE_HOST

RUN npm run build

FROM nginx:1.25-alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY default.conf /etc/nginx/conf.d/default.conf

COPY --from=builder /app/dist /usr/share/nginx/html

RUN mkdir -p /var/cache/nginx && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
  CMD curl -f http://localhost/ || exit 1

USER nginx

CMD ["nginx", "-g", "daemon off;"]
