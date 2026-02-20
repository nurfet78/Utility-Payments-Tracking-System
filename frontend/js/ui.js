/**
 * ui.js — Переиспользуемые UI-компоненты и утилиты для рендеринга.
 *
 * Содержит:
 * - создание DOM-элементов
 * - статус-бейджи
 * - модальные окна
 * - индикаторы загрузки
 * - форматирование данных
 * - отображение ошибок
 * - клиентская валидация
 */

// ==================================================================
// DOM-хелперы
// ==================================================================

/**
 * Создать DOM-элемент с атрибутами и дочерними элементами.
 * @param {string} tag
 * @param {Object} attrs — className, textContent, innerHTML, dataset, on* и прочие
 * @param {...(Node|string)} children
 * @returns {HTMLElement}
 */
export function el(tag, attrs = {}, ...children) {
    const element = document.createElement(tag);

    for (const [key, value] of Object.entries(attrs)) {
        if (key === 'className')    { element.className = value; }
        else if (key === 'textContent') { element.textContent = value; }
        else if (key === 'innerHTML')   { element.innerHTML = value; }
        else if (key === 'dataset')     { Object.assign(element.dataset, value); }
        else if (key.startsWith('on'))  { element.addEventListener(key.slice(2).toLowerCase(), value); }
        else                            { element.setAttribute(key, value); }
    }

    for (const child of children) {
        if (child == null) continue;
        element.append(typeof child === 'string' ? document.createTextNode(child) : child);
    }

    return element;
}

/** Очистить содержимое контейнера */
export function clear(container) {
    container.innerHTML = '';
}

/** Безопасно получить элемент по ID */
export function $(id) {
    return document.getElementById(id);
}

// ==================================================================
// Форматирование
// ==================================================================

/** Маппинг ServiceType → русское название */
const SERVICE_NAMES = {
    GAS:             'Газ',
    WATER:           'Вода',
    ELECTRICITY:     'Электроэнергия',
    INTERCOM:        'Домофон',
    HEATING:         'Отопление',
    ECO_RESOURCES:   'Экоресурсы',
    HOUSING_SERVICE: 'Жилсервис',
    CAPITAL_REPAIR:  'Капитальный ремонт',
};

/** Получить русское название услуги */
export function serviceName(code) {
    return SERVICE_NAMES[code] || code;
}

/** Маппинг статуса → { label, cssClass } */
const STATUS_CONFIG = {
    PAID_THIS_MONTH: {
        label:    'Оплачено',
        cssClass: 'badge--paid',
    },
    NOT_PAID_THIS_MONTH: {
        label:    'Не оплачено',
        cssClass: 'badge--not-paid',
    },
    OVERDUE: {
        label:    'Просрочено',
        cssClass: 'badge--overdue',
    },
};

/**
 * Создать бейдж статуса оплаты.
 * @param {string} status — PAID_THIS_MONTH | NOT_PAID_THIS_MONTH | OVERDUE
 * @returns {HTMLElement}
 */
export function statusBadge(status) {
    const config = STATUS_CONFIG[status] || { label: status, cssClass: '' };
    return el('span', { className: `badge ${config.cssClass}` },
        el('span', { className: 'badge__dot' }),
        config.label
    );
}

/**
 * Создать бейдж счётчика.
 * @param {boolean} hasMeter
 * @param {number|null} digits
 * @returns {HTMLElement}
 */
export function meterBadge(hasMeter, digits) {
    if (hasMeter) {
        return el('span', { className: 'badge badge--meter' }, `${digits} цифр`);
    }
    return el('span', { className: 'badge badge--no-meter' }, 'Нет');
}

/**
 * Форматировать сумму как валюту.
 * @param {number|string} amount
 * @returns {string}
 */
export function formatAmount(amount) {
    return new Intl.NumberFormat('ru-RU', {
        style: 'currency',
        currency: 'RUB',
        minimumFractionDigits: 2,
    }).format(Number(amount));
}

/**
 * Форматировать дату ISO → DD.MM.YYYY.
 * @param {string} isoDate — '2025-01-15'
 * @returns {string}
 */
export function formatDate(isoDate) {
    if (!isoDate) return '—';
    const [y, m, d] = isoDate.split('-');
    return `${d}.${m}.${y}`;
}

/**
 * Сегодняшняя дата в формате ISO (для value input[type=date]).
 * @returns {string} 'YYYY-MM-DD'
 */
export function todayISO() {
    return new Date().toISOString().slice(0, 10);
}

// ==================================================================
// Индикаторы загрузки
// ==================================================================

/** Создать спиннер загрузки (для контейнера) */
export function loader(text = 'Загрузка...') {
    return el('div', { className: 'loader' },
        el('div', { className: 'spinner' }),
        el('span', { className: 'loader-text', textContent: text })
    );
}

/** Показать спиннер в контейнере */
export function showLoader(container, text) {
    clear(container);
    container.append(loader(text));
}

