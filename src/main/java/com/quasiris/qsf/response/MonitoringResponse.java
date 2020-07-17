package com.quasiris.qsf.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.quasiris.qsf.monitoring.MonitoringDocument;
import com.quasiris.qsf.monitoring.MonitoringStatus;

import java.util.Date;

/**
 * Created by tbl on 29.05.19.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitoringResponse {


    private Integer statusCode;
    private SimpleSearchResponse result;

    private long time;

    private Date currentTime;

    private String status;


    /**
     * Getter for property 'statusCode'.
     *
     * @return Value for property 'statusCode'.
     */
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Setter for property 'statusCode'.
     *
     * @param statusCode Value to set for property 'statusCode'.
     */
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }


    /**
     * Getter for property 'result'.
     *
     * @return Value for property 'result'.
     */
    public SimpleSearchResponse getResult() {
        return result;
    }

    /**
     * Setter for property 'result'.
     *
     * @param result Value to set for property 'result'.
     */
    public void setResult(SimpleSearchResponse result) {
        this.result = result;
    }

    /**
     * Getter for property 'time'.
     *
     * @return Value for property 'time'.
     */
    public long getTime() {
        return time;
    }

    /**
     * Setter for property 'time'.
     *
     * @param time Value to set for property 'time'.
     */
    public void setTime(long time) {
        this.time = time;
    }

    /**
     * Getter for property 'currentTime'.
     *
     * @return Value for property 'currentTime'.
     */
    public Date getCurrentTime() {
        return currentTime;
    }

    /**
     * Setter for property 'currentTime'.
     *
     * @param currentTime Value to set for property 'currentTime'.
     */
    public void setCurrentTime(Date currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * Getter for property 'status'.
     *
     * @return Value for property 'status'.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter for property 'status'.
     *
     * @param status Value to set for property 'status'.
     */
    public void setStatus(String status) {
        this.status = status;
    }

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
}
