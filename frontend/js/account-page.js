/**
 * account-page.js — Логика страницы деталей лицевого счёта.
 *
 * Отображает:
 * - информацию о счёте (услуга, номер)
 * - историю платежей с формой добавления
 * - историю показаний счётчика (если услуга поддерживает) с формой добавления
 */

import {
    accountsApi, paymentsApi, readingsApi, servicesApi, parseApiError,
} from './api.js';
import {
    $, el, clear, showLoader, showAlert, clearAlert, emptyState,
    openModal, showFormErrors, clearFormErrors,
    serviceName, statusBadge, meterBadge, formatAmount, formatDate, todayISO,
    validate, rules,
} from './ui.js';

// ------------------------------------------------------------------
// Состояние страницы
// ------------------------------------------------------------------
let accountId   = null;
let account     = null;   // AccountResponse
let serviceInfo = null;   // ServiceInfoDto (для hasMeter / meterDigits)
let payments    = [];     // PaymentResponse[]
let readings    = [];     // MeterReadingResponse[]

// ------------------------------------------------------------------
// Инициализация
// ------------------------------------------------------------------
export async function initAccountPage() {
    // Извлечь ID из query string
    const params = new URLSearchParams(window.location.search);
    accountId = Number(params.get('id'));

    if (!accountId) {
        showAlert($('account-detail'), 'error', 'ID лицевого счёта не указан');
        return;
    }

    await loadAccountData();
}

// ------------------------------------------------------------------
// Загрузка данных (параллельно)
// ------------------------------------------------------------------
async function loadAccountData() {
    const detailContainer = $('account-detail');
    showLoader(detailContainer, 'Загрузка...');

    try {
        // Загружаем данные счёта и справочник параллельно
        const [accountData, services] = await Promise.all([
            accountsApi.getById(accountId),
            servicesApi.getAll(),
        ]);

        account = accountData;
        serviceInfo = services.find(s => s.code === account.serviceType) || null;

        renderAccountInfo(detailContainer);

        // Загружаем историю параллельно
        await Promise.all([
            loadPayments(),
            loadReadings(),
        ]);
    } catch (err) {
        const apiErr = await parseApiError(err);
        clear(detailContainer);
        detailContainer.append(
            emptyState('⚠', `Ошибка: ${apiErr.message}`)
        );
    }
}

// ------------------------------------------------------------------
// Информация о счёте
// ------------------------------------------------------------------
function renderAccountInfo(container) {
    clear(container);

    // Breadcrumb
    const breadcrumb = el('nav', { className: 'breadcrumb' },
        el('a', { href: 'index.html', textContent: 'Главная' }),
        el('span', { className: 'breadcrumb__sep' }),
        el('span', { textContent: serviceName(account.serviceType) }),
        el('span', { className: 'breadcrumb__sep' }),
        el('span', { textContent: account.accountNumber }),
    );

    // Заголовок
    const heading = el('h1', { className: 'page-title' },
        `${serviceName(account.serviceType)} — ${account.accountNumber}`
    );

    // Информационные поля
    const info = el('div', { className: 'detail-info' },
        detailField('Услуга', serviceName(account.serviceType)),
        detailField('Номер счёта', account.accountNumber, true),
        detailField('ID в системе', `#${account.id}`, true),
        detailFieldEl('Счётчик',
            serviceInfo
                ? meterBadge(serviceInfo.hasMeter, serviceInfo.meterDigits)
                : el('span', { textContent: '—' })
        ),
    );

    const infoCard = el('div', { className: 'card animate-in' },
        el('div', { className: 'card__header' },
            el('span', { className: 'card__title', textContent: 'Информация о счёте' }),
        ),
        el('div', { className: 'card__body card__body--padded' }, info),
    );

    container.append(breadcrumb, heading, infoCard);
}

function detailField(label, value, mono = false) {
    return el('div', { className: 'detail-field' },
        el('div', { className: 'detail-field__label', textContent: label }),
        el('div', {
            className: `detail-field__value ${mono ? 'detail-field__value--mono' : ''}`,
            textContent: value,
        }),
    );
}

function detailFieldEl(label, valueEl) {
    return el('div', { className: 'detail-field' },
        el('div', { className: 'detail-field__label', textContent: label }),
        el('div', { className: 'detail-field__value' }, valueEl),
    );
}

// ------------------------------------------------------------------
// Платежи
// ------------------------------------------------------------------
async function loadPayments() {
    const container = $('payments-section');
    showLoader(container, 'Загрузка платежей...');

    try {
        payments = await paymentsApi.getByAccount(accountId);
        renderPayments(container);
    } catch (err) {
        const apiErr = await parseApiError(err);
        clear(container);
        container.append(emptyState('⚠', apiErr.message));
    }
}

