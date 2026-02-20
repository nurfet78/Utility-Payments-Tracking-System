
# AccountWithStatusResponse

Лицевой счёт с вычисленным статусом оплаты за текущий месяц

## Properties

Name | Type
------------ | -------------
`accountId` | number
`serviceType` | string
`serviceDisplayName` | string
`accountNumber` | string
`status` | string
`statusDisplayName` | string

## Example

```typescript
import type { AccountWithStatusResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "accountId": 1,
  "serviceType": GAS,
  "serviceDisplayName": Газ,
  "accountNumber": 12345-01.7,
  "status": PAID_THIS_MONTH,
  "statusDisplayName": Оплачено в текущем месяце,
} satisfies AccountWithStatusResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as AccountWithStatusResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


