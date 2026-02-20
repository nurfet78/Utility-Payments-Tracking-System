/**
 * main.js — Логика главной страницы (Dashboard).
 *
 * Отображает:
 * - статистику (карточки: всего / оплачено / не оплачено / просрочено)
 * - фильтр по статусу
 * - таблицу всех лицевых счетов со статусами оплаты
 * - форму создания нового лицевого счёта
 */

import { accountsApi, servicesApi, parseApiError } from './api.js';
import {
    $, el, clear, showLoader, showAlert, emptyState,
    openModal, showFormErrors, clearFormErrors,
    serviceName, statusBadge, validate, rules,
} from './ui.js';

// ------------------------------------------------------------------
// Состояние страницы
// ------------------------------------------------------------------
let allAccounts  = [];  // AccountWithStatusResponse[]
let allServices  = [];  // ServiceInfoDto[]
let activeFilter = 'ALL';

// ------------------------------------------------------------------
// Инициализация
// ------------------------------------------------------------------
export async function initDashboard() {
    setupFilters();
    setupCreateButton();
    await loadData();
}

// ------------------------------------------------------------------
// Загрузка данных
// ------------------------------------------------------------------
async function loadData() {
    const container = $('accounts-table-body');
    const statsBar  = $('stats-bar');

    showLoader(container, 'Загрузка счетов...');

    try {
        const [accounts, services] = await Promise.all([
            accountsApi.getWithStatus(),
            servicesApi.getAll(),
        ]);

        allAccounts = accounts;
        allServices = services;

        renderStats(statsBar);
        renderTable(container);
    } catch (err) {
        const apiErr = await parseApiError(err);
        clear(container);
        container.append(
            emptyState('⚠', `Ошибка загрузки: ${apiErr.message}`)
        );
    }
}

// ------------------------------------------------------------------
// Статистика (карточки)
// ------------------------------------------------------------------
function renderStats(container) {
    const total   = allAccounts.length;
    const paid    = allAccounts.filter(a => a.status === 'PAID_THIS_MONTH').length;
    const notPaid = allAccounts.filter(a => a.status === 'NOT_PAID_THIS_MONTH').length;
    const overdue = allAccounts.filter(a => a.status === 'OVERDUE').length;

    clear(container);
    container.classList.add('stagger');

    container.append(
        statCard(String(total), 'Всего счетов', ''),
        statCard(String(paid), 'Оплачено', 'stat-card--paid'),
        statCard(String(notPaid), 'Не оплачено', 'stat-card--not-paid'),
        statCard(String(overdue), 'Просрочено', 'stat-card--overdue'),
    );
}

function statCard(value, label, modifier) {
    return el('div', { className: `stat-card ${modifier}` },
        el('div', { className: 'stat-card__value', textContent: value }),
        el('div', { className: 'stat-card__label', textContent: label }),
    );
}

// ------------------------------------------------------------------
// Фильтрация
// ------------------------------------------------------------------
function setupFilters() {
    const bar = $('filter-bar');
    bar.addEventListener('click', (e) => {
        const btn = e.target.closest('.filter-btn');
        if (!btn) return;

        activeFilter = btn.dataset.filter;

        bar.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
        btn.classList.add('active');

        renderTable($('accounts-table-body'));
    });
}

function getFiltered() {
    if (activeFilter === 'ALL') return allAccounts;
    return allAccounts.filter(a => a.status === activeFilter);
}

// ------------------------------------------------------------------
// Таблица счетов
// ------------------------------------------------------------------
function renderTable(container) {
    const data = getFiltered();
    clear(container);

    if (data.length === 0) {
        container.append(
            el('tr', {},
                el('td', { colSpan: '4', className: 'empty-state' },
                    emptyState('📭', activeFilter === 'ALL'
                        ? 'Нет лицевых счетов. Нажмите «+ Новый лицевой счёт» чтобы создать.'
                        : 'Нет счетов с таким статусом')
                )
            )
        );
        return;
    }

    for (const account of data) {
        const row = el('tr', {
            onClick: () => navigateToAccount(account.accountId),
        },
            el('td', { className: 'cell-service' }, serviceName(account.serviceType)),
            el('td', { className: 'cell-data' }, account.accountNumber),
            el('td', {}, statusBadge(account.status)),
            el('td', {},
                el('span', { className: 'cell-data', textContent: `#${account.accountId}` })
            ),
        );
        container.append(row);
    }
}

