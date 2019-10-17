package com.quasiris.qsf.monitoring;

public class MonitoringStatus {

    public static final String ERROR = "ERROR";
    public static final String WARN = "WARN";
    public static final String OK = "OK";

    public static String computeStatus(String currentStatus, String newStatus) {
        if(currentStatus == null) {
            return newStatus;
        }
        if(newStatus == null) {
            return currentStatus;
        }

        if(currentStatus.equals("ERROR")) {
            return currentStatus;
        }
        if(currentStatus.equals("WARN") && newStatus.equals("ERROR")) {
            return newStatus;
        }
        if(currentStatus.equals("OK") && !newStatus.equals("OK")) {
            return newStatus;
        }
        return currentStatus;
    }
}
