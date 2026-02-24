/**
 * monthly-page.js — Логика страницы «Отчёт за месяц».
 *
 * Загружает все лицевые счета, для каждого — платежи и показания,
 * фильтрует по выбранному месяцу и отображает сводку.
 *
 * Использует только существующие API-эндпоинты (бэкенд не изменён).
 */

import {
    accountsApi, paymentsApi, readingsApi, servicesApi, parseApiError,
} from './api.js';
import {
    $, el, clear, showLoader, emptyState,
    serviceName, statusBadge, meterBadge, formatAmount, formatDate,
} from './ui.js';
import { requireAuth } from './auth.js';

// ------------------------------------------------------------------
// Состояние
// ------------------------------------------------------------------
let accounts  = [];   // AccountWithStatusResponse[]
let services  = [];   // ServiceInfoDto[]
let accountData = {}; // { [accountId]: { payments: [], readings: [] } }

let selectedYear  = null;
let selectedMonth = null;

// ------------------------------------------------------------------
// Инициализация
// ------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', async () => {
    if (!(await requireAuth())) return;
    initMonthPicker();
    await loadAllData();
});

// ------------------------------------------------------------------
// Выбор месяца
// ------------------------------------------------------------------
function initMonthPicker() {
    const input = $('month-input');

    // По умолчанию — текущий месяц
    const now = new Date();
    selectedYear  = now.getFullYear();
    selectedMonth = now.getMonth() + 1;
    input.value = `${selectedYear}-${String(selectedMonth).padStart(2, '0')}`;

    input.addEventListener('change', () => {
        const [y, m] = input.value.split('-').map(Number);
        selectedYear  = y;
        selectedMonth = m;
        renderReport();
    });
}

// ------------------------------------------------------------------
// Загрузка данных (один раз, потом фильтруем на клиенте)
// ------------------------------------------------------------------
async function loadAllData() {
    const container = $('service-cards');
    showLoader(container, 'Загрузка данных...');

    try {
        // 1. Загружаем счета и справочник услуг
        const [accs, svcs] = await Promise.all([
            accountsApi.getWithStatus(),
            servicesApi.getAll(),
        ]);
        accounts = accs;
        services = svcs;

        // 2. Для каждого счёта загружаем платежи и показания параллельно
        const serviceMap = Object.fromEntries(services.map(s => [s.code, s]));

        const dataPromises = accounts.map(async (acc) => {
            const svc = serviceMap[acc.serviceType];
            const hasMeter = svc?.hasMeter ?? false;

            const [payments, readings] = await Promise.all([
                paymentsApi.getByAccount(acc.accountId),
                hasMeter
                    ? readingsApi.getByAccount(acc.accountId)
                    : Promise.resolve([]),
            ]);

            return { accountId: acc.accountId, payments, readings };
        });

        const results = await Promise.all(dataPromises);

        accountData = {};
        for (const r of results) {
            accountData[r.accountId] = {
                payments: r.payments,
                readings: r.readings,
            };
        }

        renderReport();
    } catch (err) {
        const apiErr = await parseApiError(err);
        clear(container);
        container.append(emptyState('⚠', `Ошибка: ${apiErr.message}`));
    }
}

// ------------------------------------------------------------------
// Фильтрация по месяцу
// ------------------------------------------------------------------
function filterByMonth(items, dateField) {
    return items.filter(item => {
        const [y, m] = item[dateField].split('-').map(Number);
        return y === selectedYear && m === selectedMonth;
    });
}

/**
 * Вычислить статус оплаты для произвольного месяца.
 * Логика повторяет бэкенд PaymentStatusService:
 *   PAID_THIS_MONTH    — есть хотя бы один платёж в этом месяце
 *   NOT_PAID_THIS_MONTH — нет платежей, и ≤ 25 числа (только для текущего месяца)
 *   OVERDUE             — нет платежей, и > 25 числа (или месяц прошёл)
 */
function computeStatus(monthPayments) {
    if (monthPayments.length > 0) return 'PAID_THIS_MONTH';

    const now = new Date();
    const isCurrentMonth =
        now.getFullYear() === selectedYear &&
        (now.getMonth() + 1) === selectedMonth;

    if (isCurrentMonth) {
        return now.getDate() > 25 ? 'OVERDUE' : 'NOT_PAID_THIS_MONTH';
    }

    // Прошлый месяц без платежей — просрочено
    // Будущий месяц — ещё не оплачено
    const selectedDate = new Date(selectedYear, selectedMonth - 1, 1);
    if (selectedDate > now) {
        return 'NOT_PAID_THIS_MONTH';
    }

    return 'OVERDUE';
}

// ------------------------------------------------------------------
// Название месяца
// ------------------------------------------------------------------
const MONTH_NAMES = [
    '', 'Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь',
    'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь',
];

function monthLabel() {
    return `${MONTH_NAMES[selectedMonth]} ${selectedYear}`;
}

