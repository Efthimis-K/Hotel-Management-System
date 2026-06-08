# Hotel Management System

A console-based hotel management system with file-based JSON persistence, built with Java 21 and Maven.

## Features

- **Room Management** — Create, view, and manage rooms; check availability by date range; rooms auto-mark as unavailable when occupied
- **Customer Management** — Register customers with ID, name, email, and phone validation; view all customers
- **Reservation Management** — Create, cancel, and search reservations; supports PENDING, CONFIRMED, CANCELLED, and COMPLETED statuses; automatic total price calculation
- **Robust Error Handling** — Centralized `ErrorHandler` with logging and user-friendly messages; startup safety checks
- **File Persistence** — JSON storage via Jackson `JavaTimeModule` for dates; files auto-created on first write
- **JavaFX GUI** — Optional graphical interface with room management view

## Requirements

- Java 21+
- Maven 3.6+

## Running the Application

### Console UI

```bash
mvn clean compile exec:java -Dexec.mainClass="hotel.Main"
```

### JavaFX GUI

```bash
mvn clean javafx:run
```

Or open the project in your IDE and run `GuiMain.java` for the GUI, or `Main.java` for the console UI.

## Usage

The application presents a layered menu-driven interface.

### Main Menu

1. Room Management
2. Customer Management
3. Reservation Management
4. Exit

### Room Management

1. Create Room — choose type and set a custom price
2. View All Rooms — shows number, type, price, and availability
3. View Available Rooms — filter by date range
4. Back to Main Menu

### Customer Management

1. Register Customer — ID, first/last name, email, phone with validation
2. View All Customers
3. Back to Main Menu

### Reservation Management

1. Create Reservation — customer and room selection with date validation
2. Cancel Reservation — changes status to CANCELLED
3. Search Reservations — by customer, date range, or both
4. Check Availability — specific room or all available rooms
5. Back to Main Menu

**Date format:** `yyyy-MM-dd` (e.g., `2024-12-25`)  
**Reservation IDs:** auto-generated as `RES-XXXXXXXX` (8 hex chars)

## Room Types & Pricing

| Type   | Description  | Default Price/Night |
| ------ | ------------ | ------------------- |
| Single | Single Room  | $50                 |
| Double | Double Room  | $80                 |
| Suite  | Suite        | $150                |
| Deluxe | Deluxe Suite | $200                |

_Prices can be customized per room at creation time._

## Customer Validation

- **Email** — validated using Apache Commons Validator
- **Phone** — must match `^[+]?[0-9]{10,15}$`

## Reservation Statuses

- `PENDING` — initial state
- `CONFIRMED` — successfully booked
- `CANCELLED` — cancelled by user
- `COMPLETED` — stay has ended

_Rooms auto-toggle availability based on current date relative to their CONFIRMED reservations._

## Architecture

Layered architecture using Repository and Service patterns with constructor-based dependency injection.

- `model/` — Domain entities (`Room`, `Customer`, `Reservation`) and enums (`RoomType`, `ReservationStatus`)
- `service/` — Business logic (`RoomService`, `ReservationService`) and `HotelManager` orchestration
- `repository/` — Data access with file persistence (`RoomRepository`, `CustomerRepository`, `ReservationRepository`)
- `util/` — `JsonFileHandler` (Jackson-based JSON I/O) and `ErrorHandler` (centralized logging and user messaging)
- `exception/` — Custom exceptions (`ValidationException`, `ResourceNotFoundException`, `DuplicateResourceException`, `StorageException`, `HotelException`)
- `Main.java` — Console UI
- `gui/` — JavaFX single-window application. `GuiMain` hosts a `BorderPane` whose center is swapped between views; `NavigationManager` keeps a back/forward history and updates the breadcrumb. One view class per console operation (e.g. `CreateRoomView`, `SearchReservationsView`, `CheckAvailabilityView`) — no new windows or dialogs are ever opened.

Data is stored in `data/rooms.json`, `data/customers.json`, and `data/reservations.json`.

## Persistence

`JsonFileHandler` uses Jackson with `JavaTimeModule` to serialize/deserialize `LocalDate` fields. On startup, repositories load existing data from `data/` (or create empty lists if files do not exist). Room availability is determined dynamically at query time via `ReservationService`.

## Testing

Tests are located in `src/test/java/` and use JUnit. To run tests:

```bash
mvn test
```

## Tech Stack

- **Java 21** — `switch` expressions, text blocks, `LocalDate`
- **Maven** — build and dependency management
- **Jackson** — JSON serialization with Java 8 Time module
- **Apache Commons Validator** — email format validation
- **JavaFX** — optional GUI framework
