/**
 * ПлатежиApi — Регистрация и просмотр платежей.
 * Сгенерирован из @Tag(name = "Платежи") контроллера PaymentController.
 *
 * Метод create1 (с суффиксом) — openapi-generator добавляет суффикс
 * при конфликте operationId между контроллерами.
 */
import { BaseAPI } from '../runtime.js';

export class ПлатежиApi extends BaseAPI {

    /**
     * POST /accounts/{accountId}/payments — Зарегистрировать платёж.
     * @param {{ accountId: number, paymentCreateRequest: { amount: number, paymentDate: string } }} params
     * @returns {Promise<PaymentResponse>}
     */
    async create1(params) {
        return this.request(
            `/accounts/${params.accountId}/payments`,
            'POST',
            params.paymentCreateRequest,
        );
    }

    /**
     * GET /accounts/{accountId}/payments — История платежей.
     * @param {{ accountId: number }} params
     * @returns {Promise<PaymentResponse[]>}
     */
    async getByAccount(params) {
        return this.request(
            `/accounts/${params.accountId}/payments`,
            'GET',
        );
    }
}
