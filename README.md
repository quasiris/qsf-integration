# Quasiris Search Framework Integration

## offene Punkte
- soll die Facets in ein eigenes Objekt
- Logging
- Timeout für Filter
        
## Logging
- http://docs.spring.io/spring-boot/docs/current/reference/html/howto-logging.html


## Debugging

```
mvn spring-boot:run -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005"
```
        
## Circuit Breaker
https://spring.io/guides/gs/circuit-breaker/
        
## API Documentation
### Filter
- all 

- by default multiple searchFilter values for one property are connected by OR
- to change the logic: searchFilter.name.operator=AND



## Metrics
- http://metrics.dropwizard.io/3.1.0/getting-started/


## Testing
https://dzone.com/articles/junit-testing-for-solr-6

## Benchmark
http://openjdk.java.net/projects/code-tools/jmh/

## Metrics
- top n keywords
- 


## License