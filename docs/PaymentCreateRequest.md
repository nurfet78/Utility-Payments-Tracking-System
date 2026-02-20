
# PaymentCreateRequest

Запрос на создание платежа по лицевому счёту

## Properties

Name | Type
------------ | -------------
`amount` | number
`paymentDate` | Date

## Example

```typescript
import type { PaymentCreateRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "amount": 1500.5,
  "paymentDate": 2025-01-15,
} satisfies PaymentCreateRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PaymentCreateRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


