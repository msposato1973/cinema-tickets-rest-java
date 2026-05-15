package uk.gov.dwp.engineering.recruitment.thirdparty;

import java.math.BigDecimal;
import org.springframework.http.ResponseEntity;

public interface PaymentService {

  ResponseEntity<String> debitAccount(final Long accountId, final BigDecimal amount);
}
