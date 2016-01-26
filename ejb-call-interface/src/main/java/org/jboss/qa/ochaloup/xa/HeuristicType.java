package org.jboss.qa.ochaloup.xa;

/**
 * This enum is used to specify the type of HEURISTIC exception to throw when creating test XA datasource.
 *
 * @author Hayk Hovsepyan <hhovsepy@redhat.com>
 */
public enum HeuristicType {
    ROLLBACK, COMMIT, MIXED;
}
