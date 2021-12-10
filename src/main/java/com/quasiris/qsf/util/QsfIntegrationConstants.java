package com.quasiris.qsf.util;

/**
 * Qsf Integration specific constants
 */
public class QsfIntegrationConstants {
    /**
     * Internal aggregation with is used for variants or collapsed results to determine the real document total count
     */
    public final static String TOTAL_COUNT_AGGREGATION_NAME = "_total_count";

    /**
     * For variants a special sub aggregation is appended to each aggregation which display the real aggregation count
     */
    public final static String VARIANT_COUNT_SUB_AGGREGATION_NAME = "variant_count";
}
