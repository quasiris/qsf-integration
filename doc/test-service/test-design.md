# Table of contents
<!-- TOC -->
* [Table of contents](#table-of-contents)
* [Building a TestSuite](#building-a-testsuite)
  * [`env` - Specifying Environments](#env---specifying-environments)
* [Building a TestCase](#building-a-testcase)
  * [`query` - Specifying the test query](#query---specifying-the-test-query)
  * [`assertions` - Specifying the expected query response](#assertions---specifying-the-expected-query-response)
    * [Notes on how to build assertions](#notes-on-how-to-build-assertions)
    * [Types of possible assertions elements](#types-of-possible-assertions-elements)
      * [building `"searchResponse"` assertions](#building-searchresponse-assertions)
      * [building `"singleSearchResponse"` assertions](#building-singlesearchresponse-assertions)
      * [building `"jsonPath"` assertions](#building-jsonpath-assertions)
      * [building `"suggest"` assertions](#building-suggest-assertions)
      * [building `"http"` assertions](#building-http-assertions)
    * [How to build a value test](#how-to-build-a-value-test)
      * [Available testValue Operators](#available-testvalue-operators)
    * [How to build a jsonPath test](#how-to-build-a-jsonpath-test)
    * [Available special elementKey operators for testing QSF documents](#available-special-elementkey-operators-for-testing-qsf-documents)
<!-- TOC -->


# Building a TestSuite
A TestSuite is a JSON representing a set of TestCases that should be executed for a specific test environment

| element key name | element value type                         | description                                                                                                                                                                                                                                                                                                                                                                                                                                               |
|------------------|--------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `env`            | JSON Object containing Environment Objects | The available environments for the TestSuite <br> Each environment has a name as JSON key and an Environment as JSON value <br> Environments can contain variables to parameterize the TestCases in the TestSuite <br> Read more in section [`env` - Specifying environments](#env---specifying-environments)                                                                                                                                             |
| `defaultEnv`     | String                                     | The default environment that should be used if a testcase has no environment defined                                                                                                                                                                                                                                                                                                                                                                      |
| `location`       | String                                     | (optional) The location where your TestCase files are stored on the filesystem <br> If TestCases are directly defined in TestSuite as JSON, this element is not required <br> Has to start with `classpath://` in order to reference testcases in the resources dir <br> e.g. `classpath://com/quasiris/qsf/test/testsuite/hb/testcases`                                                                                                                  |
| `testCases`      | JSON Array of TestCase Objects             | The list of TestCases that should be executed in this TestSuite <br> A TestCase can either be defined as JSON right in the TestSuite or as a JSON in a separate file named with the TestCase id as "<id>.json" <br> When specifying via JSON directly, refer to [Building a TestCase](#building-a-testcase) <br> When specifying a TestCase in a separate file, you have to specify at least its id, so the associated file of the TestCase can be loaded |

Example TestSuite
```json
{
  "env": {
    "local": {
      "variables" : {
        "baseUrl" : "http://localhost:8080",
        "serviceName" : ""
      }
    },
    "dev": {
      "variables" : {
        "baseUrl" : "https://search-service.dev.cloud.hosting.hornbach.de",
        "serviceName" : ""
      }
    }
  },
  "defaultEnv": "local",
  "location": "classpath://com/quasiris/qsf/test/testsuite/hb/testcases",
  "testCases": [
    {
      "id": "001-listing"
    },
    {
      "id": "002-suche-tapeziern",
      "envs": ["dev"]
    }
  ]
}
```


## `env` - Specifying Environments
The `env` Object contains a list of Environments. An Environment is a JSON representing a test environment containing an identifier and variables. <br>
Multiple environments can be used to parameterize TestCases. Environments can for example be used to parameterize the query URL.

An Environments Object is a JSON object containing Environments

An Environment is built as follows:
- An environment identifier as JSON key
- A JSON element containing a Variables List as JSON value

A Variables List is built as follows:
- `"variables"` as JSON key
- A JSON Object containing variables

A Variable is built as follows:
- A variable identifier as JSON key
- A JSON Object as value

Example Environments Object
```json
{
  "local": {
    "variables" : {
      "baseUrl" : "http://localhost:8080",
      "serviceName" : ""
    }
  },
  "dev": {
    "variables" : {
      "baseUrl" : "https://search-service.dev.cloud.hosting.hornbach.de",
      "serviceName" : ""
    }
  }
}
```

Example for a parameterized test query URL:
```json
{"url": "${baseUrl}/${serviceName}kam/search/articleDetail?f.articleCode=4625462&q=*&locale=de_DE"}
```


# Building a TestCase
A TestCase is a JSON representing a query against an API that should fulfill certain assertions for a specific test environment. <br>

| element key name | element value type     | description                                                                                                                                                                                           |
|------------------|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `"id"`           | String                 | Unique identifier to specify the TestCase within a TestSuite <br> **Note that the testcase filename has to be "<id>.json" !**                                                                         |
| `"name"`         | String                 | Human-readable name for the TestCase, used for outputs and logging                                                                                                                                    |
| `"comment"`      | String                 | (optional) Comment describing what kind of requirements or features the TestCase should cover                                                                                                         |
| `"active"`       | Boolean                | (optional) Note that the test is active in a TestSuite (has not function at test execution)                                                                                                           |
| `"envs"`         | JSON Array of Strings  | (optional) Restricts the TestCase to only run on the specified list of environments specified in TestSuite <br> e.g. `"envs: ["dev"]` if `"dev"` was defined in TestSuite                             |
| `"query"`        | JSON Query Object      | Test query that should be executed for this TestCase <br> Read more in section [`query` - Specifying the test query](#query---specifying-the-test-query)                                              |
| `"assertions"`   | JSON Assertions Object | Test assertions that should be executed on the query response <br> Read more in section [`assertions` - Specifying the expected query response](#assertions---specifying-the-expected-query-response) |

Example TestCase:
```json
{
  "id": "006-artikelDetail-hybris",
  "name": "Artikel Detail Hybris",
  "active": true,
  "envs": ["dev"],
  "query": {
    "url": "${baseUrl}/${serviceName}kam/search/articleDetail?f.articleCode=4625462&q=*&locale=de_DE"
  },
  "assertions": {
    "jsonPath": [
      {
        "fieldName": "result",
        "path": "categoryBase.documents[0].document.categoryName",
        "value": "Pflanzen"
      }
    ],
    "singleSearchResponse": [
      {
        "statusCode": 200
      }
    ],
    "searchResponse": [
      {
        "statusCode": 200,
        "result": {
          "categoryBase": {
            "statusCode": 200,
            "name": "categoryBase",
            "documents": [
              {
                "document": {
                  "_envs": [
                    "dev",
                    "int"
                  ],
                  "_total": ">:3000",
                  "categoryName": "Pflanzen",
                  "parentCategoryId": "S10011",
                  "categoryId": "S11175"
                }
              }
            ]
          },
          "articles": {
            "statusCode": 200,
            "name": "articles",
            "documents": [
              {
                "document": {
                  "_position": "top10",
                  "_envs": "int",
                  "_jsonPath": [
                    {
                      "fieldName": "data",
                      "path": "meta_title",
                      "value": "containsLowerCase:Rose"
                    }
                  ],
                  "sku": "10298269"
                }
              }
            ]
          }
        }
      }
    ]
  }
}
```


## `query` - Specifying the test query

A Query is a JSON representing a query against a QSF API

| element key name  | element value type      | description                                                                                                                                                                                                                                                                                                             |
|-------------------|-------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `"url"`           | String                  | The query URL that should be tested <br> Can be parameterized via variables defined in the test environment or in the `"variations"` element of the query <br> e.g. `"dev/kam/search/search?q=fliesen"` for a fixed query <br> or `"${baseUrl}/${serviceName}kam/search/search?q=fliesen"` for a parameterized query    |
| `"variations"`    | JSON Array of Variables | (optional) Variations can be used to test the same testcase against a variety of queries with different URLs via variables <br> The defined variables can be referenced with their key in the URL by using `${variable_key}` <br> Note that using `${variable_key.encoded}` additionally URL-encodes the variable value |

A Variable is built as follows:
- A variable identifier as JSON key
- A JSON Object as value, usually a String

Example Query:
```json
{
  "query": {
    "url": "${baseUrl}/search-autocomplete/v2/autocomplete/search/multiterms?q=${q.encoded}&locale=${locale}&market=740",
    "variations": [
      {
        "q": "wago",
        "locale": "de_LU"
      },
      {
        "q": "bosch",
        "locale": "fr_LU"
      }
    ]
  }
}
```


## `assertions` - Specifying the expected query response
Assertions represent the test assertions that you need to make sure that your search API works as intended. <br>
With the qsf-integration test service you can test for every JSON element that is possibly contained in your API response <br>

### Notes on how to build assertions
Since the APIs response structure is not trivial, building assertions for your tests can be difficult. Here are some notes
- Inspect example testcases to get a feeling of the overall TestSuite and TestCase structure
- As test and response structure are the same for `"searchResponse"`assertions, you can make an API query to your QSF based search API and use its response as starting point
  - Before making larger updates to your API it makes sense to create tests with your API responses as gold standard in order to test for possible response changes after the update
- Only keep elements in you assertions, you want to test for. All other elements can be removed, as there is no minimum requirement for elements in assertions
- Always make sure to either remove elements that change frequently, or to escape them with an `"is[Type]"` operator, so only their type is getting tested

### Types of possible assertions elements
There are 5 available types of assertions: `"searchResponse"`, `"singleSearchResponse"`, `"jsonPath"`, `"suggest"`, `"http"`:

#### building `"searchResponse"` assertions
The searchResponse assertion can be used to test for all sorts of elements of the search response and its contained search results <br>
The search response as well as the search results can get very complex, so using an API response as starting point for your test structure is recommended

A searchResponse assertion is built as follows:
- `"searchResponse"` as JSON key
- A SearchResponse object as value

The SearchResponse object and its structure are defined in [com.quasiris.qsf.dto.response.SearchResponse](https://github.com/quasiris/qsf-dto/blob/master/src/main/java/com/quasiris/qsf/dto/response/SearchResponse.java)

Note that a SearchResponse object can contain a JSON Array with multiple SearchResult objects in the `"result"` element. Their structure is defined in [com.quasiris.qsf.dto.response.SearchResult](https://github.com/quasiris/qsf-dto/blob/master/src/main/java/com/quasiris/qsf/dto/response/SearchResult.java)

Example searchResponse assertion:

```json
{
  "assertions": {
    "searchResponse": [
      {
        "statusCode": 200,
        "result": {
          "articles": {
            "statusCode": 200,
            "name": "articles",
            "documents": [
              {
                "document": {
                  "_position": "top10",
                  "_envs": "prd",
                  "_jsonPath": [
                    {
                      "fieldName": "data",
                      "path": "meta_title",
                      "value": "containsLowerCase:Sichtschutzhecke"
                    }
                  ]
                }
              },
              {
                "document": {
                  "_position": "0",
                  "_envs": [
                    "dev",
                    "int"
                  ],
                  "_jsonPath": [
                    {
                      "fieldName": "data",
                      "path": "meta_title",
                      "value": "containsLowerCase:Sichtschutz"
                    }
                  ]
                }
              }
            ],
            "total": 1700,
            "facets": [
              {
                "name": "categoryId",
                "id": "categoryId",
                "filterName": "f.categoryId.or",
                "count": 2000,
                "values": [
                  {
                    "value": "S11418",
                    "count": 50
                  }
                ]
              }
            ],
            "facetCount": 10
          }
        }
      }
    ]
  }
}
```

#### building `"singleSearchResponse"` assertions
The singleSearchResponse assertion can be used to test for statusCode, time, total amount of documents and for contents of all Documents in a searchResponse.

A singleSearchResponse assertion is built as follows:
- `"singleSearchResponse"` as JSON key
- A SingleSearchResponse object as value

The SingleSearchResponse object and its structure are defined in [com.quasiris.qsf.dto.response.SingleSearchResponse](https://github.com/quasiris/qsf-dto/blob/master/src/main/java/com/quasiris/qsf/dto/response/SingleSearchResponse.java)

Example singleSearchResultResponse:

```json
{
  "assertions": {
    "singleSearchResponse": [
      {
        "statusCode": 200
      }
    ]
  }
}
```

#### building `"jsonPath"` assertions
The jsonPath assertion can be used to test a set of JSON elements by specifying their path in the searchResponse, instead of representing them with their complete JSON structure. <br>
This makes testing for a limited set of elements much easier, since you don't have to keep the correct searchResponse structure and instead only specify the root element and the JsonPath from it, in order to test for some value

A jsonPath assertion is built as follows:
- `"jsonPath"` as JSON key
- A JSON Array of JsonPath tests as value

How JsonPath tests are built is described in [How to build a JsonPath test](#how-to-build-a-jsonpath-test). <br>
Note that the `fieldName` element needs to be an element of the searchResponse in order to work as path root

Example jsonPath assertion:
```json
{
  "assertions": {
    "jsonPath": [
      {
        "fieldName": "data",
        "path": "searchresult[0].brand",
        "value": "Flairstone"
      },
      {
        "fieldName": "data",
        "path": "searchresult[0].count",
        "value": ">:2000"
      }
    ]
  }
}
```

#### building `"suggest"` assertions
The suggest assertion can be used to test for suggest responses

A suggest assertion is built as follows:
- `"suggest"` as JSON key
- A JSON Array of Suggest objects as values

A Suggest object can be built as follows:
- `"type"` as JSON key
- A String as JSON value

Example suggest assertion:

```json
{
  "assertions": {
    "suggest": [
      {
        "type": "product"
      },
      {
        "type": "sample"
      }
    ]
  }
}
```


#### building `"http"` assertions
The http assertion can be used to test for HTTP headers and status codes.

A http assertion is built as follows:
- `"http"` as JSON key
- A JSON Array of Http objects as value

A Http object is built as follows:
- a statusCode element
  - `"statusCode"` as JSON key
  - an HTTP status Code as Value (can be Integer or String)
- a header element
  - `"header"` as JSON key
  - A JSON Array of HttpHeader objects as value

A HttpHeader object is built as follows:
- a name element
  - `"name"` as JSON key
  - A String as JSON value
- a value element
  - `"value"` as JSON key
  - A String as JSON value

Example http assertion:

```json
{
  "assertions": {
    "http": [
      {
        "statusCode": 200,
        "header": [
          {
            "name": "content-type",
            "value": "application/json"
          },
          {
            "name": "locale",
            "value": "de_DE"
          }
        ]
      }
    ]
  }
}
```



### How to build a value test
When testing an API for certain JSON elements, we have to differentiate between
- The **elementKey** for which responses should be tested
- The **testValue** that represents some kind of expected value
- The **responseValue** that is received by the API

A value test is a JSON element that consists of the **elementKey** and the **testValue**
- The elementKey needs to retain the JSON path of the expected response in order to test its value
  - e.g. `"productAttributes": {"color": "contains:blue"}` tests if the response for key `"productAttributes"` exists and if it contains and element with key `"color"` that has a value containing the String `"blue"`
- The testValue can represent the expected response value or a value used by one of the available test operators
  - e.g. `"age": 35` tests if the response for key `"age"` was the number `35` or the String `"35"`
  - e.g. `"name": "startsWith:John"` tests if the response for key `"name"` starts with the word `"John"`
  - **Note that the testValue cannot be `null` since testing for null responses is currently not supported**

#### Available testValue Operators
- **To specify a testValue using an operator, use `operator:expectedValue`** <br> e.g. `"value": "less:100"` to test for value to be lower than 100
- When not specifying an operator, `equals` will be used as default operator
- Numerical comparisons can only be done if expected and provided value can be parsed as number. If one or both cannot be parsed as number, the values are compared lexicographically!
- You cannot use the operator escape symbol `:` in Strings that you want to test for //TODO this will change with a suggested future implementation

| operator               | description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
|------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `equals`, `=`          | Default operator <br> Can be used to compare two numbers numerically <br> Can be used to compare two Strings lexicographically <br> e.g. `"age": "42"` to test for age to be equal to 42                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          |
| `equalsIgnoreCase`     | Can be used to compare two Strings lexicographically but case-insensitive <br> e.g. `"format": "equalsIgnoreCase:jpeg-image"` to test for format to be equal to `"JPEG-Image"` case-insensitive                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   |
| `contains`             | Can be used to check if a responseValue String contains an expected String <br> e.g. `"car": "contains:Mercedes"` to test for car containing the word Mercedes                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `containsIgnoreCase`   | Can be used to check if a responseValue String contains an expected String but case-insensitive <br> e.g. `"color": "containsIgnoreCase:blue"` to test for color to contain the word blue case-insensitive                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        |
| `startsWith`           | Can be used to check if a responseValue String starts with an expected String <br> e.g. `"name": "startsWith:John"` to test for name to start with the word John                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `startsWithIgnoreCase` | Can be used to check if a responseValue String starts with an expected String but case-insensitive <br> e.g. `"name": "startsWithIgnoreCase:John"` to test for name to start with the word John case-insensitive                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
| `isDateTime`           | Test will fail if responseValue is not a valid timestamp <br> To evaluate timestamps, [com.quasiris.qsf.commons.util.DateUtil.getDate()](https://github.com/quasiris/qsf-commons/blob/master/src/main/java/com/quasiris/qsf/commons/util/DateUtil.java) is used, which tries to parse Strings with known timestamp pattern lengths using [java.text.SimpleDateFormat](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/text/SimpleDateFormat.html). This method supports the following patterns: <ul><li>`"yyyy-MM-dd"` e.g. `"2020-08-06"`</li><li>`"yyyy-MM-dd'T'HH:mm:ssZ"` e.g. `"2020-08-06T22:18:26+0000"`</li><li>`"yyyy-MM-dd'T'HH:mm:ss.SSSZ"` e.g. `"2020-08-06T22:18:26.528+0000"`</li><li>`"2020-08-06T22:18:26.528+00:00"` e.g. `"yyyy-MM-dd'T'HH:mm:ss.SSSXXX"`</li></ul> For timestamp patterns with other lengths than 10, 24, 28 or 29 symbols, the fallback method tries to parse to a ISO-8601 timestamp using [java.time.Instant](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Instant.html). //TODO future change in qsf.commons might support all possible patterns described by ISO-8601<br> Note that currently you have to specify some expectedValue, even though this operator won't use it |
| `isUri`                | **Not yet implemented** <br> Test will fail if responseValue is not a valid URI <br> Valid URIs are specified by [RFC 2396](https://www.ietf.org/rfc/rfc2396.txt) and [RFC 2732](https://www.ietf.org/rfc/rfc2732.txt)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `isUrl`                | **Not yet implemented** <br> Test will fail if responseValue is not a valid URL <br> Valid URLs are specified by [RFC 2396](https://www.ietf.org/rfc/rfc2396.txt) and [RFC 2732](https://www.ietf.org/rfc/rfc2732.txt)                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `isBoolean`            | Test will fail if responseValue is not `"true"`, `true`, `"false"` or `false` <br> e.g. `"available": "isBoolean:foo"` will not fail if available is false <br> Note that currently you have to specify some expectedValue, even though this operator won't use it                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| `isNumber`             | **Not yet implemented** <br> Test will fail if responseValue cannot be parsed as number                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `isString`             | **Not yet implemented** <br> Test will fail if responseValue is DateTime, Path, Url, Boolean, Number or `null`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    |
| `exists`               | Test will fail if responseValue is `null` or does not exist <br> e.g. `"childNum": "exists:foo"` will fail if childNum is not present or `null` <br> Note that currently you have to specify some expectedValue, even though this operator won't use it                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| `greater`, `>`         | Can be used to compare two numbers numerically <br> Can be used to compare two Strings lexicographically <br> e.g. `"value": ">:100"` to test for the returned value to be greater than 100                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                       |
| `greaterEquals`, `>=`  | Can be used to compare two numbers numerically <br> Can be used to compare two Strings lexicographically <br> e.g. `"value": ">=:100"` to test for value to be greater or equal to 100                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            |
| `less`, `<`            | Can be used to compare two numbers numerically <br> Can be used to compare two Strings lexicographically <br> e.g. `"value": "<:100"` to test for value to be lower than 100                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      |
| `lessEquals`, `<=`     | Can be used to compare two numbers numerically <br> Can be used to compare two Strings lexicographically <br> e.g. `"value": "<=:100"` to test for value to be lower or equal to 100                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              |


### How to build a jsonPath test
A JsonPath object is a JSON Array containing jsonPtah test elements that consist of three elements:

| element key name | element value type | description                                                                                                                                                                                                    |
|------------------|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `"fieldName"`    | String             | The JSON key of the field in which the raw JSON that should be tested for is stored                                                                                                                            |
| `"path"`         | String             | A Jayway JsonPath to determine the key path for which the value in the raw JSON should be tested <br> See [JsonPath on Github](https://github.com/json-path/JsonPath#operators) for info how to build the path |
| `"value"`        | String             | A value test as described above in [How to build a value tests](#how-to-build-a-value-test)                                                                                                                    |

Example JsonPath Array:
```json
{
  "jsonPath": [
    {
      "fieldName": "data",
      "path": "[0].name",
      "value": "Startseite"
    },
    {
      "fieldName": "data",
      "path": "[4].name",
      "value": "Sonnenschirmständer"
    },
    {
      "fieldName": "data",
      "path": "searchresult[0].count",
      "value": ">:2000"
    }
  ]
}
```


### Available special elementKey operators for testing QSF documents
When testing for QSF search response documents ([com.quasiris.qsf.dto.response.Document](https://github.com/quasiris/qsf-dto/blob/master/src/main/java/com/quasiris/qsf/dto/response/Document.java)) you can use a set of additional special keys within the document in order to test for additional conditions of the documents.

Note that these are only keys, testValue Operators still apply for the values!

| element key name | element value type              | description                                                                                                                                                                                                                                                                                                                                                                                        |
|------------------|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `"_envs"`        | String or JSON Array of Strings | Can be used to test for the parent document only for certain environments <br> e.g. String `"dev"` <br> e.g. JSON Array of String `"_envs": ["dev", "int"]`                                                                                                                                                                                                                                        |
| `"_total"`       | Integer                         | Can be used to test for the total amount of document objects in the SearchResult documents <br> e.g. `"_total": ">:3000"`                                                                                                                                                                                                                                                                          |
| `"_position"`    | Integer                         | Can be used to test for the position of a parent document in the SearchResult documents <br> Can either be an Integer to determine the exact position <br> Or can be a String starting with `"top"` followed by an integer n to determine that the document was in the first n positions <br> e.g. exact at position 10: `"_position": "10"` <br> e.g. in top 10 positions: `"_position": "top10"` |
| `"_jsonPath"`    | JSON Array of JsonPath Objects  | Can be used to test for values in raw JSON Strings <br> See [How to build a jsonPath test](#how-to-build-a-jsonpath-test) for information on how to write such tests                                                                                                                                                                                                                               |
| `"_..."`         | any                             | All other elements with key names starting with `_` are ignored in testing <br> e.g. `"_special": "not implemented"` will be ignored in testing                                                                                                                                                                                                                                                    |
| `"#..."`         | any                             | All elements with key names starting with `#` are considered to be comments and thus ignored in testing <br> e.g. `"#notes": "This test "` will be ignored in testing                                                                                                                                                                                                                              |

```json
{
  "documents": [
    {
      "document": {
        "_envs": "prd",
        "_total": ">:3000",
        "abstractProductId": "6540097",
        "categoryName": "Pflanzen"
      }
    },
    {
      "document": {
        "_position": "top10",
        "_jsonPath": [
          {
            "fieldName": "data",
            "path": "meta_title",
            "value": "containsLowerCase:Rose"
          }
        ]
      }
    }
  ]
}
```
