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
docker compose up -d            # Start all 8 services (MySQL, Redis, RabbitMQ, KKFileView, backend, frontend, Prometheus, Grafana)
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

### Database (16 tables)
Core: `users`, `plants`, `knowledge`, `inheritors`, `resources`
Interaction: `comments`, `favorites`, `feedback`, `qa`, `quiz_questions`, `quiz_record`, `plant_game_record`
Tracking: `operation_log`, `browse_history`, `chat_history`, `search_history`

### Deployment
- Docker Compose 8 services on `dong-medicine-network`, only frontend (80), MySQL (3307), Grafana (3001), and RabbitMQ management (15672) exposed to host
- GitHub Actions CI/CD: test → build Docker images → push to GHCR → SSH deploy
- Deployment scripts in `deploy/` (docker-deploy.sh with rollback, init-server.sh, cleanup.sh)

## Conventions

### Backend
- The `common/R.java` response wrapper is used by all controllers — never return raw objects. Structure: `{code, msg, data, requestId}`
- Lombok is used extensively in entity/DTO classes (`@Data`, `@Builder`, etc.)
- Entity classes use MyBatis-Plus annotations (`@TableName`, `@TableField`); all extend `BaseEntity` which provides `id` (auto-increment), `createdAt`, `updatedAt` with auto-fill
- **Auth**: `SaTokenConfig` interceptor enforces login for ALL POST/PUT/DELETE/PATCH on `/api/**` by default. New write endpoints are automatically secured. To exempt a write endpoint (like `/api/user/login`), add it to the exclude list in `SaTokenConfig`. Use `@SaCheckLogin` / `@SaCheckRole("admin")` for additional checks. Admin controllers use `@SaCheckRole("admin")` at the class level
- **View endpoints use POST**: endpoints like `/api/plants/1/view` use POST and are explicitly excluded from auth — intentional so anonymous users can trigger view counts
- Paginated responses use `PageUtils.toMap(pageResult)` to convert MyBatis-Plus `Page` to `Map<String, Object>` for the `R.data` field
- DTOs follow `*CreateDTO` / `*UpdateDTO` naming for admin CRUD operations. Admin controllers use `BeanUtils.copyProperties()` to convert DTOs to entities
- After any admin content mutation, call `service.clearCache()` to evict the relevant cache region
- `BusinessException` has static factories: `notFound()`, `badRequest()`, `unauthorized()`, `forbidden()`, `conflict()` — use these instead of constructing directly. Error codes are domain-structured: 1xxx user, 2xxx resource, 3xxx param, 4xxx file, 5xxx operation, 6xxx infrastructure, 7xxx AI, 9xxx system
- Rate limiting via `@RateLimit(value = 5, key = "user_login")` annotation — tries Redis first, falls back to local token bucket
- Polymorphic interactions: comments, favorites, and browse history use `targetType` (enum: plant/knowledge/inheritor/resource/qa) + `targetId` to point at any content type
- RabbitMQ has 5 message types: OperationLog, Statistics, Feedback, FileProcess, Notification — producers in `mq/producer/`, consumers in `mq/consumer/`. Gracefully degrades when disabled (`app.rabbitmq.enabled=false`) — operation logs fall back to synchronous DB writes
- Entity deletion with file cleanup uses `deleteWithFiles()` which calls `FileCleanupHelper` to remove associated media from disk
- All sensitive config (DB creds, JWT secret, DeepSeek API key) comes from environment variables; the app loads `.env` files via dotenv-java before Spring Boot starts
- Static file uploads go to the backend's configured upload directory; paths are stored as relative URLs in the DB
- API route segments are defined as constants in `ApiPaths.java`

### Frontend
- Frontend API calls go through `src/utils/request.js` which adds `/api` prefix. The interceptor also: auto-retries GET requests with exponential backoff (max 3), deduplicates concurrent non-GET requests (aborting the earlier one), sanitizes all string data for XSS/SQL injection, and handles 401 token refresh with a queue pattern (concurrent 401s share one refresh call)
- **Login is a dialog, not a page**: `App.vue` manages login/register dialogs. Unauthenticated users are redirected to `/` with `needLogin=true` query param, not to `/login`
- **Two token formats**: the user store supports both UUID-style tokens (Sa-Token) and standard JWTs — `isLoggedIn` and `isAdmin` branch on this distinction
- **Design system**: dual CSS custom properties (`variables.css`) and SCSS variables (`_variables.scss`) with identical values. Both auto-injected via Vite's `additionalData` — every `<style lang="scss">` has access without imports
- **Custom directives**: 8 globally registered (`v-lazy`, `v-debounce`, `v-throttle`, `v-click-outside`, `v-focus`, `v-permission`, `v-loading`, `v-lazy-background`) — installed via `app.use(directives)` in `main.js`
- **Two-tier frontend cache**: `utils/cache.js` provides memory + sessionStorage cache with per-entity TTLs. Use `createCachedFetcher()` to wrap async data fetching
- **Barrel imports**: `components/business/index.js` re-exports all ~35 business components; `composables/index.js` exports 11 composables (7 others are imported directly by path)
- `v-permission` directive reads `role` from localStorage and removes DOM nodes if role doesn't match — admin always passes
- `provide/inject` in `App.vue` supplies `isLoggedIn`, `userName`, `updateUserState`, `showLoginDialog` to all descendants
- The Vite dev server proxies `/api`, `/images`, `/videos`, `/documents`, `/public` to `localhost:8080`, and `/kkfileview` to `localhost:8012`
- SCSS uses `@use` (not `@import`) with `api: "modern-compiler"` for variable/mixin injection

### Testing
- Backend tests use H2 in MySQL compatibility mode — no external DB needed
- Backend test tiers: `common/` + `service/impl/` + `controller/` (unit), `integration/` (integration with H2 via `BaseIntegrationTest`), `regression/` (bug regression with `@Nested` classes), `websocket/` (unit)
- Frontend E2E tests split into 9 spec files under `e2e/` by feature area (incl. ai-chat)
- Component unit tests: `__tests__/personal-center.test.js` covers ProfileSection, StatsDashboard, BrowseHistoryPanel
- Cache names in `CacheConfig` have per-entity TTLs: `plants`/`knowledges`/`inheritors` (6h), `resources` (4h), `users` (30m), `quizQuestions` (12h), `searchResults` (5m), `hotData` (1h)
