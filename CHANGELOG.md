# Changelog
All notable changes qsf-integration project will be documented in this file.


## [Unreleased]
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

### Changed
- change the handling of debug objects

### Fixed
- suggest query transformer: don't set the include parameter in case of an empty query
- add a default constructor to the data beans to allow deserialization
- improve computing of the status of a monitoring event
- bugfix for computing the correct total pages

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
