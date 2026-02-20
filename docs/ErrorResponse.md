
# ErrorResponse

Ответ при ошибке — содержит код, описание и опционально ошибки по полям

## Properties

Name | Type
------------ | -------------
`status` | number
`error` | string
`message` | string
`fieldErrors` | { [key: string]: string; }
`timestamp` | Date

## Example

```typescript
import type { ErrorResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "status": 400,
  "error": Business Rule Violation,
  "message": Лицевой счёт '12345' уже существует для услуги 'Газ',
  "fieldErrors": {accountNumber=Допустимые символы: цифры, точка, тире},
  "timestamp": 2025-01-15T14:30:00,
} satisfies ErrorResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ErrorResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


