package com.voyagia.backend.exception;


/**
 * 잘못된 사용자 데이터로 인해 발생하는 예외(Exception caused by invalid data)
 * <p>
 * 주로 다음과 같은 경우에 발생:
 * - invalid Password Format
 * - missing Required fields
 * - invalid data format
 * <p>
 * TODO: Will be mapped at @ControllerAdvice with HTTP 400 Bad Request
 */
public class InvalidUserDataException extends RuntimeException {

    private final String field; // Field that caused error
    private final Object invalidValue; // Invalid value

    public InvalidUserDataException(String field, Object invalidValue) {
        super("Invalid data for field '" + field + "': " + invalidValue);
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public InvalidUserDataException(String message) {
        super(message);
        this.field = null;
        this.invalidValue = null;
    }

    public InvalidUserDataException(String message, Throwable cause) {
        super(message, cause);
        this.field = null;
        this.invalidValue = null;
    }

    public String getField() {
        return field;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }
}
