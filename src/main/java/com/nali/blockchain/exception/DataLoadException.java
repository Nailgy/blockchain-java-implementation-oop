package com.nali.blockchain.exception;

/**
 * Thrown when a data file cannot be loaded at all (missing, unreadable, no valid data).
 * A single bad line does NOT throw this exception: it is skipped with a warning.
 */
public class DataLoadException extends BlockchainException {

    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