// ==================================================================
// Пустое состояние
// ==================================================================

/**
 * Заглушка «пустой список».
 * @param {string} icon — эмодзи
 * @param {string} text
 * @returns {HTMLElement}
 */
export function emptyState(icon, text) {
    return el('div', { className: 'empty-state' },
        el('div', { className: 'empty-state__icon', textContent: icon }),
        el('div', { className: 'empty-state__text', textContent: text })
    );
}

// ==================================================================
// Уведомления / ошибки
// ==================================================================

/**
 * Показать алерт (error / success) в контейнере.
 * @param {HTMLElement} container
 * @param {'error'|'success'} type
 * @param {string} message
 */
export function showAlert(container, type, message) {
    const existing = container.querySelector('.alert');
    if (existing) existing.remove();

    const alert = el('div', {
        className: `alert alert--${type}`,
        textContent: message,
    });
    container.prepend(alert);

    // Автоскрытие success через 4 с
    if (type === 'success') {
        setTimeout(() => alert.remove(), 4000);
    }
}

/** Убрать алерт из контейнера */
export function clearAlert(container) {
    const alert = container.querySelector('.alert');
    if (alert) alert.remove();
}

/**
 * Показать серверные ошибки из parseApiError().
 * Если есть fieldErrors — подсвечивает конкретные поля.
 * @param {HTMLElement} formContainer — родительский элемент формы / модалки
 * @param {{ message: string, fieldErrors: Object|null }} apiError
 */
export function showFormErrors(formContainer, apiError) {
    // Общее сообщение
    showAlert(formContainer, 'error', apiError.message);

    // Подсветка отдельных полей
    if (apiError.fieldErrors) {
        for (const [field, msg] of Object.entries(apiError.fieldErrors)) {
            const input = formContainer.querySelector(`[name="${field}"]`);
            if (input) {
                input.classList.add('error');
                const hint = input.parentElement.querySelector('.form-error');
                if (hint) hint.textContent = msg;
            }
        }
    }
}

/** Сбросить ошибки формы */
export function clearFormErrors(formContainer) {
    clearAlert(formContainer);
    formContainer.querySelectorAll('.form-input.error, .form-select.error')
        .forEach(el => el.classList.remove('error'));
    formContainer.querySelectorAll('.form-error')
        .forEach(el => el.textContent = '');
}

// ==================================================================
// Модальное окно
// ==================================================================

/**
 * Открыть модальное окно.
 * @param {string} title
 * @param {HTMLElement} bodyContent — содержимое тела
 * @returns {{ overlay: HTMLElement, close: Function }}
 */
export function openModal(title, bodyContent) {
    const overlay = el('div', { className: 'modal-overlay' },
        el('div', { className: 'modal' },
            el('div', { className: 'modal__header' },
                el('h3', { className: 'modal__title', textContent: title }),
                el('button', {
                    className: 'modal__close',
                    innerHTML: '&times;',
                    onClick: () => closeModal(overlay),
                    'aria-label': 'Закрыть',
                })
            ),
            el('div', { className: 'modal__body' }, bodyContent)
        )
    );

    // Закрытие по клику на оверлей
    overlay.addEventListener('click', (e) => {
        if (e.target === overlay) closeModal(overlay);
    });

    // Закрытие по Escape
    const escHandler = (e) => {
        if (e.key === 'Escape') {
            closeModal(overlay);
            document.removeEventListener('keydown', escHandler);
        }
    };
    document.addEventListener('keydown', escHandler);

    document.body.append(overlay);

    // Триггер анимации
    requestAnimationFrame(() => overlay.classList.add('visible'));

    return {
        overlay,
        close: () => closeModal(overlay),
    };
}

function closeModal(overlay) {
    overlay.classList.remove('visible');
    setTimeout(() => overlay.remove(), 200);
}

// ==================================================================
// Клиентская валидация
// ==================================================================

/**
 * Провалидировать поля формы по правилам.
 * @param {Object} data — значения полей { field: value }
 * @param {Object} rules — правила { field: (value) => errorMsg | null }
 * @returns {{ valid: boolean, errors: Object }}
 */
export function validate(data, rules) {
    const errors = {};
    let valid = true;

    for (const [field, rule] of Object.entries(rules)) {
        const error = rule(data[field]);
        if (error) {
            errors[field] = error;
            valid = false;
        }
    }

    return { valid, errors };
}

/** Стандартные правила валидации */
export const rules = {
    required: (msg = 'Обязательное поле') =>
        (v) => (!v || !String(v).trim()) ? msg : null,

    pattern: (regex, msg) =>
        (v) => (v && !regex.test(v)) ? msg : null,

    minExclusive: (min, msg) =>
        (v) => (v !== '' && Number(v) <= min) ? msg : null,

    exactLength: (len, msg) =>
        (v) => (v && v.length !== len) ? (msg || `Должно быть ровно ${len} символов`) : null,
};
