package hotel.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import hotel.exception.ResourceNotFoundException;
import hotel.exception.ValidationException;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.repository.ReservationRepository;
import hotel.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createReservationCreatesConfirmedReservationWhenRoomExistsAndIsAvailable() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        LocalDate checkOut = checkIn.plusDays(3);
        Room room = new Room(101, RoomType.SINGLE, 80.0);
        when(roomRepository.getRoomByNumber(101)).thenReturn(Optional.of(room));
        when(reservationRepository.getReservationsByRoom(101)).thenReturn(List.of());

        Reservation reservation = reservationService.createReservation("CUST-1", 101, checkIn, checkOut);

        assertNotNull(reservation.getReservationId());
        assertTrue(reservation.getReservationId().startsWith("RES-"));
        assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus());
        assertEquals("CUST-1", reservation.getCustomerId());
        verify(reservationRepository).addReservation(reservation);
        verify(roomRepository).updateRoom(any(Room.class));
    }

    @Test
    void createReservationRejectsMissingRoom() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        when(roomRepository.getRoomByNumber(404)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.createReservation("CUST-1", 404, checkIn, checkIn.plusDays(2)));

        assertEquals("Room with number 404 not found", exception.getMessage());
        verify(reservationRepository, never()).addReservation(any());
        verify(roomRepository, never()).updateRoom(any());
    }

    @Test
    void createReservationRejectsUnavailableRoom() {
        LocalDate checkIn = LocalDate.now().plusDays(3);
        LocalDate checkOut = checkIn.plusDays(2);
        Room room = new Room(202, RoomType.DOUBLE, 100.0);
        Reservation existingReservation = new Reservation("RES-EXIST", "CUST-2", 202, checkIn.minusDays(1),
                checkIn.plusDays(1));
        existingReservation.setStatus(ReservationStatus.CONFIRMED);

        when(roomRepository.getRoomByNumber(202)).thenReturn(Optional.of(room));
        when(reservationRepository.getReservationsByRoom(202)).thenReturn(List.of(existingReservation));

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservationService.createReservation("CUST-3", 202, checkIn, checkOut));

        assertEquals("Room is not available for the selected dates", exception.getMessage());
        verify(reservationRepository, never()).addReservation(any());
        verify(roomRepository, never()).updateRoom(any());
    }

    @Test
    void cancelReservationUpdatesStoredReservation() {
        LocalDate checkIn = LocalDate.now().plusDays(1);
        Room room = new Room(303, RoomType.SINGLE, 80.0);
        Reservation reservation = new Reservation("RES-CANCEL", "CUST-4", 303, checkIn, checkIn.plusDays(2));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.getReservationById("RES-CANCEL")).thenReturn(Optional.of(reservation));
        when(roomRepository.getRoomByNumber(303)).thenReturn(Optional.of(room));
        when(reservationRepository.getReservationsByRoom(303)).thenReturn(List.of());

        reservationService.cancelReservation("RES-CANCEL");

        ArgumentCaptor<Reservation> captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepository).updateReservation(captor.capture());
        assertEquals(ReservationStatus.CANCELLED, captor.getValue().getStatus());
        verify(roomRepository).updateRoom(any(Room.class));
    }

    @Test
    void cancelReservationRejectsMissingReservation() {
        when(reservationRepository.getReservationById("RES-MISSING")).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> reservationService.cancelReservation("RES-MISSING"));

        assertEquals("Reservation with ID RES-MISSING not found", exception.getMessage());
        verify(roomRepository, never()).updateRoom(any());
    }

    @Test
    void isRoomAvailableIgnoresCancelledReservations() {
        LocalDate checkIn = LocalDate.now().plusDays(10);
        Reservation cancelledReservation = new Reservation("RES-CAN", "CUST-5", 404, checkIn, checkIn.plusDays(2));
        cancelledReservation.setStatus(ReservationStatus.CANCELLED);
        when(reservationRepository.getReservationsByRoom(404)).thenReturn(List.of(cancelledReservation));

        boolean available = reservationService.isRoomAvailable(404, checkIn.plusDays(1), checkIn.plusDays(3));

        assertTrue(available);
    }

    @Test
    void isRoomAvailableReturnsFalseForOverlappingConfirmedReservation() {
        LocalDate checkIn = LocalDate.now().plusDays(7);
        Reservation reservation = new Reservation("RES-BUSY", "CUST-6", 505, checkIn, checkIn.plusDays(3));
        reservation.setStatus(ReservationStatus.CONFIRMED);
        when(reservationRepository.getReservationsByRoom(505)).thenReturn(List.of(reservation));

        boolean available = reservationService.isRoomAvailable(505, checkIn.plusDays(1), checkIn.plusDays(2));

        assertFalse(available);
    }

    @Test
    void getReservationsByDateRangeReturnsIntersectingReservations() {
        LocalDate start = LocalDate.now().plusDays(15);
        Reservation matching = new Reservation("RES-1", "CUST-7", 601, start.plusDays(1), start.plusDays(3));
        Reservation nonMatching = new Reservation("RES-2", "CUST-8", 602, start.plusDays(5), start.plusDays(7));
        when(reservationRepository.getAllReservations()).thenReturn(List.of(matching, nonMatching));

        List<Reservation> reservations = reservationService.getReservationsByDateRange(start, start.plusDays(2));

        assertEquals(1, reservations.size());
        assertEquals("RES-1", reservations.get(0).getReservationId());
    }

    @Test
    void getReservationsByDateRangeRejectsInvertedRange() {
        LocalDate start = LocalDate.now().plusDays(10);
        LocalDate end = start.minusDays(5);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> reservationService.getReservationsByDateRange(start, end));

        assertEquals("Start date must be before or equal to end date", exception.getMessage());
    }

    @Test
    void historicalReservationCanBeLoadedViaSetter() {
        LocalDate pastDate = LocalDate.now().minusDays(10);
        Reservation reservation = new Reservation();
        reservation.setReservationId("RES-HIST");
        reservation.setCustomerId("CUST-HIST");
        reservation.setRoomNumber(701);
        reservation.setCheckInDate(pastDate);
        reservation.setCheckOutDate(pastDate.plusDays(3));
        reservation.setStatus(ReservationStatus.COMPLETED);

        assertEquals(pastDate, reservation.getCheckInDate());
        assertEquals("RES-HIST", reservation.getReservationId());
    }

    @Test
    void newReservationRejectsPastCheckInDate() {
        LocalDate pastDate = LocalDate.now().minusDays(5);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> new Reservation("RES-NEW", "CUST-NEW", 801, pastDate, pastDate.plusDays(2)));

        assertEquals("Check-in date cannot be in the past", exception.getMessage());
    }

    @Test
    void createReservationRejectsBlankCustomerId() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        assertThrows(
            ValidationException.class,
            () -> reservationService.createReservation("", 101, checkIn, checkIn.plusDays(2))
        );
    }

    @Test
    void createReservationRejectsNullCheckInDate() {
        LocalDate checkOut = LocalDate.now().plusDays(2);
        assertThrows(
            ValidationException.class,
            () -> reservationService.createReservation("CUST-1", 101, null, checkOut)
        );
    }

    @Test
    void createReservationRejectsNullCheckOutDate() {
        LocalDate checkIn = LocalDate.now().plusDays(2);
        assertThrows(
            ValidationException.class,
            () -> reservationService.createReservation("CUST-1", 101, checkIn, null)
        );
    }

    @Test
    void getReservationByIdReturnsRepositoryResult() {
        Reservation reservation = new Reservation("RES-FIND", "CUST-F", 901, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        when(reservationRepository.getReservationById("RES-FIND")).thenReturn(Optional.of(reservation));

        var result = reservationService.getReservationById("RES-FIND");

        assertTrue(result.isPresent());
        assertEquals("RES-FIND", result.get().getReservationId());
    }

    @Test
    void getReservationsByCustomerReturnsRepositoryResults() {
        Reservation reservation = new Reservation("RES-CUST", "CUST-LIST", 901, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        when(reservationRepository.getReservationsByCustomer("CUST-LIST")).thenReturn(List.of(reservation));

        var results = reservationService.getReservationsByCustomer("CUST-LIST");

        assertEquals(1, results.size());
        assertEquals("CUST-LIST", results.getFirst().getCustomerId());
    }

    @Test
    void getAllReservationsReturnsRepositoryResults() {
        Reservation reservation = new Reservation("RES-ALL", "CUST-A", 901, LocalDate.now().plusDays(3), LocalDate.now().plusDays(5));
        when(reservationRepository.getAllReservations()).thenReturn(List.of(reservation));

        var results = reservationService.getAllReservations();

        assertEquals(1, results.size());
    }
}
