package hotel.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hotel.exception.DuplicateResourceException;
import hotel.exception.ResourceNotFoundException;
import hotel.model.Reservation;
import hotel.model.ReservationStatus;
import hotel.util.JsonFileHandler;

public class ReservationRepository {
    private static final String FILE_PATH = "data/reservations.json";
    private List<Reservation> reservations;

    public ReservationRepository() {
        loadReservations();
    }

    private void loadReservations() {
        reservations = JsonFileHandler.loadFromFile(FILE_PATH, Reservation.class);
        if (reservations == null) {
            reservations = new ArrayList<>();
        }
    }

    private void saveReservations() {
        JsonFileHandler.saveToFile(reservations, FILE_PATH);
    }

    public void addReservation(Reservation reservation) {
        if (reservations.stream().anyMatch(r -> r.getReservationId().equals(reservation.getReservationId()))) {
            throw DuplicateResourceException.forResource("Reservation", "ID", reservation.getReservationId());
        }
        reservations.add(reservation);
        saveReservations();
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations);
    }

    public Optional<Reservation> getReservationById(String reservationId) {
        if (reservationId == null) {
            return Optional.empty();
        }
        return reservations.stream().filter(r -> r.getReservationId().equals(reservationId)).findFirst();
    }

    public void updateReservation(Reservation reservation) {
        boolean removed = reservations.removeIf(r -> r.getReservationId().equals(reservation.getReservationId()));
        if (!removed) {
            throw ResourceNotFoundException.forResource("Reservation", "ID", reservation.getReservationId());
        }
        reservations.add(reservation);
        saveReservations();
    }

    public void deleteReservation(String reservationId) {
        boolean removed = reservations.removeIf(r -> r.getReservationId().equals(reservationId));
        if (!removed) {
            throw ResourceNotFoundException.forResource("Reservation", "ID", reservationId);
        }
        saveReservations();
    }

    public List<Reservation> getReservationsByCustomer(String customerId) {
        if (customerId == null) {
            return List.of();
        }
        return reservations.stream()
                .filter(r -> customerId.equals(r.getCustomerId()))
                .toList();
    }

    public List<Reservation> getReservationsByRoom(int roomNumber) {
        return reservations.stream()
                .filter(r -> r.getRoomNumber() == roomNumber)
                .toList();
    }

    public List<Reservation> getActiveReservations() {
        return reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.PENDING)
                .toList();
    }
}
