package hotel.model;

public enum RoomType {
    SINGLE("Single Room", 50.0),
    DOUBLE("Double Room", 80.0),
    SUITE("Suite", 150.0),
    DELUXE("Deluxe Suite", 200.0);

    private final String description;
    private final double defaultPrice;

    RoomType(String description, double defaultPrice) {
        this.description = description;
        this.defaultPrice = defaultPrice;
    }

    public String getDescription() {
        return description;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }
}
