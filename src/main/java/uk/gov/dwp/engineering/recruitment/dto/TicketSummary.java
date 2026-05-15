package uk.gov.dwp.engineering.recruitment.dto;

public record TicketSummary(
        int totalTickets,
        int adultCount,
        int childCount,
        int infantCount,
        int totalAmount,
        int totalSeats
) {}

