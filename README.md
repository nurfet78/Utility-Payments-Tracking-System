# Utility Payments Tracking System

Система учёта коммунальных платежей — Spring Boot приложение для портфолио.

## Технологии

- Java 21, Spring Boot 3.4, Spring Data JPA, Spring Security
- PostgreSQL 18, Flyway (миграции)
- Jakarta Validation, Lombok
- springdoc-openapi (Swagger UI)
- Testcontainers (интеграционные тесты)
- Docker, Docker Compose
- Frontend: чистый JavaScript (ES6+), CSS (Flexbox/Grid)
- Nginx (reverse proxy, раздача статики)

## Предметная область

Система учитывает платежи и показания счётчиков по фиксированному набору коммунальных услуг:

| Услуга             | Счётчик | Цифры |
|--------------------|---------|-------|
| Газ                | Да      | 4     |
| Вода               | Да      | 4     |
| Электроэнергия     | Да      | 5     |
| Домофон            | Нет     | —     |
| Отопление          | Нет     | —     |
| Экоресурсы         | Нет     | —     |
| Жилсервис          | Нет     | —     |
| Капитальный ремонт | Нет     | —     |

### Статус оплаты

Вычисляется динамически (не хранится в БД):

| Статус               | Условие                                         |
|----------------------|-------------------------------------------------|
| `PAID_THIS_MONTH`    | Есть хотя бы один платёж в текущем месяце        |
| `NOT_PAID_THIS_MONTH`| Нет платежей, текущая дата ≤ 25                  |
| `OVERDUE`            | Нет платежей, текущая дата > 25                  |

## REST API

```
GET  /services                         — справочник услуг (8 типов)
POST /accounts                         — создать лицевой счёт
GET  /accounts/{id}                    — получить лицевой счёт
GET  /accounts/with-status             — все счета со статусом оплаты
POST /accounts/{id}/payments           — зарегистрировать платёж
GET  /accounts/{id}/payments           — история платежей
PUT  /accounts/{id}/payments/{pid}     — редактировать платёж
DELETE /accounts/{id}/payments/{pid}   — удалить платёж
POST /accounts/{id}/readings           — передать показание счётчика
GET  /accounts/{id}/readings           — история показаний
PUT  /accounts/{id}/readings/{rid}     — редактировать показание
DELETE /accounts/{id}/readings/{rid}   — удалить показание
POST /api/auth/login                   — аутентификация (JSON: username, password)
GET  /api/auth/me                      — проверка статуса авторизации
POST /api/auth/logout                  — выход из системы
```

## Авторизация

Приложение защищено Spring Security (сессионная авторизация).

- Один пользователь, in-memory (без БД)
- Логин/пароль задаются через переменные окружения `APP_SECURITY_USERNAME` / `APP_SECURITY_PASSWORD`
- Фоллбэк: admin / admin (если переменные не заданы)
- При открытии любой страницы JS проверяет сессию (`GET /api/auth/me`), если не авторизован — редирект на `login.html`
- Cookie `JSESSIONID` передаётся автоматически
- Эндпоинт `/services` разрешён без авторизации (используется healthcheck)

## Запуск

### Docker Compose (рекомендуемый)

```bash
docker compose up --build
```

Первая сборка занимает несколько минут (скачивание зависимостей Maven). Повторные — быстрее за счёт кэша Docker.

| Сервис    | URL                                       |
|-----------|-------------------------------------------|
| Frontend  | http://localhost:3000                      |
| API       | http://localhost:8888                      |
| Swagger   | http://localhost:8888/swagger-ui.html      |

Управление:

```bash
docker compose down              # остановить все контейнеры
docker compose down -v           # остановить + удалить данные PostgreSQL
docker compose up --build        # пересобрать и запустить
docker compose logs backend      # логи бэкенда
docker compose logs frontend     # логи фронтенда
docker compose ps                # статус контейнеров
docker image prune               # удалить неиспользуемые образы
```

### Локальный запуск (без Docker Compose)

Требования: Java 21+, Docker (для PostgreSQL)

**1. Запустить PostgreSQL:**

```bash
docker compose up -d postgres
```

**2. Запустить приложение (профиль prod):**

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Или через IDE: Active Profile = `prod`.

API доступен на `http://localhost:8888`.

**3. Swagger UI (профиль dev):**

Swagger доступен только в dev-профиле:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Открыть: http://localhost:8888/swagger-ui.html

