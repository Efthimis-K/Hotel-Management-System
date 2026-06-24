# Hotel Management System

A hotel management system with a menu-driven console UI and a JavaFX GUI, built with Java 21 and Maven. Persistence is provided by SQLite, with one-time JSON migration on first launch.

## Features

- **Room Management** — Create, view, and manage rooms; check availability by date range
- **Customer Management** — Register customers with ID, name, email, and phone validation; view all customers
- **Reservation Management** — Create, cancel, and search reservations; supports PENDING, CONFIRMED, CANCELLED, and COMPLETED statuses; automatic total price calculation
- **Robust Error Handling** — Centralized `ErrorHandler` with logging and user-friendly messages; startup safety checks
- **SQLite Persistence** — Local SQLite database (`data/hotel.db`) with schema, indexes, and foreign-key enforcement; legacy JSON files are auto-migrated on first run
- **JavaFX GUI** — Optional single-window graphical interface with breadcrumb navigation

## Requirements

- Java 21+
- Maven 3.6+

## Running the Application

### Console UI

```bash
mvn exec:java
```

### JavaFX GUI

```bash
mvn javafx:run
```

Or open the project in your IDE and run `hotel.gui.GuiMain` for the GUI, or `hotel.Main` for the console UI.

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

Prices can be customized per room at creation time.

## Customer Validation

- **Email** — validated using Apache Commons Validator
- **Phone** — must match `^[+]?[0-9]{10,15}$`

## Reservation Statuses

- `PENDING` — initial state
- `CONFIRMED` — successfully booked
- `CANCELLED` — cancelled by user
- `COMPLETED` — stay has ended

## Architecture

Layered architecture using Repository and Service patterns with constructor-based dependency injection.

- `hotel.model` — Domain entities (`Room`, `Customer`, `Reservation`) and enums (`RoomType`, `ReservationStatus`)
- `hotel.service` — Business logic (`RoomService`, `ReservationService`) and `HotelManager` orchestration
- `hotel.repository` — Data access via JDBC (`RoomRepository`, `CustomerRepository`, `ReservationRepository`)
- `hotel.storage` — `DatabaseManager` (SQLite singleton connection), `DatabaseInitializer` (schema + one-time JSON-to-SQLite migration), and `JsonImportUtil` (JSON ingest utility)
- `hotel.util` — `JsonFileHandler` (Jackson-based JSON I/O) and `ErrorHandler` (centralized logging and user messaging)
- `hotel.exception` — Custom exceptions (`ValidationException`, `ResourceNotFoundException`, `DuplicateResourceException`, `StorageException`, `HotelException`)
- `hotel.gui` — JavaFX single-window application. `GuiMain` hosts a `BorderPane` whose center is swapped between views; `NavigationManager` keeps a back/forward history and updates the breadcrumb. One view class per console operation — no new windows or dialogs are ever opened.
- `hotel.Main` — Console UI entry point

## Persistence

`DatabaseManager` provides a singleton SQLite connection to `data/hotel.db`. `DatabaseInitializer` creates the schema (`customers`, `rooms`, `reservations`, plus indexes) on first launch and migrates any existing `data/customers.json`, `data/rooms.json`, and `data/reservations.json` into the database. Subsequent starts skip migration and use the database directly.

## Project Structure

```
src/
├── main/java/hotel/
│   ├── model/          # Domain entities and enums
│   ├── service/        # Business logic
│   ├── repository/     # Data access layer
│   ├── storage/        # Database connection and initialization
│   ├── util/           # Utilities
│   ├── exception/      # Custom exceptions
│   ├── gui/            # JavaFX views and navigation
│   └── Main.java       # Console entry point
└── test/java/hotel/    # JUnit and Mockito tests
```

## Testing

Tests are located in `src/test/java/hotel/` and use JUnit 5.14.4 and Mockito 5.23.0. To run tests:

```bash
mvn test
```

## Data Directory

- `data/hotel.db` — SQLite database
- `data/customers.json`, `data/rooms.json`, `data/reservations.json` — legacy JSON files (auto-migrated on first launch)

## Tech Stack

- **Java 21** — `switch` expressions, text blocks, `LocalDate`
- **Maven** — build and dependency management
- **SQLite** — local relational database via `sqlite-jdbc` 3.53.2.0
- **Jackson** 2.21.4 — JSON serialization with Java 8 Time module (used for legacy migration)
- **Apache Commons Validator** 1.10.1 — email format validation
- **JavaFX** 21.0.2 — optional GUI framework
- **JUnit** 5.14.4, **Mockito** 5.23.0 — testing
