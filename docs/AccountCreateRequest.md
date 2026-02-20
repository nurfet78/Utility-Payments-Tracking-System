
# AccountCreateRequest

Запрос на создание лицевого счёта

## Properties

Name | Type
------------ | -------------
`serviceType` | string
`accountNumber` | string

## Example

```typescript
import type { AccountCreateRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "serviceType": GAS,
  "accountNumber": 12345-01.7,
} satisfies AccountCreateRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as AccountCreateRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


