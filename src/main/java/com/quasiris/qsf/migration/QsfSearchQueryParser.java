package com.quasiris.qsf.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.dto.query.SearchQueryDTO;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParser;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.SearchQueryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

public class QsfSearchQueryParser {
    private static Logger LOG = LoggerFactory.getLogger(QsfSearchQueryParser.class);

    public SearchQuery parseSearchQuery(HttpServletRequest httpServletRequest) {
        SearchQuery searchQuery;
        if("POST".equals(httpServletRequest.getMethod())) {
            searchQuery = handlePOSTRequest(httpServletRequest);
        } else {
            searchQuery = handleGETRequest(httpServletRequest);
        }
        return searchQuery;
    }


    protected SearchQuery handlePOSTRequest(HttpServletRequest httpServletRequest) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            SearchQueryDTO searchQueryDTO = objectMapper.readValue(httpServletRequest.getInputStream(), SearchQueryDTO.class);
            SearchQueryMapper mapper = new SearchQueryMapper();
            SearchQuery searchQuery = mapper.map(searchQueryDTO);
            if("true".equals(httpServletRequest.getParameter("debug"))) {
                searchQuery.setDebug(true);
            }
            Set<String> ctrl = QsfqlParser.parseCtrlFromString(httpServletRequest.getParameter("ctrl"));
            if(ctrl != null && ctrl.size() > 0) {
                searchQuery.setCtrl(ctrl);
            }
            return searchQuery;
        } catch (Exception e) {
            throw new RuntimeException("Could not read convert search query, because: " + e.getMessage(), e);
        }
    }

    protected SearchQuery handleGETRequest(HttpServletRequest httpServletRequest) {
        QsfqlParser qsfqlParser = new QsfqlParser(httpServletRequest);
        SearchQuery searchQuery = qsfqlParser.getQuery();
        return searchQuery;
    }
}
