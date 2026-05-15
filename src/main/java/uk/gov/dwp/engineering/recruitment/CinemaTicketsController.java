package uk.gov.dwp.engineering.recruitment;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cinema")
public class CinemaTicketsController {

  /**
   * The Ticket Booking Service.
   */
  private final CinemaTicketsService cinemaTicketsService;

  public CinemaTicketsController(final CinemaTicketsService cinemaTicketsService) {
    this.cinemaTicketsService = cinemaTicketsService;
  }

}
