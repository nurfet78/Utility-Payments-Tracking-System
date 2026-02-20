/**
 * api.js — Обёртка над сгенерированным typescript-fetch клиентом.
 *
 * Клиент сгенерирован командой:
 *   openapi-generator-cli generate -i http://localhost:8888/v3/api-docs -g typescript-fetch
 *
 * Классы API (сгенерированы из @Tag-аннотаций контроллеров):
 *   - УслугиApi               → GET /services
 *   - ЛицевыеСчетаApi         → POST /accounts, GET /accounts/{id}, GET /accounts/with-status
 *   - ПлатежиApi              → POST /accounts/{id}/payments, GET /accounts/{id}/payments
 *   - ПоказанияСчётчиковApi   → POST /accounts/{id}/readings, GET /accounts/{id}/readings
 *
 * Методы API (сгенерированы из operationId):
 *   - getAll()           → GET /services
 *   - create()           → POST /accounts
 *   - getById({id})      → GET /accounts/{id}
 *   - getWithStatus()    → GET /accounts/with-status
 *   - create1({...})     → POST /accounts/{id}/payments
 *   - getByAccount({..}) → GET /accounts/{id}/payments
 *   - create2({...})     → POST /accounts/{id}/readings
 *   - getByAccount1({..})→ GET /accounts/{id}/readings
 *
 * Этот модуль инициализирует клиент и предоставляет
 * удобный фасад с единообразной обработкой ошибок.
 */

// ------------------------------------------------------------------
// Импорт из сгенерированного клиента (typescript-fetch, compiled)
// ------------------------------------------------------------------
import { Configuration }         from './generated-client/runtime.js';
import { УслугиApi }             from './generated-client/apis/УслугиApi.js';
import { ЛицевыеСчетаApi }       from './generated-client/apis/ЛицевыеСчетаApi.js';
import { ПлатежиApi }            from './generated-client/apis/ПлатежиApi.js';
import { ПоказанияСчётчиковApi } from './generated-client/apis/ПоказанияСчётчиковApi.js';

// ------------------------------------------------------------------
// Конфигурация — базовый URL бэкенда
// ------------------------------------------------------------------
const config = new Configuration({
    basePath: window.API_BASE_URL || 'http://localhost:8888',
});

// ------------------------------------------------------------------
// Экземпляры сгенерированных API-классов
// ------------------------------------------------------------------
const услугиApi             = new УслугиApi(config);
const лицевыеСчетаApi       = new ЛицевыеСчетаApi(config);
const платежиApi            = new ПлатежиApi(config);
const показанияСчётчиковApi = new ПоказанияСчётчиковApi(config);

// ------------------------------------------------------------------
// Обработка ошибок: извлечение ErrorResponse из тела ответа
// ------------------------------------------------------------------

/**
 * Парсит тело ошибки из ResponseError, возвращённого сгенерированным клиентом.
 * Формат ErrorResponse: { status, error, message, fieldErrors, timestamp }
 */
async function parseApiError(err) {
    if (err?.response) {
        try {
            const body = await err.response.json();
            return {
                status:      body.status || err.response.status,
                message:     body.message || 'Неизвестная ошибка',
                fieldErrors: body.fieldErrors || null,
            };
        } catch {
            return {
                status:  err.response.status,
                message: `HTTP ${err.response.status}: ошибка сервера`,
                fieldErrors: null,
            };
        }
    }
    return {
        status:  0,
        message: err?.message || 'Нет соединения с сервером',
        fieldErrors: null,
    };
}

// ------------------------------------------------------------------
// Публичный API — фасад с единообразной сигнатурой
// ------------------------------------------------------------------

/** Справочник услуг */
export const servicesApi = {
    /** @returns {Promise<ServiceInfoDto[]>} */
    getAll: () => услугиApi.getAll(),
};

/** Лицевые счета */
export const accountsApi = {
    /**
     * Создать лицевой счёт.
     * @param {{ serviceType: string, accountNumber: string }} data
     * @returns {Promise<AccountResponse>}
     */
    create: (data) =>
        лицевыеСчетаApi.create({ accountCreateRequest: data }),

    /**
     * Получить лицевой счёт по ID.
     * @param {number} id
     * @returns {Promise<AccountResponse>}
     */
    getById: (id) =>
        лицевыеСчетаApi.getById({ id }),

    /**
     * Получить все счета со статусом оплаты за текущий месяц.
     * @returns {Promise<AccountWithStatusResponse[]>}
     */
    getWithStatus: () =>
        лицевыеСчетаApi.getWithStatus(),
};

/** Платежи */
export const paymentsApi = {
    /**
     * Зарегистрировать платёж.
     * @param {number} accountId
     * @param {{ amount: number, paymentDate: string }} data
     * @returns {Promise<PaymentResponse>}
     */
    create: (accountId, data) =>
        платежиApi.create1({ accountId, paymentCreateRequest: data }),

    /**
     * Получить историю платежей по лицевому счёту.
     * @param {number} accountId
     * @returns {Promise<PaymentResponse[]>}
     */
    getByAccount: (accountId) =>
        платежиApi.getByAccount({ accountId }),
};

/** Показания счётчиков */
export const readingsApi = {
    /**
     * Передать показание счётчика.
     * @param {number} accountId
     * @param {{ value: string, readingDate: string }} data
     * @returns {Promise<MeterReadingResponse>}
     */
    create: (accountId, data) =>
        показанияСчётчиковApi.create2({ accountId, meterReadingCreateRequest: data }),

    /**
     * Получить историю показаний по лицевому счёту.
     * @param {number} accountId
     * @returns {Promise<MeterReadingResponse[]>}
     */
    getByAccount: (accountId) =>
        показанияСчётчиковApi.getByAccount1({ accountId }),
};

export { parseApiError };
