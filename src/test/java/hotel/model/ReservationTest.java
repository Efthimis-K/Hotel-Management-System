package hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import hotel.exception.ValidationException;

class ReservationTest {

    @Test
    void constructorSetsPendingStatusAndCalculatesTotalPrice() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);

        Reservation reservation = new Reservation("RES-1000", "CUST-1", 101, checkIn, checkOut);

        assertEquals(ReservationStatus.PENDING, reservation.getStatus());
        assertEquals(0, reservation.calculateTotalPrice(120.0).compareTo(BigDecimal.valueOf(360)));
    }

    @Test
    void constructorRejectsPastCheckInDate() {
        LocalDate pastDate = LocalDate.now().minusDays(5);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> new Reservation("RES-1001", "CUST-1", 101, pastDate, pastDate.plusDays(2))
        );

        assertEquals("Check-in date cannot be in the past", exception.getMessage());
    }

    @Test
    void setCheckInDateAcceptsPastDateForDeserialization() {
        Reservation reservation = new Reservation();
        LocalDate pastDate = LocalDate.now().minusDays(10);

        reservation.setCheckInDate(pastDate);

        assertEquals(pastDate, reservation.getCheckInDate());
    }

    @Test
    void setCheckOutDateRejectsNonFutureCheckout() {
        Reservation reservation = new Reservation();
        LocalDate checkIn = LocalDate.now().plusDays(1);
        reservation.setCheckInDate(checkIn);

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> reservation.setCheckOutDate(checkIn)
        );

        assertEquals("Field 'checkOutDate': must be after check-in date", exception.getMessage());
    }

    @Test
    void cancelMarksReservationCancelled() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation("RES-2000", "CUST-2", 102, checkIn, checkIn.plusDays(2));
        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservation.cancel();

        assertEquals(ReservationStatus.CANCELLED, reservation.getStatus());
    }

    @Test
    void cancelRejectsCompletedReservation() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation("RES-3000", "CUST-3", 103, checkIn, checkIn.plusDays(2));
        reservation.setStatus(ReservationStatus.COMPLETED);

        IllegalStateException exception = assertThrows(IllegalStateException.class, reservation::cancel);

        assertEquals("Cannot cancel a completed reservation", exception.getMessage());
    }

    @Test
    void overlapsWithMatchesIntersectingDateRangesOnly() {
        LocalDate checkIn = LocalDate.now().plusDays(5);
        Reservation reservation = new Reservation("RES-4000", "CUST-4", 104, checkIn, checkIn.plusDays(4));

        assertTrue(reservation.overlapsWith(checkIn.plusDays(1), checkIn.plusDays(5)));
        assertFalse(reservation.overlapsWith(checkIn.plusDays(4), checkIn.plusDays(6)));
    }

    @Test
    void setCustomerIdRejectsBlank() {
        Reservation reservation = new Reservation();
        assertThrows(ValidationException.class, () -> reservation.setCustomerId(""));
    }

    @Test
    void setReservationIdRejectsNull() {
        Reservation reservation = new Reservation();
        assertThrows(ValidationException.class, () -> reservation.setReservationId(null));
    }

    @Test
    void calculateTotalPriceThrowsOnNullDates() {
        Reservation reservation = new Reservation();
        assertThrows(ValidationException.class, () -> reservation.calculateTotalPrice(100.0));
    }

    @Test
    void calculateTotalPriceThrowsOnNegativePrice() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        Reservation reservation = new Reservation("RES-5", "CUST-5", 105, checkIn, checkIn.plusDays(2));
        assertThrows(ValidationException.class, () -> reservation.calculateTotalPrice(-1.0));
    }
}
