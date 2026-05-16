package uk.gov.dwp.engineering.recruitment.utils;

public class Constant {


	public static final int MAX_TICKETS_PER_REQUEST = 25;
    public static final int ADULT_PRICE = 25;
    public static final int CHILD_PRICE = 15;
    public static final int INFANT_PRICE = 0;


    public static final String MAX_TICKETS_ERROR = "Total ticket count exceeds the maximum allowed: ";


    public static final String MESSAGE_FORMATTING = "Successfully purchased %d tickets. Total amount: £%d. Seats reserved: %d. Account ID: %d";
    public static final String MESSEGE_MINTICKET_ERR = "Must purchase at least one ticket.";
    public static final String MESSEGE_TICKET_PROVIDED_ERR = "At least one ticket request must be provided.";
    public static final String MSG_TICKET_REQUEST_ERR = "Ticket request cannot be null.";
    public static final String MSG_INFANT_WITHOUT_ADULT_ERR = "Infant tickets cannot be purchased without an adult ticket";
    public static final String MSG_CHILD_WITHOUT_ADULT_ERR = "Child tickets cannot be purchased without an Adult ticket.";

    
    

}
