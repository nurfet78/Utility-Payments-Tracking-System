# Utility Payments Tracking System

Система учёта коммунальных платежей — Spring Boot приложение для портфолио.

## Технологии

- Java 21, Spring Boot 3.4, Spring Data JPA
- PostgreSQL 18, Flyway (миграции)
- Jakarta Validation, MapStruct, Lombok
- springdoc-openapi (Swagger UI) — только в dev-профиле
- Testcontainers (интеграционные тесты)
- Docker, Docker Compose
- Frontend: чистый JavaScript (ES6+), CSS (Flexbox/Grid)

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
```

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
| Postgres  | localhost:5432                             |

Управление:

```bash
docker compose down              # остановить все контейнеры
docker compose down -v           # остановить + удалить данные PostgreSQL
docker compose up --build        # пересобрать и запустить
docker compose logs backend      # логи бэкенда
docker compose logs frontend     # логи фронтенда
docker compose ps                # статус контейнеров
docker image prune               # удалить неиспользуемые образы (освободить место)
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

Интеграционные тесты запускают PostgreSQL через Testcontainers (требуется запущенный Docker).

## Профили

| Профиль | Назначение                                  |
|---------|---------------------------------------------|
| `prod`  | Продакшен: Swagger отключён                 |
| `dev`   | Разработка: Swagger UI, подробные логи, flyway.clean() разрешён |
| `test`  | Тесты: Testcontainers, фиксированный Clock  |

## Архитектура

```
org.nurfet.paymentstrackingsystem
├── controller/     — REST-контроллеры (OpenAPI-аннотации)
├── service/        — бизнес-логика и валидация
├── repository/     — Spring Data JPA
├── domain/         — сущности и enum
├── dto/            — request/response объекты (@Schema)
├── mapper/         — MapStruct маппинг entity ↔ DTO
├── exception/      — BusinessException, GlobalExceptionHandler
└── config/         — OpenAPI, Clock, CORS, Jackson

frontend/
├── index.html          — Dashboard: статусы, фильтр, создание счёта
├── account.html        — Детали: платежи, показания, формы
├── css/styles.css      — Стили
└── js/
    ├── api.js          — Фасад над API-клиентом
    ├── ui.js           — Переиспользуемые UI-компоненты
    ├── main.js         — Логика Dashboard
    ├── account-page.js — Логика страницы деталей
    └── generated-client/  — API-клиент (typescript-fetch интерфейс)

docker/
├── backend/Dockerfile  — Multi-stage: maven + eclipse-temurin:21
└── frontend/
    ├── Dockerfile      — nginx:alpine + статика + reverse proxy
    └── nginx.conf      — Проксирование /services, /accounts → backend

compose.yaml            — postgres + backend + frontend
```

## Frontend

Веб-интерфейс на чистом JavaScript (ES6+), без фреймворков.

Функциональность:
- Создание лицевых счетов (модальная форма с выбором услуги)
- Таблица счетов со статусами оплаты (PAID / NOT_PAID / OVERDUE)
- Фильтрация по статусу
- Статистика (карточки с подсчётами)
- Детальная страница: история платежей и показаний
- Формы добавления платежей и показаний с клиентской валидацией
- Отображение серверных ошибок из ErrorResponse
- Индикаторы загрузки (спиннеры)
- Адаптивная вёрстка (desktop + mobile)