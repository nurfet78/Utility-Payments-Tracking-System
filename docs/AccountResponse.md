
# AccountResponse

Данные лицевого счёта

## Properties

Name | Type
------------ | -------------
`id` | number
`serviceType` | string
`serviceDisplayName` | string
`accountNumber` | string

## Example

```typescript
import type { AccountResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": 1,
  "serviceType": GAS,
  "serviceDisplayName": Газ,
  "accountNumber": 12345-01.7,
} satisfies AccountResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as AccountResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


