package uk.gov.dwp.engineering.recruitment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.CHILD;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.INFANT;

import org.junit.jupiter.api.Test;

class TicketRequestTest {

  @Test
  void givenValidInfantTicketRequest_whenGetTypeAndTicketCount_thenExpectedValuesReturned() {
    final TicketRequest ticketRequest = new TicketRequest(INFANT, 0);
    assertEquals(0, ticketRequest.ticketCount());
    assertEquals(INFANT, ticketRequest.type());
  }

  @Test
  void givenValidChildTicketRequest_whenGetTypeAndTicketCount_thenExpectedValuesReturned() {
    final TicketRequest ticketRequest = new TicketRequest(CHILD, 1);
    assertEquals(1, ticketRequest.ticketCount());
    assertEquals(CHILD, ticketRequest.type());
  }

  @Test
  void givenValidAdultTicketRequest_whenGetTypeAndTicketCount_thenExpectedValuesReturned() {
    final TicketRequest ticketRequest = new TicketRequest(ADULT, 2);
    assertEquals(2, ticketRequest.ticketCount());
    assertEquals(ADULT, ticketRequest.type());
  }
}
