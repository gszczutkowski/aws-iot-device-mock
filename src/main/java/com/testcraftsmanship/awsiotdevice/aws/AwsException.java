package com.testcraftsmanship.awsiotdevice.aws;

/**
 * Thrown when something wrong occurs during working with AWS API.
 */
public class AwsException extends RuntimeException {

    /**
     * Creates AWS related exception.
     *
     * @param message message
     * @param cause   cause
     */
    public AwsException(String message, Exception cause) {
        super(message, cause);
    }
}
