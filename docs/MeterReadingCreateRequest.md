
# MeterReadingCreateRequest

Запрос на передачу показания счётчика

## Properties

Name | Type
------------ | -------------
`value` | string
`readingDate` | Date

## Example

```typescript
import type { MeterReadingCreateRequest } from ''

// TODO: Update the object below with actual values
const example = {
  "value": 1234,
  "readingDate": 2025-01-20,
} satisfies MeterReadingCreateRequest

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as MeterReadingCreateRequest
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


