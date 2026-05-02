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
  ├─ /ws/*        → WebSocket to backend (AI chat streaming)
  ├─ /images/     → Backend static file serving
  └─ /kkfileview/ → KKFileView document preview (port 8012)
```

### Backend (`dong-medicine-backend/`)
- **Java 17 / Spring Boot 3.1.12** with Maven
- **Auth**: Sa-Token with JWT mode
- **ORM**: MyBatis-Plus (`com.dongmedicine.mapper/`)
- **DB**: MySQL 8.0 (`dong_medicine`), schema + seed data in `sql/dong_medicine.sql`
- **Cache**: Redis 7 + Caffeine — true two-level cache (Caffeine L1 in-memory → Redis L2 distributed), see `CacheConfig.TwoLevelCacheManager`
- **MQ**: RabbitMQ for async operation logs, statistics, feedback, file processing, notifications (`com.dongmedicine.mq/`)
- **AI Chat**: WebSocket handler (`com.dongmedicine.websocket.ChatWebSocketHandler`) + Spring WebFlux WebClient streaming to DeepSeek API
- **API Docs**: Swagger UI via SpringDoc/OpenAPI
- **Unified response**: `R.java` wraps all responses as `{code, msg, data, requestId}`
- **AOP cross-cutting**: `OperationLogAspect` (audit logs), `RateLimitAspect` (rate limiting), `LoggingAspect` (perf logging), `XssFilter` (XSS protection)
- **Profiles**: `application-dev.yml` (local), `application-prod.yml` (Docker), `application-test.yml` (H2 in-memory, no RabbitMQ)

### Frontend (`dong-medicine-frontend/`)
- **Vue 3.4 + Vite 5** with Element Plus UI (Chinese locale)
- **State**: Pinia (single store: `stores/user.js`)
- **Router**: 14 routes with auth guards (`/personal` requires login, `/admin` requires admin role)
- **Composables**: Business logic in `src/composables/` (useAdminData, useFavorite, useQuiz, useChatWebSocket, useChatSessions, useStudyStats, useBrowseHistory, etc.)
- **HTTP**: Axios instance in `src/utils/request.js` — handles auth tokens, error codes, token refresh
- **XSS**: DOMPurify sanitization via `src/utils/xss.js`
- **Charts**: ECharts for data visualization
- **AI Chat**: WebSocket connection with Marked for markdown rendering

### Database (13+ tables)
Core: `user`, `plant`, `knowledge`, `inheritor`, `resource`
Interaction: `comment`, `favorite`, `feedback`, `qa`, `quiz_question`, `quiz_record`, `plant_game_record`
Tracking: `operation_log`, `browse_history`, `chat_history`

### Deployment
- Docker Compose 6 services on `dong-medicine-network`, only frontend (80) and MySQL (3307) exposed to host
- GitHub Actions CI/CD: test → build Docker images → push to GHCR → SSH deploy
- Deployment scripts in `deploy/` (docker-deploy.sh with rollback, init-server.sh, cleanup.sh)

## Conventions

- Backend tests use H2 in MySQL compatibility mode — no external DB needed
- Frontend API calls go through `src/utils/request.js` which adds `/api` prefix
- The `common/R.java` response wrapper is used by all controllers — never return raw objects
- Lombok is used extensively in entity/DTO classes (`@Data`, `@Builder`, etc.)
- Entity classes use MyBatis-Plus annotations (`@TableName`, `@TableField`)
- The `@SaCheckLogin` / `@SaCheckRole` annotations handle auth on controller methods
- RabbitMQ producers in `mq/producer/` send messages; corresponding consumers in `mq/consumer/` process them
- Large views split into sub-components: `views/personal-center/` (6), `components/business/display/ai-chat/` (5), `views/home/` (7), `views/global-search/` (1)
- E2E tests split into 9 spec files under `e2e/` by feature area (incl. ai-chat)
- Backend regression tests split by bug category under `regression/` (5 files)
- Component unit tests: `__tests__/personal-center.test.js` covers ProfileSection, StatsDashboard, BrowseHistoryPanel
- Static file uploads go to the backend's configured upload directory; paths are stored as relative URLs in the DB
