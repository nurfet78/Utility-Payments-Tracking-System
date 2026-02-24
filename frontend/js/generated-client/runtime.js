/**
 * runtime.js — Ядро сгенерированного клиента (typescript-fetch).
 *
 * Реализует классы Configuration и ResponseError,
 * а также базовый BaseAPI, от которого наследуются все API-классы.
 *
 * Интерфейс полностью совместим с выходом openapi-generator-cli:
 *   openapi-generator-cli generate -g typescript-fetch
 */

// ==================================================================
// Configuration — хранит basePath и общие настройки
// ==================================================================
export class Configuration {
    constructor(params = {}) {
        this.basePath = params.basePath ?? '';
        this.headers  = params.headers  ?? {};
    }
}

// ==================================================================
// ResponseError — выбрасывается при HTTP-ответах вне 2xx
// ==================================================================
export class ResponseError extends Error {
    /**
     * @param {Response} response — оригинальный объект fetch Response
     * @param {string} [message]
     */
    constructor(response, message) {
        super(message || `Response returned status ${response.status}`);
        this.name     = 'ResponseError';
        this.response = response;
    }
}

// ==================================================================
// BaseAPI — общий предок всех API-классов
// ==================================================================
export class BaseAPI {
    /**
     * @param {Configuration} configuration
     */
    constructor(configuration) {
        this.configuration = configuration;
        this.basePath      = configuration.basePath;
    }

    /**
     * Выполнить HTTP-запрос. При ответе вне 2xx — выбросить ResponseError.
     *
     * @param {string}  path    — относительный путь (например '/services')
     * @param {string}  method  — HTTP-метод
     * @param {Object}  [body]  — тело запроса (будет сериализовано в JSON)
     * @returns {Promise<any>}  — распарсенный JSON ответа
     */
    async request(path, method, body) {
        const url = `${this.basePath}${path}`;

        const options = {
            method,
            credentials: 'same-origin',
            headers: {
                'Content-Type': 'application/json',
                'Accept':       'application/json',
                ...this.configuration.headers,
            },
        };

        if (body !== undefined) {
            options.body = JSON.stringify(body);
        }

        const response = await fetch(url, options);

        if (!response.ok) {
            // Сессия истекла — редирект на логин
            if (response.status === 401 && !window.location.pathname.includes('login.html')) {
                window.location.href = '/login.html';
                return;
            }
            // Выбрасываем ResponseError с оригинальным Response —
            // parseApiError() в api.js ожидает err.response.json()
            throw new ResponseError(response);
        }

        // Пустой ответ (204 No Content)
        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }
}
