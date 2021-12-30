package com.quasiris.qsf.pipeline.filter.tracking;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParserTestUtil;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.test.AbstractPipelineTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by tbl on 6.02.21.
 */
public class TrackingFilterTest extends AbstractPipelineTest {


        @Test
        public void testEmptyQuery() throws Exception {
            TrackingFilter trackingFilter = new TrackingFilter();
            PipelineContainer pipelineContainer = new PipelineContainer();
            Document tracking = trackingFilter.getTracking(pipelineContainer);
            assertNotNull(tracking.getFieldValue("id"));
        }
        @Test
        public void testRangeQuery() throws Exception {
            SearchQuery searchQuery = QsfqlParserTestUtil.createQuery("f.price.range=3,5");
            PipelineContainer pipelineContainer = new PipelineContainer();
            pipelineContainer.setSearchQuery(searchQuery);

            TrackingFilter trackingFilter = new TrackingFilter();
            Document tracking = trackingFilter.getTracking(pipelineContainer);
            assertEquals("price=3.0,5.0", tracking.getFieldValue("filterValue"));
        }

}
