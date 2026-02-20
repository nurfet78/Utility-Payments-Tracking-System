/**
 * ЛицевыеСчетаApi — Управление лицевыми счетами.
 * Сгенерирован из @Tag(name = "Лицевые счета") контроллера AccountController.
 */
import { BaseAPI } from '../runtime.js';

export class ЛицевыеСчетаApi extends BaseAPI {

    /**
     * POST /accounts — Создать лицевой счёт.
     * @param {{ accountCreateRequest: { serviceType: string, accountNumber: string } }} params
     * @returns {Promise<AccountResponse>}
     */
    async create(params) {
        return this.request('/accounts', 'POST', params.accountCreateRequest);
    }

    /**
     * GET /accounts/{id} — Получить лицевой счёт по ID.
     * @param {{ id: number }} params
     * @returns {Promise<AccountResponse>}
     */
    async getById(params) {
        return this.request(`/accounts/${params.id}`, 'GET');
    }

    /**
     * GET /accounts/with-status — Все счета со статусом оплаты.
     * @returns {Promise<AccountWithStatusResponse[]>}
     */
    async getWithStatus() {
        return this.request('/accounts/with-status', 'GET');
    }
}
