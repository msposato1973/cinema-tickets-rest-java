package uk.gov.dwp.engineering.recruitment;

import org.springframework.stereotype.Service;
import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;

import uk.gov.dwp.engineering.recruitment.dto.TicketSummary;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;

import uk.gov.dwp.engineering.recruitment.utils.Constant;

import java.math.BigDecimal;

@Service
public class CinemaTicketsServiceImpl implements CinemaTicketsService {

    private final PaymentService paymentService;
    private final SeatReservationService seatReservationService;

    public CinemaTicketsServiceImpl(PaymentService paymentService,
                                    SeatReservationService seatReservationService
    ) {
        this.paymentService = paymentService;
        this.seatReservationService = seatReservationService;
    }

    @Override
    public String purchaseTickets(Long accountId, TicketRequest... ticketRequests) throws InvalidBookingException {

        // Validate account ID
        validateAccountId(accountId);

        // Validate ticket requests
        validateTicketRequests(ticketRequests);

        // Calculate totals
        TicketSummary summary = calculateTicketSummary(ticketRequests);

        // Check total ticket limit (including infants)
        //if (summary.totalTickets() > Constant.MAX_TICKETS_PER_REQUEST) {
        //    throw new InvalidBookingException(Constant.MAX_TICKETS_ERROR + Constant.MAX_TICKETS_PER_REQUEST);
        //}

        // Validate business rules
        validateBusinessRules(summary);

        BigDecimal totalAmount = new BigDecimal(summary.totalAmount());
        paymentService.debitAccount(accountId, totalAmount);

        Long seatCount = Long.valueOf(summary.totalSeats());

        seatReservationService.reserveSeats(accountId, seatCount);

        // Return success message
        return String.format(Constant.MESSAGE_FORMATTING,
                summary.totalTickets(), summary.totalAmount(), summary.totalSeats(), accountId);

    }

    private void validateBusinessRules(TicketSummary summary) {

        // Rule: Minimum one ticket
        if (summary.totalTickets() == 0) throw new InvalidBookingException(Constant.MESSEGE_MINTICKET_ERR);

        // Rule: Maximum 25 tickets
        if (summary.totalTickets() > Constant.MAX_TICKETS_PER_REQUEST)
            throw new InvalidBookingException("Cannot purchase more than " + Constant.MAX_TICKETS_PER_REQUEST +
                    " tickets. Requested: " + summary.totalTickets()
            );

        // Rule: Child tickets require at least one Adult ticket
        if (summary.childCount() > 0 && summary.adultCount() == 0)
            throw new InvalidBookingException(Constant.MSG_CHILD_WITHOUT_ADULT_ERR);

        // Rule: Infant tickets require at least one Adult ticket
        if (summary.infantCount() > 0 && summary.adultCount() == 0)
            throw new InvalidBookingException(Constant.MSG_INFANT_WITHOUT_ADULT_ERR);

        // Rule: Infants cannot exceed number of adults (each infant needs an adult lap)
        if (summary.infantCount() > summary.adultCount()) {
            throw new InvalidBookingException("Number of infants (" + summary.infantCount() +
                    ") cannot exceed number of adults (" + summary.adultCount() +
                    "). Each infant requires an adult lap.");
        }

    }

    private void validateAccountId(final Long accountId) throws InvalidBookingException {
        if (accountId == null || accountId <= 0) {
            throw new InvalidBookingException("Invalid account ID: " + accountId +
                    ". Account ID must be greater than 0.");
        }
    }

    private void validateTicketRequests(final TicketRequest... ticketRequests)
            throws InvalidBookingException {

        if (ticketRequests == null || ticketRequests.length == 0) {
            throw new InvalidBookingException(Constant.MESSEGE_TICKET_PROVIDED_ERR);
        }


        for (TicketRequest request : ticketRequests) {

            if (request == null) {
                throw new InvalidBookingException(Constant.MSG_TICKET_REQUEST_ERR);
            }

            if (request.ticketCount() < 0) {
                throw new InvalidBookingException("Ticket count cannot be negative: " + request.ticketCount());
            }
        }

    }

    private TicketSummary calculateTicketSummary(TicketRequest[] ticketRequests) {

        int totalTickets = 0;
        int adultCount = 0;
        int childCount = 0;
        int infantCount = 0;
        int totalAmount = 0;
        int totalSeats = 0;


        for (TicketRequest request : ticketRequests) {
            int quantity = request.ticketCount();
            totalTickets += quantity;

            switch (request.type()) {
                case ADULT:
                    adultCount += quantity;
                    totalAmount += quantity * Constant.ADULT_PRICE;
                    totalSeats += quantity;
                    break;
                case CHILD:
                    childCount += quantity;
                    totalAmount += quantity * Constant.CHILD_PRICE;
                    totalSeats += quantity;
                    break;
                case INFANT:
                    infantCount += quantity;
                    totalAmount += quantity * Constant.INFANT_PRICE;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown ticket type: " + request.type());
            }
        }

        return new TicketSummary(totalTickets, adultCount, childCount, infantCount,
                totalAmount, totalSeats);
    }

}
