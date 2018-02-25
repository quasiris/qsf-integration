package com.quasiris.qsf.pipeline.filter.elastic;

import com.quasiris.qsf.mock.Mockfactory;
import com.quasiris.qsf.pipeline.*;
import com.quasiris.qsf.pipeline.filter.qsql.QSQLRequestFilter;
import com.quasiris.qsf.response.Document;
import com.quasiris.qsf.response.Facet;
import com.quasiris.qsf.response.FacetValue;
import com.quasiris.qsf.response.SearchResult;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by mki on 11.02.18.
 */
public class QsfqlElasticFilterTest extends AbstractPipelineTest {

        @Test
        @Ignore
        public void debug() throws Exception {
                try {
                        testQsfqlElasticFilterVersion1();
                } catch (PipelineContainerException e) {
                        System.out.println(e.getMessage());
                } catch (PipelineContainerDebugException debug) {
                        System.out.println(debug.getDebugStack());
                }
        }

        @Test
        public void testQsfqlElasticFilterVersion1() throws Exception {
                testQsfqlElasticFilter(1, "location-v1.json", "http://localhost:9214/osm");
        }

        @Test
        public void testQsfqlElasticFilterVersion6() throws Exception {
                testQsfqlElasticFilter(6, "location.json", "http://localhost:9262/osm");
        }

        public void testQsfqlElasticFilter(int version, String profile, String baseUrl) throws Exception {

            MockElasticClient mockElasticClient = new MockElasticClient();
            //mockElasticClient.setRecord(true);


            Pipeline pipeline = PipelineBuilder.create().
                    pipeline("products").
                    timeout(100000000L).
                    filter(new QSQLRequestFilter()).
                    filter(ElasticFilterBuilder.create().
                            elasticVersion(version).
                            client(mockElasticClient).
                            baseUrl(baseUrl).
                            profile("classpath://com/quasiris/qsf/elastic/profiles/" + profile ).
                            addAggregation("places", "place").
                            mapAggregationName("places", "Places").
                            addAggregation("tag", "tagkey_is_in").
                            mapFilter("place", "tagkey_place").
                            mapField("id","id").
                            mapAggregation("tag", "tag").
                            mapAggregationName("tag", "Tag").
                            defaultSort("nameAZ").
                            mapSort("nameAZ", "[{ \"name\" : \"desc\" }]").
                            mapSort("nameZA", "[{ \"name\" : \"asc\" }]").
                            resultSetId("products").
                            build()).
                    build();

            Assert.assertNotNull(pipeline.print(""));

            HttpServletRequest httpServletRequest = Mockfactory.createHttpServletRequest("http://localhost?q=darmstadt&foo=bar&f.place=city&page=1");

            PipelineContainer pipelineContainer = PipelineExecuter.create().
                    pipeline(pipeline).
                    httpRequest(httpServletRequest).
                    execute();

            if(!pipelineContainer.isSuccess()) {
                Assert.fail();
            }

            SearchResult searchResult = pipelineContainer.getSearchResult("products");
            Assert.assertEquals(Long.valueOf(1), searchResult.getTotal());
            Assert.assertEquals(1,searchResult.getDocuments().size());

            Document document = searchResult.getDocuments().get(0);
            Assert.assertEquals(31, document.getFieldCount());


            Facet facet = searchResult.getFacetById("places");
            //Assert.assertEquals("Places", facet.getName());
            Assert.assertEquals("places", facet.getId());
            Assert.assertEquals(Long.valueOf(1), facet.getCount());
            Assert.assertEquals(Long.valueOf(1), facet.getResultCount());

            FacetValue facetValue = facet.getValues().get(0);
            Assert.assertEquals("city", facetValue.getValue());
            Assert.assertEquals(Long.valueOf(1), facetValue.getCount());
            Assert.assertEquals("places=city", facetValue.getFilter());

        }


}
