package uk.gov.dwp.engineering.recruitment.thirdparty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SeatReservationServiceImpl implements SeatReservationService {

  private final RestClient restClient;
  @Value("${reservation-service.url}")
  private String reservationServiceUrl;

  public SeatReservationServiceImpl(final RestClient.Builder builder) {
    this.restClient = builder.baseUrl(reservationServiceUrl).build();
  }

  @Override
  public ResponseEntity<String> reserveSeats(final Long accountId, final Long seatCount) {
    // Real implementation omitted for initial iteration(s).
    return new ResponseEntity<>("TODO", HttpStatus.OK);
  }
}
