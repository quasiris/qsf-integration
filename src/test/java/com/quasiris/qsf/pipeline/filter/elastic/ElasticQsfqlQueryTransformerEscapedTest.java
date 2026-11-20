package com.quasiris.qsf.pipeline.filter.elastic;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParserTestUtil;
import com.quasiris.qsf.query.SearchQuery;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The escaped query string profile relies on the {@code .escaped} variable suffix, which is only
 * provided by the deprecated parameter loading. Therefore this test does not belong to
 * {@link ElasticQsfqlQueryTransformerTest}, whose subclass reruns every test with a ParameterMapper.
 */
public class ElasticQsfqlQueryTransformerEscapedTest {

    @DisplayName("Transform a query with lucene reserved characters with the escaped query string profile")
    @ParameterizedTest(name = "{index} => q=''{0}'' escaped=''{1}''")
    @CsvSource({
            "'/', '\\/'",
            "'foo:bar', 'foo\\:bar'",
            "'foo (bar', 'foo \\(bar'"
    })
    public void transformQueryStringEscaped(String q, String escaped) throws Exception {
        ElasticQsfqlQueryTransformer transformer = new ElasticQsfqlQueryTransformer();
        transformer.setProfile(Profiles.queryStringEscaped());

        SearchQuery searchQuery = QsfqlParserTestUtil.createQuery("q=" + q);
        PipelineContainer pipelineContainer = new PipelineContainer(null, null);
        pipelineContainer.setSearchQuery(searchQuery);
        transformer.transform(pipelineContainer);

        ObjectNode elasticQuery = transformer.getElasticQuery();
        assertEquals(escaped, elasticQuery.at("/query/bool/must/0/query_string/query").asText());
    }
}
