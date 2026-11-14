package com.nali.blockchain.ui;

/**
 * Thrown by the console menu when the user types something it cannot use
 * (not a number, a block index that does not exist) or when there is no more input.
 * It is separate from BlockchainException because it is about the user interface,
 * not about the business rules.
 */
public class InvalidInputException extends Exception {

    public InvalidInputException(String message) {
        super(message);
    }
}
