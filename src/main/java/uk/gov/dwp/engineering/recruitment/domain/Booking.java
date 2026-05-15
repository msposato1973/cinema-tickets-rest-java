package uk.gov.dwp.engineering.recruitment.domain;

public record Booking(Long accountId, TicketRequest... ticketRequests) {

}
