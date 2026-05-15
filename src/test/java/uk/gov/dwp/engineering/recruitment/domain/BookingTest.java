package uk.gov.dwp.engineering.recruitment.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static uk.gov.dwp.engineering.recruitment.domain.TicketType.ADULT;

import java.util.List;
import org.junit.jupiter.api.Test;

class BookingTest {

  @Test
  void givenAdultTicketBooking_whenGetAccountAndTicketRequest_thenExpectedValuesReturned() {
    final TicketRequest ticketRequest = new TicketRequest(ADULT, 1);
    final Booking booking = new Booking(123L, ticketRequest);

    assertEquals(123L, booking.accountId());

    final List<TicketRequest> ticketRequests = List.of(booking.ticketRequests());
    assertNotNull(ticketRequests);
    assertEquals(1, ticketRequests.size());

    final TicketRequest actualTicketRequest = ticketRequests.getFirst();
    assertEquals(ticketRequest, actualTicketRequest);
  }

}
