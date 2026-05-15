package uk.gov.dwp.engineering.recruitment.thirdparty;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;

@RestClientTest(PaymentService.class)
class PaymentServiceImplTest {

  @Autowired
  MockRestServiceServer server;

  @Autowired
  private PaymentService paymentService;

  @Test
  void givenPaymentRequest_thenOkIsReturned() {
    final ResponseEntity<String> responseEntity = paymentService.debitAccount(1L,
        BigDecimal.valueOf(1.00));
    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }
}
