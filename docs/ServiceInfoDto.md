
# ServiceInfoDto

Информация о коммунальной услуге из фиксированного справочника

## Properties

Name | Type
------------ | -------------
`code` | string
`displayName` | string
`hasMeter` | boolean
`meterDigits` | number

## Example

```typescript
import type { ServiceInfoDto } from ''

// TODO: Update the object below with actual values
const example = {
  "code": GAS,
  "displayName": Газ,
  "hasMeter": true,
  "meterDigits": 4,
} satisfies ServiceInfoDto

console.log(example)

// Convert the instance to a JSON string
const exampleJSON: string = JSON.stringify(example)
console.log(exampleJSON)

// Parse the JSON string back to an object
const exampleParsed = JSON.parse(exampleJSON) as ServiceInfoDto
console.log(exampleParsed)
```

[[Back to top]](#) [[Back to API list]](../README.md#api-endpoints) [[Back to Model list]](../README.md#models) [[Back to README]](../README.md)


