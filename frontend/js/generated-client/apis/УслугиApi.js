/**
 * УслугиApi — Справочник коммунальных услуг.
 * Сгенерирован из @Tag(name = "Услуги") контроллера ServiceController.
 */
import { BaseAPI } from '../runtime.js';

export class УслугиApi extends BaseAPI {

    /**
     * GET /services — Получить список всех услуг.
     * @returns {Promise<ServiceInfoDto[]>}
     */
    async getAll() {
        return this.request('/services', 'GET');
    }
}
