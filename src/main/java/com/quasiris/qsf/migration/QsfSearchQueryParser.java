package com.quasiris.qsf.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quasiris.qsf.dto.query.SearchQueryDTO;
import com.quasiris.qsf.pipeline.filter.qsql.parser.QsfqlParser;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.SearchQueryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;

public class QsfSearchQueryParser {
    private static Logger LOG = LoggerFactory.getLogger(QsfSearchQueryParser.class);

    private boolean applyDefaults = true;

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
            SearchQueryDTO searchQueryDTO = readPost(httpServletRequest);
            SearchQuery searchQuery = parseSearchQueryDTO(searchQueryDTO, httpServletRequest);
            return searchQuery;
        } catch (Exception e) {
            throw new RuntimeException("Could not read convert search query, because: " + e.getMessage(), e);
        }
    }

    public SearchQueryDTO readPost(HttpServletRequest httpServletRequest) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SearchQueryDTO searchQueryDTO = objectMapper.readValue(httpServletRequest.
                getInputStream(), SearchQueryDTO.class);
        return searchQueryDTO;
    }

    public SearchQuery parseSearchQueryDTO(SearchQueryDTO searchQueryDTO, HttpServletRequest httpServletRequest) {
        SearchQueryMapper mapper = new SearchQueryMapper();
        SearchQuery searchQuery = mapper.map(searchQueryDTO);

        if(applyDefaults) {
            mapper.applyDefaults(searchQuery);
        }

        if("true".equals(httpServletRequest.getParameter("debug"))) {
            searchQuery.setDebug(true);
        }
        if("true".equals(httpServletRequest.getParameter("explain"))) {
            searchQuery.setExplain(true);
        }
        Set<String> ctrl = QsfqlParser.parseCtrlFromString(httpServletRequest.getParameter("ctrl"));
        if(ctrl != null && ctrl.size() > 0) {
            searchQuery.setCtrl(ctrl);
        }
        return searchQuery;
    }

    protected SearchQuery handleGETRequest(HttpServletRequest httpServletRequest) {
        QsfqlParser qsfqlParser = new QsfqlParser(httpServletRequest);
        SearchQuery searchQuery = qsfqlParser.getQuery();
        SearchQueryMapper mapper = new SearchQueryMapper();
        if(applyDefaults) {
            mapper.applyDefaults(searchQuery);
        }
        return searchQuery;
    }

    public boolean isApplyDefaults() {
        return applyDefaults;
    }

    public void setApplyDefaults(boolean applyDefaults) {
        this.applyDefaults = applyDefaults;
    }
}