**4. Frontend (отдельно):**

```bash
cd frontend
npx serve .
```

Откроется на http://localhost:3000.

### Тесты

```bash
mvn test
```

Интеграционные тесты запускают PostgreSQL через Testcontainers (требуется запущенный Docker). Все тесты выполняются с `@WithMockUser` для корректной работы Spring Security.

## Развёртывание на VPS

Кратко:

```bash
# Локально: сохранить образы
docker save -o utilpay-images.tar paymentstrackingsystem-backend paymentstrackingsystem-frontend postgres:18

# На сервере: загрузить и запустить
docker load -i utilpay-images.tar
docker compose up -d
```

На сервере используется отдельный `compose.yaml` с `image:` вместо `build:` и `restart: always`.

## Профили

| Профиль | Назначение                                  |
|---------|---------------------------------------------|
| `prod`  | Продакшен: Swagger отключён                 |
| `dev`   | Разработка: Swagger UI, подробные логи      |
| `test`  | Тесты: Testcontainers, фиксированный Clock  |

## Архитектура

```
org.nurfet.paymentstrackingsystem
├── controller/     — REST-контроллеры (OpenAPI-аннотации) + AuthController
├── service/        — бизнес-логика и валидация
├── repository/     — Spring Data JPA
├── domain/         — сущности и enum
├── dto/            — request/response объекты (@Schema)
├── mapper/         — маппинг entity ↔ DTO
├── exception/      — BusinessException, GlobalExceptionHandler
└── config/         — SecurityConfig, OpenAPI, Clock, CORS

frontend/
├── index.html          — Dashboard: статусы, фильтр, создание счёта
├── account.html        — Детали: платежи, показания, CRUD (редактирование/удаление)
├── monthly.html        — Отчёт по месяцам: выбор месяца, сводка, детализация
├── login.html          — Страница входа (тёмная тема)
├── css/styles.css      — Дизайн-система (Industrial Utilitarian)
└── js/
    ├── api.js          — Фасад над API-клиентом
    ├── auth.js         — Проверка авторизации, редирект, кнопка «Выйти»
    ├── ui.js           — Переиспользуемые UI-компоненты
    ├── main.js         — Логика Dashboard
    ├── account-page.js — Логика страницы деталей + CRUD
    ├── monthly-page.js — Логика месячного отчёта
    └── generated-client/  — API-клиент (typescript-fetch интерфейс)
        └── apis/
            ├── UslugiApi.js
            ├── LitsevyeSchetaApi.js
            ├── PlatezhiApi.js
            └── PokazaniyaSchyotchikovApi.js

docker/
├── backend/Dockerfile  — Multi-stage: maven + eclipse-temurin:21
└── frontend/
    ├── Dockerfile      — nginx:alpine + статика + reverse proxy
    └── nginx.conf      — Проксирование /services, /accounts, /api/auth → backend

compose.yaml            — postgres + backend + frontend
DEPLOYMENT.md           — Инструкция по развёртыванию на VPS
```

## Frontend

Веб-интерфейс на чистом JavaScript (ES6+), без фреймворков.

Функциональность:

- Авторизация (логин-форма, сессии, кнопка «Выйти»)
- Создание лицевых счетов (модальная форма с выбором услуги)
- Таблица счетов со статусами оплаты (PAID / NOT_PAID / OVERDUE)
- Фильтрация по статусу
- Статистика (карточки с подсчётами)
- Детальная страница: история платежей и показаний
- CRUD: редактирование и удаление платежей/показаний (модальные формы с подтверждением)
- Месячный отчёт: выбор месяца, сводка по всем счетам, детализация по услугам
- Формы добавления платежей и показаний с клиентской валидацией
- Отображение серверных ошибок из ErrorResponse
- Индикаторы загрузки (спиннеры)
- Адаптивная вёрстка (desktop + mobile)
- Кэш-стратегия: HTML без кэша, JS/CSS с хэшами в именах (immutable, 1 год)

## Безопасность

- Spring Security: сессионная авторизация, CSRF отключён (REST API)
- Пароль задаётся через переменные окружения (не хардкод)
- PostgreSQL без внешнего порта в production (доступен только внутри Docker-сети)
- Nginx reverse proxy: фронтенд и бэкенд за одним портом
- CORS: `allowCredentials(true)` для передачи cookie
