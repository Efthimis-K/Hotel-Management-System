package hotel.model;

import java.util.Objects;

import hotel.exception.ValidationException;

public class Room {
    private int roomNumber;
    private RoomType roomType;
    private double pricePerNight;
    private boolean isAvailable;

    public Room() {
        this.isAvailable = true;
    }

    public Room(int roomNumber, RoomType roomType, double pricePerNight) {
        setRoomNumber(roomNumber);
        this.roomType = roomType;
        setPricePerNight(pricePerNight);
        this.isAvailable = true;
    }

    public int getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(int roomNumber) {
        if (roomNumber <= 0) {
            throw new ValidationException("Room number must be positive");
        }
        this.roomNumber = roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public void setRoomType(RoomType roomType) {
        if (roomType == null) {
            throw ValidationException.forField("roomType", "must not be null");
        }
        this.roomType = roomType;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        if (Double.isNaN(pricePerNight) || Double.isInfinite(pricePerNight)) {
            throw ValidationException.forField("pricePerNight", "must be a finite number");
        }
        if (pricePerNight <= 0) {
            throw new ValidationException("Price per night must be positive");
        }
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void toggleAvailability() {
        this.isAvailable = !this.isAvailable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomNumber == room.roomNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomNumber);
    }

    @Override
    public String toString() {
        return "Room{" +
                "roomNumber=" + roomNumber +
                ", roomType=" + roomType +
                ", pricePerNight=" + pricePerNight +
                ", isAvailable=" + isAvailable +
                '}';
    }
}
