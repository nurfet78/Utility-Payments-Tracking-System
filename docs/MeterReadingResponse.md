
# MeterReadingResponse

Данные показания счётчика

## Properties

Name | Type
------------ | -------------
`id` | number
`value` | string
`readingDate` | Date

## Example

```typescript
import type { MeterReadingResponse } from ''

// TODO: Update the object below with actual values
const example = {
  "id": 7,
  "value": 1234,
  "readingDate": 2025-01-20,
} satisfies MeterReadingResponse

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as MeterReadingResponse
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


