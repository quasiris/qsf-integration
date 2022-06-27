# Changelog
All notable changes qsf-integration project will be documented in this file.

## [UNRELEASED]
### Fixed
- fix http oom

## [5.6]
### Added
- make it possible to stop a pipeline in a query transformer

### Changed
- use new elastic clients
- move elastic dto to qsf-dto

### Removed
- remove jsonassert dependency

## [5.5]
### Added
- implement explain for tracking filter
- implement tagging and excluding of facets

## [5.4]
### Added
- add tags to SpellCheckContext
- add explain information for spellcheck
- add sorting for spellcheck query
- implement category select facetting

### Changed
- only set the searchFilters once
- use tree field for facetting category select filter
- FORCE_HTTP_1 for async requests due HTTPASYNC-164

## [5.3]
### Changed
- update httpcomponents to 5.x
- implement a query parameter to disable the spellcheck
- implement explain feature

### Fixed
- set static timeout for ElasticHttpClient.postAsync, to avoid oom

## [5.2]
### Added
- implement a json assert for testing
- implement field mapping with support for wildcards
- add support for jsonPath array result
- implement field grouping for elastic collapsing
- 
### Changed
- improve the spellchecking filter for parallel execution

### Breaking Changes

### Deprecated

### Fixed
- fix filter building for elastic query transformer

## [5.1]
### Added
- support stats aggregations for elastic

### Changed
- move the logic to map the variant count to the result mapping transformer
- improve parsing nested aggregations

### Breaking Changes

### Deprecated

### Fixed


## [5.0]
### Added
- support variants in elastic queries

### Changed
- restructure the search query parser to make it more modular
- refactor build filter

### Breaking Changes

### Deprecated
- remove support for elastic <=2.x

### Fixed
- fixed NPE for profile loader in case of null parameters
## [4.0]
### Added
- add a result dto to the search query to make the result configurable in the request
- don't compute facets in case facets are disabled
- compute document positions
- ensure that the page is always > 0 for a search query dto
- map the parameters of the search query dto

### Changed
- fix NPE in case of no query


## [3.11]
### Added
- implement variants feature

## [3.10]
### Added
- add result and parameters to the search query
- increase test coverage

## [3.8]
### Added
- add a pausedUntil to the monitoring builder
- implement a facet filter

## [3.5]
### Added
- implement NOW and * parameter for daterange filter

## [3.4]
### Added
- implement a year facet type
- implement a DateFormatFacetKeyMapper

## [3.3]
### Added
- implement a human date filter

## [3.2]

### Changed
- refactor code to support load search-pipelines from yaml
- move some util-functions to qsf-common
- update jackson version


## [3.1]
### Added
- add mapper for SearchQuery and SearchQueryDTO
- add QsfSearchQueryParser for migration
- move qsc test framework to qsf

### Changed
- use qsf-commons dependency
- update jackson-dataformat-yaml to 2.10.3

### Fixed
- only create debug message in SolrFilter when debug mode is enabled

## [3.0]
### Changed
- use qsf-dto 1.0.0 dependency
- move some dto methods to factory classes

## [2.18]
### Added
- add methods to easily merge monitoring responses and create a empty search response
- add createFilters method

### Changed
- update guava version

### Deprecated
- deprecate QSFHttpServletRequest

## [2.17]
### Added
- add an operator to the facet to implement multi select facets
- implement multi select faceting for sliders
- add a data structure for redirects
- implement a DocumentStringSubstitutor to replace all variables in a string with the values of the document
- make it possible to override the loading of a profile
- add a new parameter ctrl - to control search actions like load more facets
- add a splitter to easily split values
- implement a feature to load more facets in qsfql
- remove password from url in Elastic Filter debug output

### Changed
- use StringSubstitutor instead of StrSubstitutor because it is deprecated

### Breaking Changes

### Fixed
- fix test execution by updating maven-surefire-plugin

## [2.16]
### Added
- add a possibility to stop a running pipeline
- add a possibility to restart a running pipeline
- add mapping for inner hits for collapsing

### Changed

### Breaking Changes
- use the Hit datastructure for innerhits in elastic result
- remove support for static defined filters in elastic profiles

### Deprecated
- don't use the request response from the pipeline countainer directly
- use the conditions instead of the notActiveFilters
- the success object is replaced by a status object
- the message is replaced by a status object
- the timeout is indicated by a status code

### Fixed
- keep the order of the configured facets for the elastic result mapper

## [2.15]
### Added
- make it possible to set a custom searchQuery object in the elastic query transformer
- implement tracking for range values
- implement date range queries for qsfql and elasticsearch
- implement a facet key mapper for elastic result transformer
- implement a date histogram facet for elastic
- parse the parameter tracking in the QSFQL


### Changed
- don't execute the the elastic filter in case the query is null
- use graal.js as script engine to allow compatibility for jdk15

### Breaking Changes
- set the default for upper bound range filters to include

## [2.14]
### Added
- implement sort rules
- add minTokenWeight for Spellcheck
- implement a logic to track the reason for a changed query
- implement a include for the json builder
- jsonBuilder: implement an exists function
- jsonBuilder: implement a function to create not existing paths pathsForceCreate
- elastic QsfqlFilterTransformer - make the location of the filters configurable with filterPath and filterVariable
- implement a Qsfql SearchFilterMatcher
- improve error logging for elastic filter

### Removed

### Changed
- migrate to junit 5 jupiter

### Breaking Changes
- the filters can not be set automatically in a combination with a function_score query. For this purpose a filterPath or filterVariable must be configured
- change parameter value type in search query to object
- change parameter value type in elastic transformer to object
### Deprecated

