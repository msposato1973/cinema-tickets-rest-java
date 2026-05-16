# Cinema Tickets Code Test

## Objective

To assess your ability to build a simple API.

The scenario and requirements will be provided separately.



# Cinema Ticket Service Implementation
## Approach

### 1. Overall Architecture

The implementation follows a clean, layered approach with clear separation of concerns:

## Overview
This is a production-ready implementation of the `CinemaTicketsService` interface that handles ticket purchases for a cinema system, strictly adhering to the specified business rules and constraints.

## Table of Contents
- [Approach](#approach)
- [Assumptions](#assumptions)
- [Business Rules](#business-rules)
- [Technical Design](#technical-design)
- [Installation & Setup](#installation--setup)
- [Usage Examples](#usage-examples)
- [Testing Strategy](#testing-strategy)
- [Error Handling](#error-handling)
- [Performance Considerations](#performance-considerations)
- [Potential Improvements](#potential-improvements)
- [Project Structure](#project-structure)
- [Code Coverage](#Code-Coverage)

## Assumptions
Assumptions made during implementation:
1. The `PaymentService` and `SeatReservationService` are external services that will be mocked for testing.
2. The `purchaseTickets` method is the main entry point for processing ticket purchases.
3. The `TicketTypeRequest` class has a method `ticketCount()` that returns the number of tickets requested for that type.


## Business Rules Implemented

### Ticket Types & Pricing
| Ticket Type | Price | Seat Required | Notes |
|------------|-------|---------------|-------|
| ADULT      | £25   | Yes           | Full-price ticket with seat allocation |
| CHILD      | £15   | Yes           | Discounted ticket with seat allocation |
| INFANT     | £0    | No            | Free ticket, sits on adult's lap |

### Purchase Rules
1. Maximum Tickets: Maximum 25 tickets per transaction (including INFANT tickets)
2. Minimum Tickets: At least 1 ticket per transaction
3. No Negative Quantities: Ticket counts cannot be negative
4. Adult Requirement: 
   - CHILD tickets cannot be purchased without at least one ADULT ticket
   - INFANT tickets cannot be purchased without at least one ADULT ticket
5. Infant Capacity: Number of INFANT tickets cannot exceed number of ADULT tickets (each infant needs an adult lap)

### Service Integration
- PaymentService: Handles payment processing for ADULT and CHILD tickets only (INFANT tickets are free)
- SeatReservationService: Handles seat allocation for ADULT and CHILD tickets only (INFANT tickets don't require seats)

## Invalid Purchase Requests

The service rejects purchase requests under the following conditions:

### Account Validation
- Account ID is null
- Account ID ≤ 0

### Ticket Request Validation
- TicketRequests array is null or empty
- Any TicketRequest element is null
- Any ticket count is negative

### Business Rule Violations
- Total tickets = 0
- Total tickets > 25
- CHILD tickets without at least one ADULT ticket
- INFANT tickets without at least one ADULT ticket
- INFANT tickets > ADULT tickets

## Technical Details

### Dependencies
- Java 21 or higher
- Spring Boot (for Service annotation)
- JUnit 5 (for testing)
- Mockito (for mocking external services)

### Key Design Decisions

#### 1. Separation of Concerns
- Validation logic isolated in private methods
- Calculation logic encapsulated in TicketSummary DTO
- Clear separation between validation, calculation, and service invocation

#### 2. Error Handling
- Custom `InvalidBookingException` with descriptive error messages
- Fail-fast approach: validation before any service calls
- No partial transactions (payment only after successful validation)

#### 3. Testability
- Constructor injection for dependencies
- Pure functions for calculations (no side effects)
- Comprehensive unit tests including private method testing via reflection

#### 4. Extensibility
- Constants for prices and limits (maintained in Constant class)
- Switch statement for ticket type handling (easy to add new types)
- Modular validation methods for easy modification

## Project Structure


### 2. Validation Strategy (Fail-Fast)

The implementation uses a fail-fast validation strategy to ensure invalid requests are rejected immediately before any processing occurs:

Order of Validation:
1. Account Validation - Check if account ID is valid (>0 and not null)
2. Request Structure Validation - Verify ticket requests array is not null/empty and contains valid elements
3. Ticket Count Validation - Ensure no negative ticket counts
4. Total Ticket Limit - Check if total tickets (including infants) exceeds 25
5. Business Rules Validation - Verify all business rules (adult requirements, infant capacity)

Why Fail-Fast?
- Prevents unnecessary processing of invalid requests
- Avoids partial transactions
- Provides immediate feedback to clients
- Reduces load on external services

### 3. Ticket Summary Design Pattern

The `TicketSummary` DTO (Data Transfer Object) encapsulates all calculated values:

``` 
public record TicketSummary(
    int totalTickets,    // Total tickets including INFANTS (for limit checking)
    int adultCount,      // Number of adult tickets
    int childCount,      // Number of child tickets  
    int infantCount,     // Number of infant tickets
    int totalAmount,     // Total payment amount (infants excluded)
    int totalSeats       // Total seats needed (adults + children only)
) {}
``` 
## 4. Business Rule Implementation Approach

Each business rule is implemented as a separate validation check:
```
Business Rule	        	Implementation	                                   		 	Location
Maximum 25 tickets	    	summary.totalTickets() > MAX_TICKETS_PER_REQUEST		 	validateBusinessRules()
Minimum 1 ticket	    	summary.totalTickets() == 0	                        	 	validateBusinessRules()
No negative quantities		request.ticketCount() < 0	                        		validateTicketRequests()
Children need adult	    	childCount > 0 && adultCount == 0	                		validateBusinessRules()
Infants need adult	    	infantCount > 0 && adultCount == 0	                		validateBusinessRules()
Infants ≤ adults	    	infantCount > adultCount	                        		validateBusinessRules()
```

### 5. Calculation Logic
The calculation follows these principles:
- Single Pass Calculation: All counts and totals are calculated in a single pass through the ticket requests for efficiency.
- Constants for Pricing: Prices and limits are defined as constants for easy maintenance.
- Switch Statement for Ticket Types: A switch statement is used to handle different ticket types, making it easy to add new types in the future.

### 6. Service Integration
- PaymentService: Called only after successful validation, with the total amount for ADULT and CHILD tickets.
- SeatReservationService: Called only after successful validation, with the total seats needed for ADULT and CHILD tickets.

### 7. Error Handling
- A custom `InvalidBookingException` is thrown for any validation failure, with descriptive error messages to aid debugging and client feedback.

### 8. Testing Strategy
- Unit Tests: Comprehensive unit tests for all validation methods, calculation logic, and service interactions.
- Mocking External Services: Use Mockito to mock `PaymentService` and `SeatReservationService` for testing the service in isolation.

### 9. Performance Considerations
- The implementation is designed to be efficient with a single pass for calculations and early validation to minimize unnecessary processing.

### 10. Potential Improvements
- Asynchronous Processing: For high load scenarios, consider making payment and seat reservation asynchronous with callbacks or messaging.
- Enhanced Error Reporting: Implement a more structured error response format for clients.

### Installation & Setup
1. Clone the repository
2. Ensure Java 21 or higher is installed
3. Build the project using Maven or Gradle
4. Run unit tests to verify functionality

### 6. Testing Approach
- Unit Tests: Focus on testing individual validation methods, calculation logic, and service interactions.
- Mocking: Use Mockito to mock external services for isolated testing of the `CinemaTicketsServiceImpl`.
- Edge Cases: Include tests for all edge cases (e.g., maximum tickets, no adult tickets, etc.)

### 7. Error Handling
- All validation failures throw a custom `InvalidBookingException` with descriptive messages.

### 8. Performance Considerations
- The implementation is designed to be efficient with a single pass for calculations and early validation to minimize unnecessary processing.

### 9. Potential Improvements
- Asynchronous Processing: For high load scenarios, consider making payment and seat reservation asynchronous with callbacks or messaging.

### 10. Project Structure - Organization
```
CinemaTicketsServiceImplTest
├── PurchaseTicketsSuccessTests (TC01-TC07)
│   ├── Adult only
│   ├── Adults and children
│   ├── Adults, children, infants
│   ├── Adults and infants
│   ├── Maximum tickets
│   └── Cumulative requests
├── PurchaseTicketsValidationFailureTests (TC08-TC17)
│   ├── Invalid account ID
│   ├── Null/empty requests
│   ├── Negative counts
│   └── Business rule violations
├── ValidateAccountIdTests (TC18-TC21)
├── ValidateTicketRequestsTests (TC22-TC28)
├── ValidateBusinessRulesTests (TC29-TC39)
├── CalculateTicketSummaryTests (TC40-TC48)
└── IntegrationTests (TC49-TC51)
```

### 11. Installation Steps
# 1. Clone the repository
git clone https://github.com/msposato1973/cinema-tickets-rest-java.git
cd cinema-tickets-service

# 2. Build the project
mvn clean install        # Using Maven
# OR
gradle clean build       # Using Gradle

# 3. Run unit tests
mvn test                 # Using Maven
# OR
gradle test              # Using Gradle

Running Specific Tests
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CinemaTicketsServiceImplTest

# Run specific test method
mvn test -Dtest=CinemaTicketsServiceImplTest#shouldSuccessfullyPurchaseAdultsChildrenAndInfants


### 11a. Ticket Summary Design Pattern
how  tu execute 
1. Ensure you have Java 21 or higher installed.
2. Clone the repository to your local machine.
3. Navigate to the project directory in your terminal.
4. Build the project using Maven or Gradle:
   - For Maven: `mvn clean install`
   - For Gradle: `gradle build`
5. Run the unit tests to verify functionality:
   - For Maven: `mvn test`
   - For Gradle: `gradle test`
`

### 12. Code-Coverage 
Using IntelliJ IDEA
Right-click on test class

Run with Coverage
View coverage report in Coverage tool window

Using Eclipse with EclEmma
Right-click on project

Coverage As → JUnit Test
View coverage highlighting in editor

htmlReport\index.html


# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CinemaTicketsServiceImplTest

Test Execution Report
--------------------------------------------------------
T E S T S
--------------------------------------------------------
Running uk.gov.dwp.engineering.recruitment.CinemaTicketsServiceImplTest
Tests run: 66, Failures: 0, Errors: 0, Skipped: 0

Results:

Tests run: 66, Failures: 0, Errors: 0, Skipped: 0

[INFO] BUILD SUCCESS
[INFO] Total time: 4.527 s