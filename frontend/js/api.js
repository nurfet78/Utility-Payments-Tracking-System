/**
 * api.js — Обёртка над сгенерированным typescript-fetch клиентом.
 *
 * Клиент сгенерирован командой:
 *   openapi-generator-cli generate -i http://localhost:8888/v3/api-docs -g typescript-fetch
 *
 * Классы API (сгенерированы из @Tag-аннотаций контроллеров):
 *   - UslugiApi                  → GET /services
 *   - LitsevyeSchetaApi          → POST /accounts, GET /accounts/{id}, GET /accounts/with-status
 *   - PlatezhiApi                → POST|PUT|DELETE /accounts/{id}/payments
 *   - PokazaniyaSchyotchikovApi  → POST|PUT|DELETE /accounts/{id}/readings
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
import { Configuration }              from './generated-client/runtime.js';
import { UslugiApi }                  from './generated-client/apis/UslugiApi.js';
import { LitsevyeSchetaApi }          from './generated-client/apis/LitsevyeSchetaApi.js';
import { PlatezhiApi }                from './generated-client/apis/PlatezhiApi.js';
import { PokazaniyaSchyotchikovApi }  from './generated-client/apis/PokazaniyaSchyotchikovApi.js';

// ------------------------------------------------------------------
// Конфигурация — базовый URL бэкенда
// ------------------------------------------------------------------
const config = new Configuration({
    basePath: window.API_BASE_URL ?? 'http://localhost:8888',
});

// ------------------------------------------------------------------
// Экземпляры сгенерированных API-классов
// ------------------------------------------------------------------
const uslugiApi                = new UslugiApi(config);
const litsevyeSchetaApi        = new LitsevyeSchetaApi(config);
const platezhiApi              = new PlatezhiApi(config);
const pokazaniyaSchyotchikovApi = new PokazaniyaSchyotchikovApi(config);

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
    getAll: () => uslugiApi.getAll(),
};

/** Лицевые счета */
export const accountsApi = {
    /**
     * Создать лицевой счёт.
     * @param {{ serviceType: string, accountNumber: string }} data
     * @returns {Promise<AccountResponse>}
     */
    create: (data) =>
        litsevyeSchetaApi.create({ accountCreateRequest: data }),

    /**
     * Получить лицевой счёт по ID.
     * @param {number} id
     * @returns {Promise<AccountResponse>}
     */
    getById: (id) =>
        litsevyeSchetaApi.getById({ id }),

    /**
     * Получить все счета со статусом оплаты за текущий месяц.
     * @returns {Promise<AccountWithStatusResponse[]>}
     */
    getWithStatus: () =>
        litsevyeSchetaApi.getWithStatus(),
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
        platezhiApi.create1({ accountId, paymentCreateRequest: data }),

    /**
     * Получить историю платежей по лицевому счёту.
     * @param {number} accountId
     * @returns {Promise<PaymentResponse[]>}
     */
    getByAccount: (accountId) =>
        platezhiApi.getByAccount({ accountId }),

    /**
     * Редактировать платёж.
     * @param {number} accountId
     * @param {number} paymentId
     * @param {{ amount: number, paymentDate: string }} data
     * @returns {Promise<PaymentResponse>}
     */
    update: (accountId, paymentId, data) =>
        platezhiApi.update1({ accountId, paymentId, paymentCreateRequest: data }),

    /**
     * Удалить платёж.
     * @param {number} accountId
     * @param {number} paymentId
     * @returns {Promise<void>}
     */
    delete: (accountId, paymentId) =>
        platezhiApi.delete1({ accountId, paymentId }),
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
        pokazaniyaSchyotchikovApi.create2({ accountId, meterReadingCreateRequest: data }),

    /**
     * Получить историю показаний по лицевому счёту.
     * @param {number} accountId
     * @returns {Promise<MeterReadingResponse[]>}
     */
    getByAccount: (accountId) =>
        pokazaniyaSchyotchikovApi.getByAccount1({ accountId }),

    /**
     * Редактировать показание.
     * @param {number} accountId
     * @param {number} readingId
     * @param {{ value: string, readingDate: string }} data
     * @returns {Promise<MeterReadingResponse>}
     */
    update: (accountId, readingId, data) =>
        pokazaniyaSchyotchikovApi.update2({ accountId, readingId, meterReadingCreateRequest: data }),

    /**
     * Удалить показание.
     * @param {number} accountId
     * @param {number} readingId
     * @returns {Promise<void>}
     */
    delete: (accountId, readingId) =>
        pokazaniyaSchyotchikovApi.delete2({ accountId, readingId }),
};

export { parseApiError };