// ------------------------------------------------------------------
// Создание лицевого счёта
// ------------------------------------------------------------------

function setupCreateButton() {
    $('btn-create-account').addEventListener('click', openCreateAccountModal);
}

function openCreateAccountModal() {
    // Выпадающий список услуг
    const select = el('select', {
        name: 'serviceType',
        className: 'form-select',
    },
        el('option', { value: '', textContent: '— Выберите услугу —', disabled: 'true', selected: 'true' }),
    );

    const services = allServices.length > 0
        ? allServices
        : [
            { code: 'GAS', displayName: 'Газ' },
            { code: 'WATER', displayName: 'Вода' },
            { code: 'ELECTRICITY', displayName: 'Электроэнергия' },
            { code: 'INTERCOM', displayName: 'Домофон' },
            { code: 'HEATING', displayName: 'Отопление' },
            { code: 'ECO_RESOURCES', displayName: 'Экоресурсы' },
            { code: 'HOUSING_SERVICE', displayName: 'Жилсервис' },
            { code: 'CAPITAL_REPAIR', displayName: 'Капитальный ремонт' },
        ];

    for (const svc of services) {
        select.append(el('option', { value: svc.code, textContent: svc.displayName }));
    }

    const form = el('div', { className: 'form-grid' },
        // Тип услуги
        el('div', { className: 'form-group form-group--full' },
            el('label', { className: 'form-label', textContent: 'Тип услуги' }),
            select,
            el('span', { className: 'form-error', dataset: { field: 'serviceType' } }),
        ),

        // Номер лицевого счёта
        el('div', { className: 'form-group form-group--full' },
            el('label', { className: 'form-label', textContent: 'Номер лицевого счёта' }),
            el('input', {
                type: 'text',
                name: 'accountNumber',
                className: 'form-input form-input--mono',
                placeholder: '1234-56.78',
                maxLength: '30',
            }),
            el('span', { className: 'form-hint', textContent: 'До 30 символов. Допустимы: цифры, точка, тире' }),
            el('span', { className: 'form-error', dataset: { field: 'accountNumber' } }),
        ),

        // Кнопка
        el('div', { className: 'form-actions' },
            el('button', {
                className: 'btn btn--primary',
                textContent: 'Создать',
                id: 'submit-account',
                onClick: () => submitCreateAccount(modal),
            }),
        ),
    );

    const modal = openModal('Новый лицевой счёт', form);
}

async function submitCreateAccount(modal) {
    console.log('submitCreateAccount вызвана');
    const modalBody = modal.overlay.querySelector('.modal__body');
    clearFormErrors(modalBody);

    const serviceType   = modalBody.querySelector('[name="serviceType"]').value;
    const accountNumber = modalBody.querySelector('[name="accountNumber"]').value;

    // Клиентская валидация
    const { valid, errors } = validate({ serviceType, accountNumber }, {
        serviceType:   rules.required('Выберите тип услуги'),
        accountNumber: (v) => {
            if (!v || !v.trim()) return 'Номер счёта обязателен';
            if (!/^[0-9.\-]+$/.test(v)) return 'Допустимы только цифры, точка, тире';
            if (v.length > 30) return 'Максимум 30 символов';
            return null;
        },
    });

    if (!valid) {
        highlightErrors(modalBody, errors);
        return;
    }

    const btn = modalBody.querySelector('#submit-account');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Создание...';

    try {
        await accountsApi.create({ serviceType, accountNumber });
        modal.close();
        await loadData();
    } catch (err) {
        const apiErr = await parseApiError(err);
        showFormErrors(modalBody, apiErr);
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

function highlightErrors(container, errors) {
    for (const [field, msg] of Object.entries(errors)) {
        const input = container.querySelector(`[name="${field}"]`);
        if (input) input.classList.add('error');

        const errorEl = container.querySelector(`.form-error[data-field="${field}"]`);
        if (errorEl) errorEl.textContent = msg;
    }
}

// ------------------------------------------------------------------
// Навигация
// ------------------------------------------------------------------
function navigateToAccount(accountId) {
    window.location.href = `account.html?id=${accountId}`;
}

// ------------------------------------------------------------------
// Автозапуск
// ------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', initDashboard);
