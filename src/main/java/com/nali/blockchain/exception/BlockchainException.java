package com.nali.blockchain.exception;

/**
 * Base class for all business errors of the application.
 * It is a checked exception on purpose: the compiler forces the caller
 * to decide what to do (show a message, skip a line, ...).
 */
public class BlockchainException extends Exception {

    public BlockchainException(String message) {
        super(message);
    }

    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}
