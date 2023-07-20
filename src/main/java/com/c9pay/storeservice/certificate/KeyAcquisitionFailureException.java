package com.c9pay.storeservice.certificate;

public class KeyAcquisitionFailureException extends RuntimeException {
    public KeyAcquisitionFailureException(String message) {
        super(message);
    }

    public KeyAcquisitionFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
