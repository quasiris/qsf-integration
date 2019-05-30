# Monitoring

With the QSF Integration framework it is easy to monitor search applications.
It is possible to define checks on specific search queries or on the whole dataset.
For the check the facets, documents and total hits can be used.


## Monitoring status
The check of a monitoring can result in the following status values:
- OK - everything is ok.
- WARN - the warn limit is raised.
- ERROR - the error limit is raised.
- UNKNOWN - the status is unknown. For example a not checked monitor remains in the status unknown.


## Configuration with the builder pattern

In the following example a monitoring is created with the builder pattern.
It will be checked that:
- the index has mor than 3000 documents
- the brand foo has more than 130 documents
- the age of the index is younger than 36 hours

```java 
    public MonitoringResponse monitoringDetails() throws PipelineContainerException, PipelineContainerDebugException {
        List<MonitoringDocument> monitoringDocumentList = MonitoringBuilder.aMonitoring().
                totalHits(2000L, 3000L).
                facetValue("brand", "foo", 120L, 130L).
                processingTime(48, 36).
                build();

        ElasticMonitoringExecuter elasticMonitoringExecuter = new ElasticMonitoringExecuter(baseUrl, monitoringDocumentList);
        return elasticMonitoringExecuter.doMonitoring();
    }
```

## Custom checks
To define custom checks the MonitoringDocumentBuilder.java can be used.

## Health Check

For the health check the monitoring response can be checked for the status.

```java 
monitoringDetails().getStatus().equals("OK");
```
