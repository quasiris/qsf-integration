package com.quasiris.qsf.response;

import com.quasiris.qsf.dto.response.Document;
import com.quasiris.qsf.dto.response.MonitoringResponse;
import com.quasiris.qsf.dto.response.SearchResult;
import com.quasiris.qsf.dto.response.SimpleSearchResponse;
import com.quasiris.qsf.monitoring.MonitoringDocument;
import com.quasiris.qsf.monitoring.MonitoringStatus;

import java.util.Date;

public class MonitoringResponseFactory {
    /**
     * Create a new monitoring response from the search result and the status.
     *
     * @param searchResult the search result.
     * @param status the status.
     * @return MonitoringResponse
     */
    public static MonitoringResponse create(SearchResult searchResult, String status) {
        MonitoringResponse monitoringResponse = new MonitoringResponse();
        monitoringResponse.setStatus(status);

        monitoringResponse.setCurrentTime(new Date());
        monitoringResponse.setStatusCode(searchResult.getStatusCode());
        monitoringResponse.setTime(searchResult.getTime());

        SimpleSearchResponse monitoringResult = new SimpleSearchResponse();

        if(searchResult.getDocuments() == null) {
            return monitoringResponse;
        }
        for(Document document : searchResult.getDocuments()) {
            monitoringResult.add(document.getDocument());
        }

        monitoringResponse.setResult(monitoringResult);
        return monitoringResponse;
    }

    public static MonitoringResponse create(MonitoringDocument monitoringDocument, long time) {
        MonitoringResponse monitoringResponse = new MonitoringResponse();
        monitoringResponse.setStatus(monitoringDocument.getStatus());

        monitoringResponse.setCurrentTime(new Date());
        monitoringResponse.setStatusCode(200);
        monitoringResponse.setTime(time);

        SimpleSearchResponse monitoringResult = new SimpleSearchResponse();
        monitoringResult.add(monitoringDocument.getDocument());
        monitoringResponse.setResult(monitoringResult);
        return monitoringResponse;
    }

    public static MonitoringResponse merge(MonitoringResponse left, MonitoringResponse right) {
        if(left == null) {
            left = right;
            return left;
        }

        left.getResult().addAll(right.getResult());
        String newStatus = MonitoringStatus.computeStatus(left.getStatus(), right.getStatus());
        left.setStatus(newStatus);
        left.setTime(left.getTime() + right.getTime());
        return left;
    }

    public static MonitoringResponse merge(MonitoringResponse... monitoringResponses) {
        MonitoringResponse ret = null;

        for(MonitoringResponse monitoringResponse : monitoringResponses) {
            if(ret == null) {
                ret = monitoringResponse;
                continue;
            }
            ret = merge(ret, monitoringResponse);
        }

        return ret;
    }
}
