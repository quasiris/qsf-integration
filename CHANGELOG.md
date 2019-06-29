# Changelog
All notable changes qsf-integration project will be documented in this file.


## [Unreleased]
### Added
- added filter rules for elastic
- default rows in QSFQLRequestFilter
- profile paramter for elastic

### Changed
- improve the setting of aggregations

### Fixed

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
