# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Dong Medicine (dong-medicine-platform) — a digital exhibition platform for Dong ethnic minority traditional medicine. Full-stack app with **Vue 3 frontend** and **Spring Boot backend**, front-back separation architecture.

## Commands

### Backend (from `dong-medicine-backend/`)
```bash
./mvnw spring-boot:run          # Run dev server (port 8080)
./mvnw test -B                  # Run all tests (uses H2 in-memory DB)
./mvnw test -B -pl . -Dtest=ClassName  # Run single test class
./mvnw test -B -pl . -Dtest=ClassName#methodName  # Run single test method
mvn clean package -DskipTests   # Build JAR
```

### Frontend (from `dong-medicine-frontend/`)
```bash
npm run dev          # Dev server on port 5173 (proxies /api to backend)
npm run build        # Production build
npm run test         # Vitest watch mode
npm run test:run     # Vitest single run
npm run test:coverage # Vitest with coverage
npm run test:e2e     # Playwright E2E tests (needs backend on localhost:3000)
npm run lint         # ESLint
```

### Full Stack (Docker)
```bash
docker compose up -d            # Start all 6 services (MySQL, Redis, RabbitMQ, KKFileView, backend, frontend)
```
Copy `.env.example` to `.env` and fill in secrets before running.

## Architecture

### Request Flow
```
Browser → Nginx (port 80/3000)
  ├─ /            → Vue SPA static files
  ├─ /api/*       → Spring Boot backend (port 8080)
  ├─ /ws/*        → WebSocket to backend (AI chat streaming via DeepSeek API)
  ├─ /images/     → Backend static file serving
  ├─ /videos/     → Backend static file serving (Range/byte-range support)
  ├─ /documents/  → Backend static file serving
  └─ /kkfileview/ → KKFileView document preview (port 8012)
```

### Backend (`dong-medicine-backend/`)
- **Java 17 / Spring Boot 3.1.12** with Maven
- **Auth**: Sa-Token with JWT mode, `@SaCheckLogin` / `@SaCheckRole` annotations on controller methods
- **ORM**: MyBatis-Plus (`com.dongmedicine.mapper/`)
- **DB**: MySQL 8.0 (`dong_medicine`), schema + seed data in `sql/dong_medicine.sql`
- **Cache**: Redis 7 + Caffeine (two-level cache via `CacheConfig`)
- **MQ**: RabbitMQ for async operation logs, statistics, feedback, file processing, notifications (`com.dongmedicine.mq/`)
- **AI Chat**: WebSocket handler (`com.dongmedicine.websocket.ChatWebSocketHandler`) + Spring WebFlux WebClient streaming to DeepSeek API
- **API Docs**: Swagger UI via SpringDoc/OpenAPI
- **Unified response**: `R.java` wraps all responses as `{code, msg, data, requestId}`
- **AOP cross-cutting**: `OperationLogAspect` (audit logs), `RateLimitAspect` (rate limiting), `LoggingAspect` (perf logging), `XssFilter` (XSS protection) — all in `config/`
- **Service pattern**: Interface in `service/` with implementation in `service/impl/`
- **Profiles**: `application-dev.yml` (local), `application-prod.yml` (Docker), `application-test.yml` (H2 in-memory, no RabbitMQ)
- **Docker entrypoint**: `entrypoint.sh` waits for MySQL/Redis/RabbitMQ to be ready and auto-initializes the DB on first run

### Frontend (`dong-medicine-frontend/`)
- **Vue 3.4 + Vite 5** with Element Plus UI (Chinese locale)
- **State**: Pinia (single store: `stores/user.js`)
- **Router**: 15 routes with auth guards (`/personal` requires login, `/admin` requires admin role)
- **Composables**: Business logic in `src/composables/` (useAdminData, useFavorite, useQuiz, useChatWebSocket, useChatSessions, useStudyStats, useBrowseHistory, etc.)
- **HTTP**: Axios instance in `src/utils/request.js` — handles auth tokens, error codes, token refresh, request deduplication, automatic retry (exponential backoff, max 3)
- **XSS**: DOMPurify sanitization via `src/utils/xss.js`; request-level XSS/SQL injection detection in `request.js`
- **Charts**: ECharts for data visualization
- **AI Chat**: WebSocket connection with Marked for markdown rendering
- **Path alias**: `@` maps to `src/` directory
- **SCSS**: Variables and mixins are auto-injected via Vite config — no manual imports needed

### Database (13+ tables)
Core: `user`, `plant`, `knowledge`, `inheritor`, `resource`
Interaction: `comment`, `favorite`, `feedback`, `qa`, `quiz_question`, `quiz_record`, `plant_game_record`
Tracking: `operation_log`, `browse_history`, `chat_history`

Schema + seed data: `dong-medicine-backend/sql/dong_medicine.sql` (290KB)

### Deployment
- Docker Compose 6 services on `dong-medicine-network`: MySQL (3307), Redis, RabbitMQ, KKFileView, Backend (8080), Frontend (80) — only frontend (80) and MySQL (3307) exposed to host
- GitHub Actions CI/CD: test-backend → test-frontend → build Docker images → push to GHCR → SSH deploy
- Deployment scripts in `deploy/` (docker-deploy.sh with rollback, init-server.sh, cleanup.sh)
- Copy `.env.example` to `.env` and fill in secrets before Docker Compose

## Conventions

- Backend tests use H2 in MySQL compatibility mode — no external DB needed
- Frontend API calls go through `src/utils/request.js` which adds `/api` prefix
- The `common/R.java` response wrapper is used by all controllers — never return raw objects
- Lombok is used extensively in entity/DTO classes (`@Data`, `@Builder`, etc.)
- Entity classes use MyBatis-Plus annotations (`@TableName`, `@TableField`)
- Sa-Token annotations (`@SaCheckLogin`, `@SaCheckRole`) handle auth on controller methods
- RabbitMQ producers in `mq/producer/` send messages; corresponding consumers in `mq/consumer/` process them
- Static file uploads go to the backend's configured upload directory; paths are stored as relative URLs in the DB
- Frontend components organized: `base/` (ErrorBoundary, VirtualList, BaseDetailDialog), `business/` (admin, dialogs, display/ai-chat, interact, layout, media, upload), `common/` (skeleton loaders)
- Large views split into sub-components: `views/personal-center/` (6 sub-components), `components/business/display/ai-chat/` (5 sub-components)
- PurgeCSS removes unused CSS at production build time (configured in `vite.config.js`)
- E2E tests split into 8 spec files under `e2e/` (page-load, search, api, auth, navigation, regression, responsive, websocket)
- Backend regression tests split by bug category under `regression/` (Pagination, XSS, Auth, Feedback, General)
