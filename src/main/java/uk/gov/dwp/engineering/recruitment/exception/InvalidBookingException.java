package uk.gov.dwp.engineering.recruitment.exception;

public class InvalidBookingException extends RuntimeException {

  public InvalidBookingException(String message) {
    super(message);
  }
}
