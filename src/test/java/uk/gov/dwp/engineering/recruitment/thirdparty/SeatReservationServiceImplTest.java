package uk.gov.dwp.engineering.recruitment.thirdparty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(SeatReservationService.class)
class SeatReservationServiceImplTest {

  @Autowired
  MockRestServiceServer server;

  @Autowired
  SeatReservationService seatReservationService;

  @Test
  void givenReservationRequest_thenOkIsReturned() {
    final ResponseEntity<String> responseEntity = seatReservationService.reserveSeats(1L, 3L);
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }
}
