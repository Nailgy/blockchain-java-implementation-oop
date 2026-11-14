package com.nali.blockchain.exception;

/**
 * Thrown when the chain validation finds a problem:
 * a broken link between blocks, an invalid proof-of-work or an invalid transaction.
 */
public class InvalidChainException extends BlockchainException {

    public InvalidChainException(String message) {
        super(message);
    }

    public InvalidChainException(String message, Throwable cause) {
        super(message, cause);
    }
}