function renderPayments(container) {
    clear(container);

    const card = el('div', { className: 'card animate-in' },
        el('div', { className: 'card__header' },
            el('span', { className: 'card__title', textContent: `Платежи (${payments.length})` }),
            el('button', {
                className: 'btn btn--primary btn--sm',
                textContent: '+ Новый платёж',
                onClick: openPaymentModal,
            }),
        ),
        el('div', { className: 'card__body' }, paymentsTableOrEmpty()),
    );

    container.append(card);
}

function paymentsTableOrEmpty() {
    if (payments.length === 0) {
        return emptyState('💳', 'Нет платежей. Добавьте первый.');
    }

    const thead = el('thead', {},
        el('tr', {},
            el('th', { textContent: 'Дата' }),
            el('th', { textContent: 'Сумма' }),
            el('th', { textContent: 'ID' }),
        )
    );

    const tbody = el('tbody');
    for (const p of payments) {
        tbody.append(
            el('tr', {},
                el('td', { className: 'cell-date', textContent: formatDate(p.paymentDate) }),
                el('td', { className: 'cell-amount', textContent: formatAmount(p.amount) }),
                el('td', { className: 'cell-data', textContent: `#${p.id}` }),
            )
        );
    }

    return el('div', { className: 'table-wrap' },
        el('table', { className: 'table' }, thead, tbody)
    );
}

// ------------------------------------------------------------------
// Показания счётчиков
// ------------------------------------------------------------------
async function loadReadings() {
    const container = $('readings-section');

    // Если услуга не поддерживает счётчик — показываем информационную плашку
    if (serviceInfo && !serviceInfo.hasMeter) {
        clear(container);
        container.append(
            el('div', { className: 'card animate-in' },
                el('div', { className: 'card__header' },
                    el('span', { className: 'card__title', textContent: 'Показания счётчика' }),
                ),
                el('div', { className: 'card__body' },
                    emptyState('🚫', `Услуга «${serviceName(account.serviceType)}» не поддерживает показания счётчика`)
                ),
            )
        );
        return;
    }

    showLoader(container, 'Загрузка показаний...');

    try {
        readings = await readingsApi.getByAccount(accountId);
        renderReadings(container);
    } catch (err) {
        const apiErr = await parseApiError(err);
        clear(container);
        container.append(emptyState('⚠', apiErr.message));
    }
}

function renderReadings(container) {
    clear(container);

    const digits = serviceInfo?.meterDigits || '?';

    const card = el('div', { className: 'card animate-in' },
        el('div', { className: 'card__header' },
            el('span', { className: 'card__title', textContent: `Показания (${readings.length})` }),
            el('button', {
                className: 'btn btn--primary btn--sm',
                textContent: '+ Новое показание',
                onClick: openReadingModal,
            }),
        ),
        el('div', { className: 'card__body' }, readingsTableOrEmpty()),
    );

    container.append(card);
}

function readingsTableOrEmpty() {
    if (readings.length === 0) {
        return emptyState('📊', 'Нет показаний. Передайте первое.');
    }

    const thead = el('thead', {},
        el('tr', {},
            el('th', { textContent: 'Дата' }),
            el('th', { textContent: 'Показание' }),
            el('th', { textContent: 'ID' }),
        )
    );

    const tbody = el('tbody');
    for (const r of readings) {
        tbody.append(
            el('tr', {},
                el('td', { className: 'cell-date', textContent: formatDate(r.readingDate) }),
                el('td', { className: 'cell-data', textContent: r.value }),
                el('td', { className: 'cell-data', textContent: `#${r.id}` }),
            )
        );
    }

    return el('div', { className: 'table-wrap' },
        el('table', { className: 'table' }, thead, tbody)
    );
}

// ------------------------------------------------------------------
// Модалка: Новый платёж
// ------------------------------------------------------------------
function openPaymentModal() {
    const form = el('div', { className: 'form-grid' },
        // Сумма
        formGroup('amount', 'Сумма (₽)', 'number', {
            placeholder: '1500.50',
            step: '0.01',
            min: '0.01',
            className: 'form-input form-input--mono',
        }),

        // Дата
        formGroup('paymentDate', 'Дата платежа', 'date', {
            value: todayISO(),
            className: 'form-input',
        }),

        // Кнопки
        el('div', { className: 'form-actions' },
            el('button', {
                className: 'btn btn--primary',
                textContent: 'Сохранить',
                id: 'submit-payment',
                onClick: () => submitPayment(modal),
            }),
        ),
    );

    const modal = openModal('Новый платёж', form);
}

