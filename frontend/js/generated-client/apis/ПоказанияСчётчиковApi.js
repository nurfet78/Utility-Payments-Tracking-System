/**
 * ПоказанияСчётчиковApi — Передача и просмотр показаний счётчиков.
 * Сгенерирован из @Tag(name = "Показания счётчиков") контроллера MeterReadingController.
 *
 * Суффиксы create2 / getByAccount1 — результат разрешения конфликтов
 * operationId генератором openapi-generator-cli.
 */
import { BaseAPI } from '../runtime.js';

export class ПоказанияСчётчиковApi extends BaseAPI {

    /**
     * POST /accounts/{accountId}/readings — Передать показание счётчика.
     * @param {{ accountId: number, meterReadingCreateRequest: { value: string, readingDate: string } }} params
     * @returns {Promise<MeterReadingResponse>}
     */
    async create2(params) {
        return this.request(
            `/accounts/${params.accountId}/readings`,
            'POST',
            params.meterReadingCreateRequest,
        );
    }

    /**
     * GET /accounts/{accountId}/readings — История показаний.
     * @param {{ accountId: number }} params
     * @returns {Promise<MeterReadingResponse[]>}
     */
    async getByAccount1(params) {
        return this.request(
            `/accounts/${params.accountId}/readings`,
            'GET',
        );
    }
}