// ------------------------------------------------------------------
// Рендер отчёта
// ------------------------------------------------------------------
function renderReport() {
    const summaryContainer = $('month-summary');
    const cardsContainer   = $('service-cards');

    if (accounts.length === 0) {
        clear(summaryContainer);
        clear(cardsContainer);
        cardsContainer.append(emptyState('📭', 'Нет лицевых счетов.'));
        return;
    }

    const serviceMap = Object.fromEntries(services.map(s => [s.code, s]));

    // Подготовить данные для каждого счёта
    const reportItems = accounts.map(acc => {
        const data = accountData[acc.accountId] || { payments: [], readings: [] };
        const monthPayments = filterByMonth(data.payments, 'paymentDate');
        const monthReadings = filterByMonth(data.readings, 'readingDate');
        const svc = serviceMap[acc.serviceType];
        const status = computeStatus(monthPayments);
        const totalPaid = monthPayments.reduce((sum, p) => sum + Number(p.amount), 0);

        return {
            acc,
            svc,
            status,
            totalPaid,
            monthPayments,
            monthReadings,
        };
    });

    // Сводка
    renderSummary(summaryContainer, reportItems);

    // Карточки
    renderCards(cardsContainer, reportItems);
}

// ------------------------------------------------------------------
// Сводка (карточки-счётчики)
// ------------------------------------------------------------------
function renderSummary(container, items) {
    const totalAccounts = items.length;
    const paid    = items.filter(i => i.status === 'PAID_THIS_MONTH').length;
    const notPaid = items.filter(i => i.status === 'NOT_PAID_THIS_MONTH').length;
    const overdue = items.filter(i => i.status === 'OVERDUE').length;
    const totalSum = items.reduce((s, i) => s + i.totalPaid, 0);

    clear(container);

    container.append(
        summaryCard(String(totalAccounts), 'Всего счетов', ''),
        summaryCard(String(paid), 'Оплачено', 'stat-card--paid'),
        summaryCard(String(notPaid), 'Не оплачено', 'stat-card--not-paid'),
        summaryCard(String(overdue), 'Просрочено', 'stat-card--overdue'),
        summaryCard(formatAmount(totalSum), `Итого за ${monthLabel()}`, ''),
    );
}

function summaryCard(value, label, modifier) {
    return el('div', { className: `stat-card ${modifier}` },
        el('div', { className: 'stat-card__value', textContent: value }),
        el('div', { className: 'stat-card__label', textContent: label }),
    );
}

// ------------------------------------------------------------------
// Карточки по каждому счёту
// ------------------------------------------------------------------
function renderCards(container, items) {
    clear(container);

    for (const item of items) {
        container.append(renderServiceCard(item));
    }
}

function renderServiceCard({ acc, svc, status, totalPaid, monthPayments, monthReadings }) {
    const hasMeter = svc?.hasMeter ?? false;

    // Шапка карточки
    const headerRow = el('div', { className: 'service-card__header-row' },
        el('div', { className: 'service-card__info' },
            el('span', { className: 'service-card__name', textContent: serviceName(acc.serviceType) }),
            el('span', { className: 'service-card__account', textContent: acc.accountNumber }),
            statusBadge(status),
        ),
        el('span', {
            className: `service-card__total ${totalPaid > 0 ? 'service-card__total--paid' : 'service-card__total--zero'}`,
            textContent: totalPaid > 0 ? formatAmount(totalPaid) : '0 ₽',
        }),
    );

    // Таблица платежей
    const paymentsSection = el('div', { className: 'sub-section' },
        el('div', { className: 'sub-section__title', textContent: 'Платежи' }),
        monthPayments.length > 0
            ? paymentsTable(monthPayments)
            : el('div', {
                className: 'empty-state',
                style: 'padding: var(--space-sm)',
                textContent: 'Нет платежей',
            }),
    );

    // Таблица показаний (если есть счётчик)
    let readingsSection = null;
    if (hasMeter) {
        readingsSection = el('div', { className: 'sub-section' },
            el('div', { className: 'sub-section__title',
                textContent: `Показания (${svc.meterDigits} цифр)` }),
            monthReadings.length > 0
                ? readingsTable(monthReadings)
                : el('div', {
                    className: 'empty-state',
                    style: 'padding: var(--space-sm)',
                    textContent: 'Нет показаний',
                }),
        );
    }

    const body = el('div', { className: 'card__body card__body--padded' },
        paymentsSection,
    );
    if (readingsSection) body.append(readingsSection);

    return el('div', { className: 'card animate-in' },
        el('div', { className: 'card__header' }, headerRow),
        body,
    );
}

// ------------------------------------------------------------------
// Мини-таблицы
// ------------------------------------------------------------------
function paymentsTable(payments) {
    const thead = el('thead', {},
        el('tr', {},
            el('th', { textContent: 'Дата' }),
            el('th', { textContent: 'Сумма' }),
        ),
    );

    const tbody = el('tbody');
    for (const p of payments) {
        tbody.append(el('tr', {},
            el('td', { className: 'cell-date', textContent: formatDate(p.paymentDate) }),
            el('td', { className: 'cell-amount', textContent: formatAmount(p.amount) }),
        ));
    }

    return el('div', { className: 'table-wrap' },
        el('table', { className: 'table' }, thead, tbody),
    );
}

function readingsTable(readings) {
    const thead = el('thead', {},
        el('tr', {},
            el('th', { textContent: 'Дата' }),
            el('th', { textContent: 'Показание' }),
        ),
    );

    const tbody = el('tbody');
    for (const r of readings) {
        tbody.append(el('tr', {},
            el('td', { className: 'cell-date', textContent: formatDate(r.readingDate) }),
            el('td', { className: 'cell-data', textContent: r.value }),
        ));
    }

    return el('div', { className: 'table-wrap' },
        el('table', { className: 'table' }, thead, tbody),
    );
}
