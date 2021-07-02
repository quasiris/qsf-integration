package com.quasiris.qsf.pipeline.filter.tracking;

import com.quasiris.qsf.pipeline.PipelineContainer;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParserTest;
import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.test.AbstractPipelineTest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

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
            SearchQuery searchQuery = QsfqlParserTest.createQuery("f.price.range=3,5");
            PipelineContainer pipelineContainer = new PipelineContainer();
            pipelineContainer.setSearchQuery(searchQuery);

            TrackingFilter trackingFilter = new TrackingFilter();
            Document tracking = trackingFilter.getTracking(pipelineContainer);
            assertEquals("price=3.0,5.0", tracking.getFieldValue("filterValue"));
        }

}
