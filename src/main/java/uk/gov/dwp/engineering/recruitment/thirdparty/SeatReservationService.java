package uk.gov.dwp.engineering.recruitment.thirdparty;

import org.springframework.http.ResponseEntity;

public interface SeatReservationService {

  ResponseEntity<String> reserveSeats(final Long accountId, final Long seatCount);
}
