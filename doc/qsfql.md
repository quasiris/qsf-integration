# QSFQL Quasiris Search Framework Query Language

The QSFQL ( Quasiris Search Framework Query Language ) is a standardised query language for search applications.
It covers the typical operations for a search application and is easy extensible for custom requirements.

## Encoding
All parameter values are transferred as url query parameters and have to be UTF-8 url encoded.

## Request ID (requestId=123e4567-e89b-12d3-a456-556642440000)
To track or monitor search queries in a microservice environment, a request id can be passed as a query parameter.

Example: https://api.quasiris.de/qsf-example?q=iphone&requestId=123e4567-e89b-12d3-a456-556642440000

## Query (q=foo)
The most important parameter for search applications is the query parameter q.

Examples:
- https://api.quasiris.de/qsf-example?q=iphone

## Pagination
For the pagination through the search results the query parameters page and rows can be used.

- page: The page of the search result that should be displayed. (default 0) 
- rows: The number of search result that are returned (default 10) The default can be changed in the configuration. (TODO link to config)

Examples:
- https://api.quasiris.de/qsf-example?q=iphone&page=5 (shows page 5 of the search result)
- https://api.quasiris.de/qsf-example?q=iphone&page=5&rows=3 (shows page 5 and 3 results per page)



## Filtering

The filters in the url are identified by a specific pattern. As default all parameters starting with a f.* are handled as
filter parameters.

The default can be change by a configuration. (TODO Link to configuration)


- default: f.fieldName.operator

- range filter

- filterPattern (f.name)


Examples:

- .and
- .or
- .range
- .slider


f.price.range=[5,10]
f.price.range=[min,10]
f.price.range=[5,max]
f.price.slider=[5,10]

### Date Range Filter
Filter documents by a single date field within a date range.

```
f.fieldName.daterange=minDate,maxDate
```

Examples:
```
f.timestamp.daterange=2021-01-02T23:00:00Z,2021-02-05T20:59:38Z
f.timestamp.daterange=NOW,*
f.timestamp.daterange=[NOW,2021-02-05]
f.timestamp.daterange={2021-01-01,*}
```

Boundary notation:
- `[` / `]` — inclusive (gte / lte)
- `{` / `}` — exclusive (gt / lt)
- `*` — open-ended (min or max date)
- `NOW` — current timestamp

### Date Range In Period Filter
Filter documents whose date range overlaps with a given period.
This is useful for calendar-like queries where documents have a start and end date,
and you want all documents that are active within a specific time window.

For `f.date.dateRangeInPeriod` the fields `startDate` and `endDate` are used for the overlap check: `startDate <= periodEnd AND endDate >= periodStart`

**GET API:**
```
f.date.dateRangeInPeriod=2026-02-09T00:00:00Z,2026-02-15T23:59:59Z
```

**POST API:**
```json
{
  "searchFilters": [
    {
      "id": "date",
      "filterType": "dateRangeInPeriod",
      "minValue": "2026-02-09T00:00:00Z",
      "maxValue": "2026-02-15T23:59:59Z"
    }
  ]
}
```

## Faceting

## Sorting
 