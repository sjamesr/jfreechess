package org.jdns.jfreechess.timeseal;

/**
 * Represents a source of the current time in nanoseconds.
 */
public interface Clock {
  public long nanoTime();
}
