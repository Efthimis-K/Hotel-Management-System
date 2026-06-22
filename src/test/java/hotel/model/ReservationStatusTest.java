package hotel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ReservationStatusTest {

    @Test
    void containsAllFourStatuses() {
        ReservationStatus[] statuses = ReservationStatus.values();
        assertEquals(4, statuses.length);
    }

    @Test
    void pendingDisplayName() {
        assertEquals("Pending", ReservationStatus.PENDING.getDisplayName());
    }

    @Test
    void confirmedDisplayName() {
        assertEquals("Confirmed", ReservationStatus.CONFIRMED.getDisplayName());
    }

    @Test
    void cancelledDisplayName() {
        assertEquals("Cancelled", ReservationStatus.CANCELLED.getDisplayName());
    }

    @Test
    void completedDisplayName() {
        assertEquals("Completed", ReservationStatus.COMPLETED.getDisplayName());
    }

    @Test
    void valueOfReturnsCorrectEnum() {
        assertEquals(ReservationStatus.PENDING, ReservationStatus.valueOf("PENDING"));
        assertEquals(ReservationStatus.CONFIRMED, ReservationStatus.valueOf("CONFIRMED"));
        assertEquals(ReservationStatus.CANCELLED, ReservationStatus.valueOf("CANCELLED"));
        assertEquals(ReservationStatus.COMPLETED, ReservationStatus.valueOf("COMPLETED"));
    }
}
