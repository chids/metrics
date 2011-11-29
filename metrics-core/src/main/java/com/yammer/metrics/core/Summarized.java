package com.yammer.metrics.core;

public interface Summarized {

    /**
     * Returns the largest recorded value.
     *
     * @return the largest recorded value
     */
    public abstract double max();

    /**
     * Returns the smallest recorded value.
     *
     * @return the smallest recorded value
     */
    public abstract double min();

    /**
     * Returns the arithmetic mean of all recorded values.
     *
     * @return the arithmetic mean of all recorded values
     */
    public abstract double mean();

    /**
     * Returns the standard deviation of all recorded values.
     *
     * @return the standard deviation of all recorded values
     */
    public abstract double stdDev();

}