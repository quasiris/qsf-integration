package com.quasiris.qsf.pipeline.filter.qsql.parser;

import com.quasiris.qsf.commons.util.EncodingUtil;
import com.quasiris.qsf.pipeline.filter.web.QSFHttpServletRequest;
import com.quasiris.qsf.query.SearchQuery;
import com.quasiris.qsf.query.SearchQueryMapper;

/**
 * Created by mki on 13.11.16.
 */
public class QsfqlParserTestUtil {

    public static SearchQuery createQuery(String... parameters) throws Exception {
        StringBuilder urlBuilder = new StringBuilder();
        for(String param: parameters) {
            String[] paramSplitted = param.split("=");
            addUrlParameter(urlBuilder, paramSplitted[0],paramSplitted[1]);
        }
        QSFHttpServletRequest request = new QSFHttpServletRequest(urlBuilder.toString());
        QsfqlParser qsfqlParser = new QsfqlParser(request);
        SearchQuery query = qsfqlParser.getQuery();
        SearchQueryMapper mapper = new SearchQueryMapper();
        mapper.applyDefaults(query);
        return query;
    }

    public static void addUrlParameter(StringBuilder url, String name, String value) {
        if(url.length() == 0) {
            url.append("?");
        } else {
            url.append("&");
        }
        String encodedValue = EncodingUtil.encode(value);
        url.append(name).append("=").append(encodedValue);
    }

}
