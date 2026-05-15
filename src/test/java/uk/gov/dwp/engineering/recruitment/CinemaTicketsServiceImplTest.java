package uk.gov.dwp.engineering.recruitment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import uk.gov.dwp.engineering.recruitment.domain.TicketRequest;
import uk.gov.dwp.engineering.recruitment.domain.TicketType;
import uk.gov.dwp.engineering.recruitment.dto.TicketSummary;
import uk.gov.dwp.engineering.recruitment.exception.InvalidBookingException;
import uk.gov.dwp.engineering.recruitment.thirdparty.PaymentService;
import uk.gov.dwp.engineering.recruitment.thirdparty.SeatReservationService;
import uk.gov.dwp.engineering.recruitment.utils.Constant;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class CinemaTicketsServiceImplTest {

	@Mock
	private PaymentService paymentService;

	@Mock
	private SeatReservationService seatReservationService;

	private CinemaTicketsServiceImpl cinemaTicketsService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		cinemaTicketsService = new CinemaTicketsServiceImpl(paymentService, seatReservationService);
	}

	@Test
	void testPurchaseTicketNotNull() {
		assertNotNull(paymentService);
		assertNotNull(seatReservationService);
		assertNotNull(cinemaTicketsService);
	}

	@Test
	void shouldThrowExceptionWhenAccountIdIsNull() {

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {

			cinemaTicketsService.purchaseTickets(null, new TicketRequest[] {});
		});

		assertEquals("Invalid account ID: null. Account ID must be greater than 0.", exception.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenAccountIdIsZero() {

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(0L, new TicketRequest[] {});
		});

		assertEquals("Invalid account ID: 0. Account ID must be greater than 0.", exception.getMessage());
	}

	@Test
	void shouldThrowExceptionWhenAccountIdIsNegative() {
		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {

			cinemaTicketsService.purchaseTickets(-1L, new TicketRequest[] {});
		});

		assertEquals("Invalid account ID: -1. Account ID must be greater than 0.", exception.getMessage());
	}

	@Test
	@DisplayName("Should successfully purchase adults, children and infants")
	void shouldSuccessfullyPurchaseAdultsChildrenAndInfants() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 3), new TicketRequest(TicketType.CHILD, 2),
				new TicketRequest(TicketType.INFANT, 2) };

		String result = cinemaTicketsService.purchaseTickets(100L, requests);

		verify(paymentService).debitAccount(eq(100L), eq(new BigDecimal("105")));
		verify(seatReservationService).reserveSeats(eq(100L), eq(5L));
	}

	@Test
	@DisplayName("Should successfully purchase valid combination of tickets")
	void shouldSuccessfullyPurchaseValidTickets() throws InvalidBookingException {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 2), new TicketRequest(TicketType.CHILD, 1),
				new TicketRequest(TicketType.INFANT, 1) };

		String result = cinemaTicketsService.purchaseTickets(1L, requests);
		BigDecimal amount = new BigDecimal("65");
		// Verify: 2 Adults = £50, 1 Child = £15, Infant = £0 => Total £65
		verify(paymentService).debitAccount(1L, amount);

		// Verify: 2 Adults + 1 Child = 3 seats
		verify(seatReservationService).reserveSeats(1L, 3L);

		assertTrue(result.contains("Successfully purchased 4 tickets"));
		assertTrue(result.contains("Total amount: £65"));
		assertTrue(result.contains("Seats reserved: 3"));
	}

	@Test
	@DisplayName("Should throw exception when exceeding maximum tickets (25)")
	void shouldThrowExceptionWhenExceedingMaxTickets() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 26) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, requests);
		});

		assertTrue(exception.getMessage().contains("Total ticket count exceeds the maximum allowed"));
		verify(paymentService, never()).debitAccount(anyLong(), any(BigDecimal.class));
		verify(seatReservationService, never()).reserveSeats(anyLong(), anyLong());

	}

	@Test
	@DisplayName("Should throw exception when child tickets without adult")
	void shouldThrowExceptionWhenChildWithoutAdult() {
		TicketRequest[] requests = { new TicketRequest(TicketType.CHILD, 2) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, requests);
		});

		assertTrue(exception.getMessage().contains("cannot be purchased without an Adult"));
		verifyNoInteractions(paymentService, seatReservationService);
	}

	@Test
	@DisplayName("Should throw exception when infant tickets without adult")
	void shouldThrowExceptionWhenInfantWithoutAdult() {
		TicketRequest[] requests = { new TicketRequest(TicketType.INFANT, 2) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, requests);
		});

		assertTrue(exception.getMessage().contains("Infant tickets cannot be purchased without an adult ticket"));
	}

	@Test
	@DisplayName("Should throw exception when more infants than adults")
	void shouldThrowExceptionWhenMoreInfantsThanAdults() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 1), new TicketRequest(TicketType.INFANT, 2) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, requests);
		});

		assertTrue(exception.getMessage().contains("cannot exceed number of adults"));
	}

	@Test
	@DisplayName("Should throw exception when zero tickets purchased")
	void shouldThrowExceptionWhenZeroTickets() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 0) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, requests);
		});

		assertTrue(exception.getMessage().contains("purchase at least one ticket"));
	}

	@Test
	@DisplayName("Should throw exception for negative ticket counts")
	void shouldThrowExceptionForNegativeTicketCounts() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, -1) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, requests);
		});

		assertTrue(exception.getMessage().contains("cannot be negative"));
	}

	@Test
	@DisplayName("Should throw exception for invalid account ID (null)")
	void shouldThrowExceptionForNullAccountId() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 1) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(null, requests);
		});

		assertTrue(exception.getMessage().contains("Invalid account ID"));
	}

	@Test
	@DisplayName("Should throw exception for invalid account ID (zero)")
	void shouldThrowExceptionForZeroAccountId() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 1) };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(0L, requests);
		});

		assertTrue(exception.getMessage().contains("Invalid account ID"));
	}

	@Test
	@DisplayName("Should successfully purchase exactly 25 tickets")
	void shouldPurchaseExactlyMaxTickets() throws InvalidBookingException {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 20), new TicketRequest(TicketType.CHILD, 5) };

		String result = cinemaTicketsService.purchaseTickets(1L, requests);
		BigDecimal amount = new BigDecimal((20 * 25) + (5 * 15)); // (20 * £25) + (5 * £15) = £500 + £75 = £575

		// Verify payment and seat reservation interactions
		verify(paymentService).debitAccount(1L, amount);
		verify(seatReservationService).reserveSeats(1L, 25L);

		assertTrue(result.contains("Successfully purchased 25 tickets"));
	}

	@Test
	@DisplayName("Should correctly handle adult-only purchases")
	void shouldHandleAdultOnlyPurchases() throws InvalidBookingException {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 3) };

		String result = cinemaTicketsService.purchaseTickets(1L, requests);

		verify(paymentService).debitAccount(1L, new BigDecimal(75));
		verify(seatReservationService).reserveSeats(1L, 3L);

		assertTrue(result.contains("Total amount: £75"));
		assertTrue(result.contains("Seats reserved: 3"));
	}

	@Test
	@DisplayName("Should throw exception when TicketRequest contains null element")
	void shouldThrowExceptionWhenTicketRequestElementIsNull() {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 1), null };

		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, requests);
		});

		assertTrue(exception.getMessage().contains("cannot be null"));
	}

	@Test
	@DisplayName("Should handle multiple ticket types correctly")
	void shouldHandleMultipleTicketTypesCorrectly() throws InvalidBookingException {
		TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 10), new TicketRequest(TicketType.CHILD, 5),
				new TicketRequest(TicketType.INFANT, 3) };

		String result = cinemaTicketsService.purchaseTickets(100L, requests);

		verify(seatReservationService).reserveSeats(100L, 15L);

		verify(paymentService).debitAccount(100L, new BigDecimal(325));

		assertTrue(result.contains("Successfully purchased 18 tickets"));
	}

	// ==================== TESTS FOR PRIVATE METHOD calculateTicketSummary
	// ====================
	@Test
	void testCalculateTicketSummary()
			throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, SecurityException {
		TicketRequest[] ticketRequests = { new TicketRequest(TicketType.ADULT, 2),
				new TicketRequest(TicketType.CHILD, 1), new TicketRequest(TicketType.INFANT, 1) };

		Method calculateTicketSummaryMethod = CinemaTicketsServiceImpl.class.getDeclaredMethod("calculateTicketSummary",
				TicketRequest[].class);
		calculateTicketSummaryMethod.setAccessible(true);

		TicketSummary summary = (TicketSummary) calculateTicketSummaryMethod.invoke(cinemaTicketsService,
				(Object) ticketRequests);

		assertEquals(2, summary.adultCount());
		assertEquals(1, summary.childCount());
		assertEquals(1, summary.infantCount());
		assertEquals(65, summary.totalAmount()); // 2 adults * $25 + 1 child * $15 + 1 infant * $0
		assertEquals(3, summary.totalSeats()); // 2 adults + 1 child (infant does not require a seat)
	}

	// ==================== INTEGRATION TESTS ====================
	@Test
	void shouldProcessValidTicketPurchase() {
		TicketRequest[] ticketRequests = { new TicketRequest(TicketType.ADULT, 2),
				new TicketRequest(TicketType.CHILD, 1) };

		assertDoesNotThrow(() -> cinemaTicketsService.purchaseTickets(1L, ticketRequests));
		BigDecimal amount = new BigDecimal("65");

		Long accountId = 1L;
		Long seatCount = 3L; // 2 adults + 1 child

		when(paymentService.debitAccount(eq(accountId), eq(amount)))
				.thenReturn(ResponseEntity.ok("Payment successful"));
		when(seatReservationService.reserveSeats(eq(accountId), eq(5L)))
				.thenReturn(ResponseEntity.ok("Seats reserved"));

		// Verify payment and seat reservation interactions
		verify(paymentService, times(1)).debitAccount(eq(1L), eq(amount));
		verify(seatReservationService).reserveSeats(eq(1L), eq(3L));
	}

	@Nested
	@DisplayName("Private Method: calculateTicketSummary")
	class CalculateTicketSummaryTests {

		private Method calculateTicketSummaryMethod;

		@BeforeEach
		void setUp() throws Exception {
			calculateTicketSummaryMethod = CinemaTicketsServiceImpl.class.getDeclaredMethod("calculateTicketSummary",
					TicketRequest[].class);
			calculateTicketSummaryMethod.setAccessible(true);
		}

		@Test
		@DisplayName("TC48: Should calculate correctly for adult tickets only")
		void shouldCalculateCorrectlyForAdultsOnly() throws Exception {
			TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 3) };

			TicketSummary summary = (TicketSummary) calculateTicketSummaryMethod.invoke(cinemaTicketsService,
					(Object) requests);

			assertNotNull(summary);
			assertEquals(3, summary.totalTickets());
			assertEquals(3, summary.adultCount());
			assertEquals(0, summary.childCount());
			assertEquals(0, summary.infantCount());
			assertEquals(75, summary.totalAmount());
			assertEquals(3, summary.totalSeats());
		}

		@Test
		@DisplayName("TC49: Should calculate correctly for children tickets only")
		void shouldCalculateCorrectlyForChildrenOnly() throws Exception {
			TicketRequest[] requests = { new TicketRequest(TicketType.CHILD, 4) };

			TicketSummary summary = (TicketSummary) calculateTicketSummaryMethod.invoke(cinemaTicketsService,
					(Object) requests);

			assertNotNull(summary);
			assertEquals(4, summary.totalTickets());
			assertEquals(0, summary.adultCount());
			assertEquals(4, summary.childCount());
			assertEquals(0, summary.infantCount());
			assertEquals(60, summary.totalAmount());
			assertEquals(4, summary.totalSeats());
		}

	}

	@Nested
	@DisplayName("Integration Tests - Complete Flow")
	class IntegrationTests {

		@Test
		@DisplayName("TC57: Complete flow should process valid request end-to-end")
		void shouldProcessValidRequestEndToEnd() throws Exception {
			TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 3), new TicketRequest(TicketType.CHILD, 2),
					new TicketRequest(TicketType.INFANT, 1) };

			BigDecimal amount = new BigDecimal("105");
			Long accountId = 1L;

			when(paymentService.debitAccount(eq(accountId), eq(amount)))
					.thenReturn(ResponseEntity.ok("Payment successful"));
			when(seatReservationService.reserveSeats(eq(accountId), eq(5L)))
					.thenReturn(ResponseEntity.ok("Seats reserved"));

			String result = cinemaTicketsService.purchaseTickets(1L, requests);

			verify(paymentService).debitAccount(eq(1L), eq(amount));
			verify(seatReservationService).reserveSeats(eq(1L), eq(5L));

			assertTrue(result.contains("Successfully purchased 6 tickets."));
			assertTrue(result.contains("Total amount: £105"));
			assertTrue(result.contains("Seats reserved: 5"));
			assertTrue(result.contains("Account ID: 1"));

		}

		@Test
		@DisplayName("TC58: Complete flow should not process request with infants exceeding adults")
		void shouldNotProcessRequestWithInfantsExceedingAdults() {
			TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 1),
					new TicketRequest(TicketType.INFANT, 2) };

			assertThrows(InvalidBookingException.class, () -> {
				cinemaTicketsService.purchaseTickets(1L, requests);
			});

			verifyNoInteractions(paymentService);
			verifyNoInteractions(seatReservationService);
		}

		@Test
		@DisplayName("TC59: Complete flow should not process request with children without adult")
		void shouldNotProcessRequestWithChildrenWithoutAdult() {
			TicketRequest[] requests = { new TicketRequest(TicketType.CHILD, 2) };

			assertThrows(InvalidBookingException.class, () -> {
				cinemaTicketsService.purchaseTickets(1L, requests);
			});

			verifyNoInteractions(paymentService);
			verifyNoInteractions(seatReservationService);
		}

		@Test
		@DisplayName("TC60: Complete flow should verify services called only once")
		void shouldVerifyServicesCalledOnlyOnce() throws InvalidBookingException {
			TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 2),
					new TicketRequest(TicketType.CHILD, 1) };

			cinemaTicketsService.purchaseTickets(1L, requests);

			verify(paymentService, times(1)).debitAccount(anyLong(), any(BigDecimal.class));
			verify(seatReservationService, times(1)).reserveSeats(anyLong(), anyLong());
		}

		@Test
		@DisplayName("TC61: Complete flow should handle maximum ticket purchase correctly")
		void shouldHandleMaximumTicketPurchaseCorrectly() throws InvalidBookingException {
			TicketRequest[] requests = { new TicketRequest(TicketType.ADULT, 20), new TicketRequest(TicketType.CHILD, 5) };

			BigDecimal amount = new BigDecimal((20 * 25) + (5 * 15)); // (20 * £25) + (5 * £15) = £500 + £75 = £575
			Long accountId = 1L;

			when(paymentService.debitAccount(eq(accountId), eq(amount)))
					.thenReturn(ResponseEntity.ok("Payment successful"));
			when(seatReservationService.reserveSeats(eq(accountId), eq(25L)))
					.thenReturn(ResponseEntity.ok("Seats reserved"));

			String result = cinemaTicketsService.purchaseTickets(1L, requests);

			verify(paymentService).debitAccount(eq(1L), eq(amount));
			verify(seatReservationService).reserveSeats(eq(1L), eq(25L));

			assertTrue(result.contains("Successfully purchased 25 tickets"));
			assertTrue(result.contains("Total amount: £575"));
			assertTrue(result.contains("Seats reserved: 25"));
		}

	}

	// ==================== PARAMETERIZED TESTS FOR EDGE CASES ====================

	@ParameterizedTest
	@MethodSource("provideInvalidTicketRequests")
	@DisplayName("Should throw exception for invalid ticket requests")
	void shouldThrowExceptionForInvalidTicketRequests(TicketRequest[] ticketRequests, String expectedMessage) {
		InvalidBookingException exception = assertThrows(InvalidBookingException.class, () -> {
			cinemaTicketsService.purchaseTickets(1L, ticketRequests);
		});

		assertEquals(expectedMessage, exception.getMessage());
	}

	private static Stream<Arguments> provideInvalidTicketRequests() {
		return Stream.of(Arguments.of(null, Constant.MESSEGE_TICKET_PROVIDED_ERR),
				Arguments.of(new TicketRequest[] {}, Constant.MESSEGE_TICKET_PROVIDED_ERR),
				Arguments.of(new TicketRequest[] { null }, Constant.MSG_TICKET_REQUEST_ERR),
				Arguments.of(new TicketRequest[] { new TicketRequest(TicketType.ADULT, -1) },
						"Ticket count cannot be negative: -1"));
	}

}
