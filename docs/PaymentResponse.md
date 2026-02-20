
# PaymentResponse

Данные платежа по лицевому счёту

## Properties

Name | Type
------------ | -------------
`id` | number
`amount` | number
`paymentDate` | Date

## Example

```typescript
import type { PaymentResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": 42,
  "amount": 1500.5,
  "paymentDate": 2025-01-15,
} satisfies PaymentResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as PaymentResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


