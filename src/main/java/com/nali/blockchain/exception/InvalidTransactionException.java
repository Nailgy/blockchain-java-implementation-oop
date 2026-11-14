package com.nali.blockchain.exception;

/**
 * Thrown when a transaction breaks a business rule
 * (non-positive amount, unknown wallet, sending to yourself, ...).
 */
public class InvalidTransactionException extends BlockchainException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
