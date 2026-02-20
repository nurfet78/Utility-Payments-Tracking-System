# DefaultApi

All URIs are relative to *http://localhost:8888*

| Method | HTTP request | Description |
|------------- | ------------- | -------------|
| [**create**](DefaultApi.md#create) | **POST** /accounts | Создать лицевой счёт |
| [**create1**](DefaultApi.md#create1) | **POST** /accounts/{accountId}/readings | Передать показание счётчика |
| [**create2**](DefaultApi.md#create2) | **POST** /accounts/{accountId}/payments | Зарегистрировать платёж |
| [**getAll**](DefaultApi.md#getall) | **GET** /services | Получить список всех услуг |
| [**getByAccount**](DefaultApi.md#getbyaccount) | **GET** /accounts/{accountId}/readings | Получить историю показаний |
| [**getByAccount1**](DefaultApi.md#getbyaccount1) | **GET** /accounts/{accountId}/payments | Получить историю платежей |
| [**getById**](DefaultApi.md#getbyid) | **GET** /accounts/{id} | Получить лицевой счёт по ID |
| [**getWithStatus**](DefaultApi.md#getwithstatus) | **GET** /accounts/with-status | Получить все счета со статусом оплаты |



## create

> AccountResponse create(accountCreateRequest)

Создать лицевой счёт

Создаёт новый лицевой счёт для указанной услуги. Номер счёта должен быть уникален в рамках типа услуги. Допустимые символы в номере: цифры, точка, тире.

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { CreateRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  const body = {
    // AccountCreateRequest | Данные для создания лицевого счёта
    accountCreateRequest: ...,
  } satisfies CreateRequest;

  try {
    const data = await api.create(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **accountCreateRequest** | [AccountCreateRequest](AccountCreateRequest.md) | Данные для создания лицевого счёта | |

### Return type

[**AccountResponse**](AccountResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Лицевой счёт успешно создан |  -  |
| **400** | Ошибка валидации или дубликат счёта |  -  |
| **500** | Внутренняя ошибка сервера |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## create1

> MeterReadingResponse create1(accountId, meterReadingCreateRequest)

Передать показание счётчика

Регистрирует показание прибора учёта для указанного лицевого счёта.  Ограничения: • Услуга должна поддерживать счётчик (hasMeter &#x3D; true) • Показание — строка из цифр строго фиксированной длины:   - Газ: ровно 4 цифры (например, \&quot;0523\&quot;)   - Вода: ровно 4 цифры (например, \&quot;1047\&quot;)   - Электроэнергия: ровно 5 цифр (например, \&quot;28319\&quot;) • Для услуг без счётчика (Домофон, Отопление и др.) операция запрещена

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { Create1Request } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  const body = {
    // number | ID лицевого счёта (услуга должна поддерживать счётчик)
    accountId: 1,
    // MeterReadingCreateRequest | Показание счётчика и дата снятия
    meterReadingCreateRequest: ...,
  } satisfies Create1Request;

  try {
    const data = await api.create1(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **accountId** | `number` | ID лицевого счёта (услуга должна поддерживать счётчик) | [Defaults to `undefined`] |
| **meterReadingCreateRequest** | [MeterReadingCreateRequest](MeterReadingCreateRequest.md) | Показание счётчика и дата снятия | |

### Return type

[**MeterReadingResponse**](MeterReadingResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Показание успешно зарегистрировано |  -  |
| **400** | Ошибка бизнес-логики: • услуга не поддерживает счётчик • неверная длина показания • недопустимые символы |  -  |
| **404** | Лицевой счёт с указанным ID не найден |  -  |
| **500** | Внутренняя ошибка сервера |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## create2

> PaymentResponse create2(accountId, paymentCreateRequest)

Зарегистрировать платёж

Создаёт новый платёж для указанного лицевого счёта. Сумма должна быть строго больше нуля. Дата платежа обязательна.

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { Create2Request } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  const body = {
    // number | ID лицевого счёта
    accountId: 1,
    // PaymentCreateRequest | Данные платежа: сумма и дата
    paymentCreateRequest: ...,
  } satisfies Create2Request;

  try {
    const data = await api.create2(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **accountId** | `number` | ID лицевого счёта | [Defaults to `undefined`] |
| **paymentCreateRequest** | [PaymentCreateRequest](PaymentCreateRequest.md) | Данные платежа: сумма и дата | |

### Return type

[**PaymentResponse**](PaymentResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: `application/json`
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **201** | Платёж успешно зарегистрирован |  -  |
| **400** | Ошибка валидации: отрицательная/нулевая сумма или отсутствует дата |  -  |
| **404** | Лицевой счёт с указанным ID не найден |  -  |
| **500** | Внутренняя ошибка сервера |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getAll

> Array&lt;ServiceInfoDto&gt; getAll()

Получить список всех услуг

Возвращает полный справочник коммунальных услуг. Набор фиксирован: Газ, Вода, Электроэнергия, Домофон, Отопление, Экоресурсы, Жилсервис, Капитальный ремонт. Для каждой услуги указано наличие счётчика и количество цифр в показании.

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { GetAllRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  try {
    const data = await api.getAll();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**Array&lt;ServiceInfoDto&gt;**](ServiceInfoDto.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Список услуг успешно получен |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getByAccount

> Array&lt;MeterReadingResponse&gt; getByAccount(accountId)

Получить историю показаний

Возвращает все показания счётчика по лицевому счёту, отсортированные по дате (новые первыми).

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { GetByAccountRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  const body = {
    // number | ID лицевого счёта
    accountId: 1,
  } satisfies GetByAccountRequest;

  try {
    const data = await api.getByAccount(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **accountId** | `number` | ID лицевого счёта | [Defaults to `undefined`] |

### Return type

[**Array&lt;MeterReadingResponse&gt;**](MeterReadingResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | История показаний успешно получена |  -  |
| **404** | Лицевой счёт с указанным ID не найден |  -  |
| **500** | Внутренняя ошибка сервера |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getByAccount1

> Array&lt;PaymentResponse&gt; getByAccount1(accountId)

Получить историю платежей

Возвращает список всех платежей по лицевому счёту, отсортированных по дате (новые первыми).

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { GetByAccount1Request } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  const body = {
    // number | ID лицевого счёта
    accountId: 1,
  } satisfies GetByAccount1Request;

  try {
    const data = await api.getByAccount1(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **accountId** | `number` | ID лицевого счёта | [Defaults to `undefined`] |

### Return type

[**Array&lt;PaymentResponse&gt;**](PaymentResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | История платежей успешно получена |  -  |
| **404** | Лицевой счёт с указанным ID не найден |  -  |
| **500** | Внутренняя ошибка сервера |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getById

> AccountResponse getById(id)

Получить лицевой счёт по ID

Возвращает данные лицевого счёта по его уникальному идентификатору.

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { GetByIdRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  const body = {
    // number | ID лицевого счёта
    id: 1,
  } satisfies GetByIdRequest;

  try {
    const data = await api.getById(body);
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters


| Name | Type | Description  | Notes |
|------------- | ------------- | ------------- | -------------|
| **id** | `number` | ID лицевого счёта | [Defaults to `undefined`] |

### Return type

[**AccountResponse**](AccountResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Лицевой счёт найден |  -  |
| **404** | Лицевой счёт с указанным ID не найден |  -  |
| **500** | Внутренняя ошибка сервера |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


## getWithStatus

> Array&lt;AccountWithStatusResponse&gt; getWithStatus()

Получить все счета со статусом оплаты

Возвращает все лицевые счета с динамически вычисленным статусом оплаты за текущий месяц.  Правила определения статуса: • PAID_THIS_MONTH — есть хотя бы один платёж в текущем месяце • NOT_PAID_THIS_MONTH — платежей нет, текущая дата ≤ 25 • OVERDUE — платежей нет, текущая дата &gt; 25  Выполняется за 2 SQL-запроса (без N+1).

### Example

```ts
import {
  Configuration,
  DefaultApi,
} from '';
import type { GetWithStatusRequest } from '';

async function example() {
  console.log("🚀 Testing  SDK...");
  const api = new DefaultApi();

  try {
    const data = await api.getWithStatus();
    console.log(data);
  } catch (error) {
    console.error(error);
  }
}

// Run the test
example().catch(console.error);
```

### Parameters

This endpoint does not need any parameter.

### Return type

[**Array&lt;AccountWithStatusResponse&gt;**](AccountWithStatusResponse.md)

### Authorization

No authorization required

### HTTP request headers

- **Content-Type**: Not defined
- **Accept**: `application/json`


### HTTP response details
| Status code | Description | Response headers |
|-------------|-------------|------------------|
| **200** | Список счетов со статусами успешно получен |  -  |
| **500** | Внутренняя ошибка сервера |  -  |

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)

