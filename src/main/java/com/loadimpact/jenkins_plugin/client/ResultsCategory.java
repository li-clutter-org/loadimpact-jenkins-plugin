package com.loadimpact.jenkins_plugin.client;

/**
 * Enumerates all possible test-result categories.
 *
 * @author jens
 * @date 2013-09-11, 11:18
 */
public enum ResultsCategory {
    accumulated_load_time(false), 
    bandwidth("avg"), 
    clients_active, 
    connections_active,
    failure_rate("avg"),
    loadgen_cpu_utilization(false), 
    loadgen_memory_utilization(false), 
    progress_percent_total(false),
    requests_per_second("avg"),
    total_rx_bytes, 
    total_requests, 
    user_load_time(false)
    ;

    /**
     * If true for int, else float.
     */
    public final boolean valueTypeAsInt;

    /**
     * Name of value-field(in json response), such as 'value' or 'avg'
     */
    public final String valueName;

    /**
     * REST query parameter name.
     */
    public final String param;

    
    /**
     * <ul>
     *     <li>fieldName = 'value'</li>
     *     <li>type = int</li>
     * </ul>
     */
    private ResultsCategory() {
        this(true, "value");
    }

    /**
     * <ul>
     *     <li>fieldName = 'value'</li>
     *     <li>type = int | float</li>
     * </ul>
     * @param valueTypeAsInt  true if 'int', else 'float'
     */
    private ResultsCategory(boolean valueTypeAsInt) {
        this(valueTypeAsInt, "value");
    }

    /**
     * <ul>
     *     <li>fieldName = *</li>
     *     <li>type = float</li>
     * </ul>
     * @param valueName    name of value-field 
     */
    private ResultsCategory(String valueName) {
        this(false, valueName);
    }

    /**
     * <ul>
     *     <li>fieldName = *</li>
     *     <li>type = *</li>
     * </ul>
     * @param valueTypeAsInt  true if 'int', else 'float'
     * @param valueName    name of value-field 
     */
    private ResultsCategory(boolean valueTypeAsInt, String valueName) {
        this.param          = "__li_" + name();
        this.valueTypeAsInt = valueTypeAsInt;
        this.valueName      = valueName;
    }

}
