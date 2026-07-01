package hotel.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import hotel.exception.ResourceNotFoundException;
import hotel.exception.ValidationException;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.model.Room;
import hotel.repository.ReservationRepository;
import hotel.repository.RoomRepository;

public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    public Reservation createReservation(String customerId, int roomNumber,
                                         LocalDate checkInDate, LocalDate checkOutDate) {
        if (customerId == null || customerId.isBlank()) {
            throw new ValidationException("must not be null or blank");
        }
        if (checkInDate == null || checkOutDate == null) {
            throw new ValidationException("check-in and check-out are required");
        }
        if (!checkOutDate.isAfter(checkInDate)) {
            throw new ValidationException("must be after check-in date");
        }
        if (roomRepository.getRoomByNumber(roomNumber).isEmpty()) {
            throw ResourceNotFoundException.forResource("Room", "number", roomNumber);
        }

        if (!isRoomAvailable(roomNumber, checkInDate, checkOutDate)) {
            throw new ValidationException("Room is not available for the selected dates");
        }

        String reservationId = "RES-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Reservation reservation = new Reservation(reservationId, customerId, roomNumber, checkInDate, checkOutDate);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.addReservation(reservation);
        updateRoomAvailabilityBasedOnCurrentDate(roomNumber);
        return reservation;
    }

    public void cancelReservation(String reservationId) {
        Optional<Reservation> reservationOpt = reservationRepository.getReservationById(reservationId);
        if (reservationOpt.isEmpty()) {
            throw ResourceNotFoundException.forResource("Reservation", "ID", reservationId);
        }

        Reservation reservation = reservationOpt.get();
        reservation.cancel();
        reservationRepository.updateReservation(reservation);

        updateRoomAvailabilityBasedOnCurrentDate(reservation.getRoomNumber());
    }

    public Optional<Reservation> getReservationById(String reservationId) {
        return reservationRepository.getReservationById(reservationId);
    }

    public List<Reservation> getReservationsByCustomer(String customerId) {
        return reservationRepository.getReservationsByCustomer(customerId);
    }

    public List<Reservation> getReservationsByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw ValidationException.forField("dates", "start and end dates are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before or equal to end date");
        }
        return reservationRepository.getReservationsByDateRange(startDate, endDate);
    }

    public boolean isRoomAvailable(int roomNumber, LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return false;
        }
        List<Reservation> roomReservations = reservationRepository.getReservationsByRoom(roomNumber);

        for (Reservation reservation : roomReservations) {
            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                continue;
            }
            if (reservation.overlapsWith(checkInDate, checkOutDate)) {
                return false;
            }
        }
        return true;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.getAllReservations();
    }

    private void updateRoomAvailabilityBasedOnCurrentDate(int roomNumber) {
        Optional<Room> roomOpt = roomRepository.getRoomByNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            return;
        }

        Room room = roomOpt.get();
        List<Reservation> roomReservations = reservationRepository.getReservationsByRoom(roomNumber);
        LocalDate today = LocalDate.now();

        boolean isCurrentlyOccupied = roomReservations.stream()
            .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
            .anyMatch(r -> !r.getCheckInDate().isAfter(today) && r.getCheckOutDate().isAfter(today));

        room.setAvailable(!isCurrentlyOccupied);
        roomRepository.updateRoom(room);
    }
}