async function submitPayment(modal) {
    const modalBody = modal.overlay.querySelector('.modal__body');
    clearFormErrors(modalBody);

    const amount      = modalBody.querySelector('[name="amount"]').value;
    const paymentDate = modalBody.querySelector('[name="paymentDate"]').value;

    // Клиентская валидация
    const { valid, errors } = validate({ amount, paymentDate }, {
        amount:      rules.minExclusive(0, 'Сумма должна быть > 0'),
        paymentDate: rules.required('Дата обязательна'),
    });

    if (!valid) {
        highlightErrors(modalBody, errors);
        return;
    }

    // Блокируем кнопку
    const btn = modalBody.querySelector('#submit-payment');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Сохранение...';

    try {
        await paymentsApi.create(accountId, {
            amount: Number(amount),
            paymentDate,
        });

        modal.close();
        await loadPayments(); // Перезагрузить таблицу
    } catch (err) {
        const apiErr = await parseApiError(err);
        showFormErrors(modalBody, apiErr);
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

// ------------------------------------------------------------------
// Модалка: Новое показание
// ------------------------------------------------------------------
function openReadingModal() {
    const digits = serviceInfo?.meterDigits || 4;

    const form = el('div', { className: 'form-grid' },
        // Показание
        formGroup('value', `Показание (${digits} цифр)`, 'text', {
            placeholder: '0'.repeat(digits),
            maxLength: String(digits),
            className: 'form-input form-input--mono',
        },
            el('span', { className: 'form-hint', textContent: `Ровно ${digits} цифр, только 0-9` })
        ),

        // Дата
        formGroup('readingDate', 'Дата показания', 'date', {
            value: todayISO(),
            className: 'form-input',
        }),

        // Кнопки
        el('div', { className: 'form-actions' },
            el('button', {
                className: 'btn btn--primary',
                textContent: 'Сохранить',
                id: 'submit-reading',
                onClick: () => submitReading(modal),
            }),
        ),
    );

    const modal = openModal('Новое показание', form);
}

async function submitReading(modal) {
    const modalBody = modal.overlay.querySelector('.modal__body');
    clearFormErrors(modalBody);

    const value       = modalBody.querySelector('[name="value"]').value;
    const readingDate = modalBody.querySelector('[name="readingDate"]').value;
    const digits      = serviceInfo?.meterDigits || 4;

    // Клиентская валидация
    const { valid, errors } = validate({ value, readingDate }, {
        value: (v) => {
            if (!v) return 'Обязательное поле';
            if (!/^[0-9]+$/.test(v)) return 'Допустимы только цифры';
            if (v.length !== digits) return `Должно быть ровно ${digits} цифр`;
            return null;
        },
        readingDate: rules.required('Дата обязательна'),
    });

    if (!valid) {
        highlightErrors(modalBody, errors);
        return;
    }

    const btn = modalBody.querySelector('#submit-reading');
    const originalText = btn.textContent;
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner"></span> Сохранение...';

    try {
        await readingsApi.create(accountId, { value, readingDate });
        modal.close();
        await loadReadings();
    } catch (err) {
        const apiErr = await parseApiError(err);
        showFormErrors(modalBody, apiErr);
    } finally {
        btn.disabled = false;
        btn.textContent = originalText;
    }
}

// ------------------------------------------------------------------
// Хелперы форм
// ------------------------------------------------------------------

/**
 * Создать группу формы (label + input + error placeholder).
 */
function formGroup(name, label, type, inputAttrs = {}, ...extra) {
    const input = el('input', {
        type,
        name,
        className: inputAttrs.className || 'form-input',
        ...inputAttrs,
    });

    return el('div', { className: 'form-group' },
        el('label', { className: 'form-label', textContent: label }),
        input,
        el('span', { className: 'form-error', dataset: { field: name } }),
        ...extra,
    );
}

/**
 * Подсветить ошибки валидации на форме.
 */
function highlightErrors(container, errors) {
    for (const [field, msg] of Object.entries(errors)) {
        const input = container.querySelector(`[name="${field}"]`);
        if (input) input.classList.add('error');

        const errorEl = container.querySelector(`.form-error[data-field="${field}"]`);
        if (errorEl) errorEl.textContent = msg;
    }
}

// ------------------------------------------------------------------
// Автозапуск
// ------------------------------------------------------------------
document.addEventListener('DOMContentLoaded', initAccountPage);
