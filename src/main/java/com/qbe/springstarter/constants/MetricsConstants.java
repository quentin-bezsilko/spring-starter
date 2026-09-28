package com.qbe.springstarter.constants;

public final class MetricsConstants {

    private MetricsConstants() {}

    // Operations
    public static final String CREATE = "create";
    public static final String FIND_BY_ID = "find_by_id";
    public static final String FIND_ALL = "find_all";
    public static final String UPDATE = "update";
    public static final String DELETE = "delete";

    // Metric names
    public static final String OPERATIONS_TOTAL = "sample_operations_total";
    public static final String OPERATION_DURATION = "sample_operation_duration";
    public static final String TECHNICAL_ERRORS_TOTAL = "sample_technical_errors_total";

    // Tags
    public static final String OPERATION_TAG = "operation";
    public static final String STATUS_TAG = "status";

    // Descriptions
    public static final String OPERATIONS_TOTAL_DESCRIPTION = "Number of sample operations";
    public static final String OPERATION_DURATION_DESCRIPTION = "Duration of sample operations";
    public static final String TECHNICAL_ERRORS_DESCRIPTION = "Number of technical errors";
}
