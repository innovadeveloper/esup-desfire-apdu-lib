package org.esupportail.desfire.exceptions;

/**
 * Exception for DESFire APDU operations
 */
public class DesfireException extends RuntimeException {
    
    public DesfireException(String message) {
        super(message);
    }
    
    public DesfireException(String message, Throwable cause) {
        super(message, cause);
    }
}