### Fixed

## [2.13]
### Added
- JsonBuilder: implement a logic to remove json nodes with the json substitutor
- JsonBuilder: allow to replace multiple json keys
- PipelineBuilder: allow to set an id for loop and condition filter for better debugging
- SuggestQueryTransoformer: implement qsfql filter
- add a SearchQueryFilter
- delete for elastic http client
- improve debugging of pipeline run
- implement debugging for spellcheck
- improve date utils
- support for array pretty print

### Removed

### Changed
- increase version of junit

### Deprecated


### Fixed
- fix usage of reserved chars in suggest


### Breaking changes
- - ElasticQsfqlQueryTransformer it is not possible to override detailed methods for transforming a filter. Implement a own FilterTransformer for this purpose.



## [2.12]
### Added
- DateUtil - implement an intelligent matching of the date pattern for ISO 8601 dates
- implement a processing time monitoring for update feeds
- add gitlab-ci
- add a java json builder
- add a json substitutor
- add a loop filter

### Removed

### Changed
- rename subfacet to children

### Deprecated
- MonitoringBuilder.processingTime is deprecated and will be removed in the next release. Use the processingTimeFull or processingTimeUpdate instead

### Fixed
- SpellCheckElasticFilter - implement max token length


## [2.11]
### Added
- implement a helper function in Document to get all values as a list

### Removed

### Changed

### Deprecated

### Fixed
- AbstractQueryParser: don't parse the query if there are no tokens 

## [2.10]
### Added
- implement a method to create a custom monitoring
- implement addFilter method to simplify the creation of filter
- implement a flag to deactive a monitoring
- add the possibility to set a field and a direction in the sort of the search query

### Removed

### Changed

### Deprecated

### Fixed
- don't throw an exception in case of a monitoring error


### Security

## [2.9] - 2020-07-13
### Added
- implement a wildcard field mapping for elastic

### Removed

### Changed

### Deprecated

### Fixed

### Security

## [2.8] - 2020-06-26
### Added
- ElasticSearch Query Builder select the source fields automatically if they are mapped
- add IOUtils to support io operations
- add a unzip method to the IOUtils
- implement a model repository manager
- implement a conditional filter
- implement a spellchecking filter
- implement a originalQuery parameter in the search query
- implement a queryChanged parameter in the search query
- implement a requestOrigin parameter for tracking in the search query
- implement a parameter to disable tracking in the search query
- make it possible to set tracking parameters in pipeline container
- implement a analyzer filter for elastic
- implement a filter to extract synonyms from the elastic analyzer
- implement logic to use filters in a match all query for elastic suggests
- add did you mean in search response
- add elastic explanation in debug mode

### Changed
- extend elastic debugging

### Fixed
- close the http coneection in async elastic client
- fix character escaping for elastic queries
- nullptr in monitoring
- javadoc warnings

### Breaking Changes
- ElasticUtil.escape is private

### Deprecations



## [2.7]
### Added
- add an id for the tracking filter
- implement logic to use filters in a match all query for elastic
- improve filter building for elastic
- make it possible to use string and date in range queries for elastic
- implement a search filter builder
- implement a not filter for the qsfql query
- add the request id to the pipeline container
- add support for elastic 7
- add a qsf exception converter to transform qsf exceptions to html
- add a function to merge monitoring responses
- add support for using lucene query in the elastic monitoring 
- add methods to get the value as integer or long from the document
- implement a or filter for elastic
- add support for inner hits in elastic
- make the rows configurable in the elastic filter builder
- add the possibility to use a function score query in elasticsearch profiles
- track the number of query tokens
- add elastic 7 support, add track_total_hits flag to profiles

### Changed
- change the handling of debug objects
- refactor the filter logic

### Fixed
- suggest query transformer: don't set the include parameter in case of an empty query
- add a default constructor to the data beans to allow deserialization
- improve computing of the status of a monitoring event
- bugfix for computing the correct total pages
- enable null values in query profiles
- json encode query token for suggets facet
- fix bug in suggest after entering a whitespace

### Breaking Changes
- the type of debug stack changed from List<Object> to List<Debug>

### Deprications

## [2.6]
### Added
- added filter rules for elastic
- default rows in QSFQLRequestFilter
- profile paramter for elastic
- added include, exclude rules for Facets
- implement a slider
- flag the selected facets, facet values in the response
- add a mapping for facet names in the elastic builder
- add a mapping for slider names in the elastic builder
- implement a tracking filter that read all relevant information from the search query and the search result
- extend the tracking filter to allow tracking to elasticsearch
- implement a tree filter and facet
- make the context accessable for the pipeline executer
- integrate travis and coversall
- implement a suggest for elastic

### Changed
- improve the setting of aggregations
- increase the aggregation size of the monitoring filter to 100

### Fixed
- fix bug for parameter encoding in the elastic filter

### Breaking Changes

### Deprications

## [2.5]
### Added
- add monitoring support

### Changed
- update jackson version
- update guava version
- update commons-text version
- update javax.servlet-api version
- update commons-codec version
- update slf4j-simple version
- update maven version
- improve thread handling in pipeline executer
- improve thread handling in Solr Filter
- improve handling of solr clients

### Fixed

### Breaking Changes
- it is not possible anymore to set the solr client in the pipeline

### Deprications

## [1.30]
### Added
- add a context to the search result
- add tokens
- add pos tags
- implement sub facet for elastic
- implement QSFHttpServletRequest
- implement health check for elastic
- implement health check for solr

### Changed

### Fixed
- set the total correctly in case of no documents in SingleSearchResponse


## [1.28] and previous- 2018-12-12
### Added

### Changed

### Fixed
