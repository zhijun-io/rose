package io.zhijun.multitenancy.observability;

/**
 * The cardinality of a multitenancy identifier key value in observations.
 *
 * <ul>
 * <li>{@link #HIGH} — the multitenancy identifier appears only in traces.
 * <li>{@link #LOW} — the multitenancy identifier appears in both metrics and traces.
 * </ul>
 */
public enum Cardinality {

    /**
     * High-cardinality: the multitenancy identifier is added as a high-cardinality key value,
     * appearing only in traces.
     */
    HIGH,

    /**
     * Low-cardinality: the multitenancy identifier is added as a low-cardinality key value,
     * appearing in both metrics and traces.
     */
    LOW

}
