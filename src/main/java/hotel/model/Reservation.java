package hotel.model;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import hotel.exception.ValidationException;

public class Reservation {

    private String reservationId;
    private String customerId;
    private int roomNumber;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private ReservationStatus status;

    public Reservation() {
        this.status = ReservationStatus.PENDING;
    }

    public Reservation(String reservationId, String customerId, int roomNumber,
            LocalDate checkInDate, LocalDate checkOutDate) {
        validateReservationId(reservationId);
        validateCustomerId(customerId);
        validateRoomNumber(roomNumber);
        validateCheckInDate(checkInDate);
        validateCheckOutDate(checkOutDate, checkInDate);
        if (checkInDate != null && checkInDate.isBefore(LocalDate.now())) {
            throw new ValidationException("Check-in date cannot be in the past");
        }
        this.reservationId = reservationId;
        this.customerId = customerId;
        this.roomNumber = roomNumber;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = ReservationStatus.PENDING;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        validateReservationId(reservationId);
        this.reservationId = reservationId;
    }

    private static void validateReservationId(String reservationId) {
        if (reservationId == null || reservationId.isBlank()) {
            throw ValidationException.forField("reservationId", "must not be null or blank");
        }
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        validateCustomerId(customerId);
        this.customerId = customerId;
    }

    private static void validateCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw ValidationException.forField("customerId", "must not be null or blank");
        }
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        validateRoomNumber(roomNumber);
        this.roomNumber = roomNumber;
    }

    private static void validateRoomNumber(int roomNumber) {
        if (roomNumber <= 0) {
            throw ValidationException.forField("roomNumber", "must be positive");
        }
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        validateCheckInDate(checkInDate);
        if (checkOutDate != null && !checkInDate.isBefore(checkOutDate)) {
            throw ValidationException.forField("checkInDate", "must be before check-out date");
        }
        this.checkInDate = checkInDate;
    }

    private static void validateCheckInDate(LocalDate checkInDate) {
        if (checkInDate == null) {
            throw ValidationException.forField("checkInDate", "must not be null");
        }
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        validateCheckOutDate(checkOutDate, this.checkInDate);
        this.checkOutDate = checkOutDate;
    }

    private static void validateCheckOutDate(LocalDate checkOutDate, LocalDate checkInDate) {
        if (checkOutDate == null) {
            throw ValidationException.forField("checkOutDate", "must not be null");
        }
        if (checkInDate != null && !checkOutDate.isAfter(checkInDate)) {
            throw ValidationException.forField("checkOutDate", "must be after check-in date");
        }
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        if (status == null) {
            throw ValidationException.forField("status", "must not be null");
        }
        this.status = status;
    }

    public BigDecimal calculateTotalPrice(double pricePerNight) {
        if (checkInDate == null || checkOutDate == null) {
            throw new ValidationException("Cannot calculate total price: check-in and check-out dates must be set");
        }
        if (Double.isNaN(pricePerNight) || Double.isInfinite(pricePerNight) || pricePerNight < 0) {
            throw ValidationException.forField("pricePerNight", "must be a non-negative finite number");
        }
        long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (nights < 0) {
            throw new ValidationException("Cannot calculate total price: check-out is before check-in");
        }
        BigDecimal price = BigDecimal.valueOf(pricePerNight);
        BigDecimal total = BigDecimal.valueOf(nights).multiply(price, new MathContext(16, RoundingMode.HALF_UP));
        if (total.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) > 0) {
            throw new ValidationException("Total price overflow: " + nights + " nights x $" + pricePerNight);
        }
        return total;
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED || status == ReservationStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a " + status.getDisplayName().toLowerCase() + " reservation");
        }
        this.status = ReservationStatus.CANCELLED;
    }

    public boolean overlapsWith(LocalDate otherStart, LocalDate otherEnd) {
        if (otherStart == null || otherEnd == null) {
            return false;
        }
        return (checkInDate.isBefore(otherEnd) && checkOutDate.isAfter(otherStart));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reservation that = (Reservation) o;
        return Objects.equals(reservationId, that.reservationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }

    @Override
    public String toString() {
        return "Reservation{"
                + "reservationId='" + reservationId + '\''
                + ", customerId='" + customerId + '\''
                + ", roomNumber=" + roomNumber
                + ", checkInDate=" + checkInDate
                + ", checkOutDate=" + checkOutDate
                + ", status=" + status
                + '}';
    }
}
