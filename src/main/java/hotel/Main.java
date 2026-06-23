package hotel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

import hotel.exception.HotelException;
import hotel.model.Customer;
import hotel.model.Reservation;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.repository.CustomerRepository;
import hotel.repository.ReservationRepository;
import hotel.repository.RoomRepository;
import hotel.service.HotelManager;
import hotel.storage.DatabaseInitializer;
import hotel.util.ErrorHandler;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final RoomType[] ROOM_TYPES = RoomType.values();

    private static HotelManager hotelManager;
    private static RoomRepository roomRepository;
    private static ReservationRepository reservationRepository;
    private static CustomerRepository customerRepository;

    public static void main(String[] args) {
        try {
            initializeRepositories();
        } catch (RuntimeException e) {
            // Startup failed (e.g. corrupt data files). Print a clean message
            // and exit with a non-zero code so callers (CI, scripts) can detect it.
            System.out.println(ErrorHandler.handleStartupError(e, LOGGER));
            System.exit(1);
        }

        while (true) {
            try {
                displayMainMenu();
                int choice = getIntInput("Enter your choice: ");

                switch (choice) {
                    case 1 -> roomManagementMenu();
                    case 2 -> customerManagementMenu();
                    case 3 -> reservationManagementMenu();
                    case 4 -> {
                        System.out.println("Thank you for using Hotel Management System. Goodbye!");
                        return;
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (HotelException e) {
                // Expected domain error: log and show a clean message.
                System.out.println(ErrorHandler.handle(e, LOGGER));
            } catch (RuntimeException e) {
                // Unexpected error: log full stack trace and show a generic message.
                System.out.println(ErrorHandler.handle(e, LOGGER));
            }
        }
    }

private static void initializeRepositories() {
        // Initialize database and migrate from JSON if needed
        DatabaseInitializer.initialize();
        
        roomRepository = new RoomRepository();
        reservationRepository = new ReservationRepository();
        customerRepository = new CustomerRepository();
        hotelManager = new HotelManager(roomRepository, reservationRepository, customerRepository);
    }

    private static void displayMainMenu() {
        System.out.println("\n=== Hotel Management System ===");
        System.out.println("1. Room Management");
        System.out.println("2. Customer Management");
        System.out.println("3. Reservation Management");
        System.out.println("4. Exit");
    }

    private static void roomManagementMenu() {
        while (true) {
            System.out.println("\n--- Room Management ---");
            System.out.println("1. Create Room");
            System.out.println("2. View All Rooms");
            System.out.println("3. View Available Rooms");
            System.out.println("4. Back to Main Menu");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> createRoom();
                case 2 -> displayAllRooms();
                case 3 -> displayAvailableRooms();
                case 4 -> { return; }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void customerManagementMenu() {
        while (true) {
            System.out.println("\n--- Customer Management ---");
            System.out.println("1. Register Customer");
            System.out.println("2. View All Customers");
            System.out.println("3. Back to Main Menu");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> registerCustomer();
                case 2 -> displayAllCustomers();
                case 3 -> { return; }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void reservationManagementMenu() {
        while (true) {
            System.out.println("\n--- Reservation Management ---");
            System.out.println("1. Create Reservation");
            System.out.println("2. Cancel Reservation");
            System.out.println("3. Search Reservations");
            System.out.println("4. Check Availability");
            System.out.println("5. Back to Main Menu");

            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> createReservation();
                case 2 -> cancelReservation();
                case 3 -> searchReservations();
                case 4 -> checkAvailability();
                case 5 -> { return; }
                default -> System.out.println("Invalid choice. Please try again.");
            }
        }
    }

    private static void createRoom() {
        System.out.println("\n--- Create New Room ---");
        int roomNumber = getIntInput("Enter room number: ");
        var roomService = hotelManager.getRoomService();

        if (roomService.getRoomByNumber(roomNumber).isPresent()) {
            System.out.println("Error: Room with number " + roomNumber + " already exists.");
            return;
        }

        System.out.println("Available room types:");
        for (int i = 0; i < ROOM_TYPES.length; i++) {
            System.out.println((i + 1) + ". " + ROOM_TYPES[i].getDescription() + " (Default price: $" + ROOM_TYPES[i].getDefaultPrice() + ")");
        }

        int typeChoice = getIntInput("Select room type (1-" + ROOM_TYPES.length + "): ");
        if (typeChoice < 1 || typeChoice > ROOM_TYPES.length) {
            System.out.println("Error: Invalid room type selection.");
            return;
        }
        RoomType roomType = ROOM_TYPES[typeChoice - 1];

        double price = getDoubleInput("Enter price per night (default: $" + roomType.getDefaultPrice() + "): ", roomType.getDefaultPrice());

        Room room = new Room(roomNumber, roomType, price);
        roomService.createRoom(room);

        System.out.println("Room created successfully!");
        System.out.println(room);
    }

    private static void displayAvailableRooms() {
        System.out.println("\n--- View Available Rooms ---");
        LocalDate checkIn = getDateInput("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkOut = getDateInput("Enter check-out date (yyyy-MM-dd): ");

        var availableRooms = hotelManager.getAvailableRoomsForDateRange(checkIn, checkOut);
        System.out.println("\nAvailable rooms for " + checkIn + " to " + checkOut + ":");
        if (availableRooms.isEmpty()) {
            System.out.println("No rooms available for the selected dates.");
            return;
        }
        printRoomTable(availableRooms, false);
    }

    private static void displayAllRooms() {
        var roomService = hotelManager.getRoomService();
        var rooms = roomService.getAllRooms();
        System.out.println("\n--- All Rooms ---");
        if (rooms.isEmpty()) {
            System.out.println("No rooms found. Create some rooms first.");
            return;
        }
        printRoomTable(rooms, true);
    }

    private static void registerCustomer() {
        System.out.println("\n--- Register Customer ---");
        String customerId = getStringInput("Enter customer ID: ");

        if (hotelManager.getCustomerById(customerId).isPresent()) {
            System.out.println("Error: Customer with ID " + customerId + " already exists.");
            return;
        }

        try {
            Customer customer = createCustomerFromInput(customerId);
            hotelManager.registerCustomer(customer);
            System.out.println("Customer registered successfully!");
            System.out.println(customer);
        } catch (HotelException e) {
            System.out.println(ErrorHandler.handle(e, LOGGER));
        }
    }

    private static void displayAllCustomers() {
        System.out.println("\n--- All Customers ---");
        var customers = hotelManager.getAllCustomers();

        if (customers.isEmpty()) {
            System.out.println("No customers found. Register some customers first.");
        } else {
            System.out.printf("%-15s %-20s %-20s %-30s %-15s%n",
                "Customer ID", "First Name", "Last Name", "Email", "Phone");
            System.out.println("-".repeat(100));
            for (Customer customer : customers) {
                System.out.printf("%-15s %-20s %-20s %-30s %-15s%n",
                    customer.getCustomerId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    customer.getEmail(),
                    customer.getPhoneNumber());
            }
        }
    }

    private static void createReservation() {
        System.out.println("\n--- Create Reservation ---");

        String customerId = getStringInput("Enter customer ID: ");
        if (!ensureCustomerExistsForReservation(customerId)) {
            return;
        }

        int roomNumber = getIntInput("Enter room number: ");
        var roomService = hotelManager.getRoomService();
        var roomOptional = roomService.getRoomByNumber(roomNumber);
        if (roomOptional.isEmpty()) {
            System.out.println("Error: Room not found.");
            return;
        }
        Room room = roomOptional.get();

        LocalDate checkIn = getDateInput("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkOut = getDateInput("Enter check-out date (yyyy-MM-dd): ");

        try {
            var reservationService = hotelManager.getReservationService();
            Reservation reservation = reservationService.createReservation(customerId, roomNumber, checkIn, checkOut);

            long totalPrice = reservation.calculateTotalPrice(room.getPricePerNight());

            System.out.println("Reservation created successfully!");
            System.out.println("Reservation ID: " + reservation.getReservationId());
            System.out.println("Room: " + roomNumber);
            System.out.println("Check-in: " + checkIn.format(dateFormatter));
            System.out.println("Check-out: " + checkOut.format(dateFormatter));
            System.out.println("Total Price: $" + totalPrice);
        } catch (HotelException e) {
            System.out.println(ErrorHandler.handle(e, LOGGER));
        }
    }

    private static boolean ensureCustomerExistsForReservation(String customerId) {
        if (hotelManager.getCustomerById(customerId).isPresent()) {
            return true;
        }

        System.out.println("Customer not found. Registering a new customer.");

        try {
            Customer customer = createCustomerFromInput(customerId);
            hotelManager.registerCustomer(customer);
            System.out.println("Customer registered successfully! Continuing with reservation.");
            return true;
        } catch (HotelException e) {
            System.out.println(ErrorHandler.handle(e, LOGGER));
            return false;
        }
    }

    private static void cancelReservation() {
        System.out.println("\n--- Cancel Reservation ---");
        String reservationId = getStringInput("Enter reservation ID: ");

        try {
            hotelManager.getReservationService().cancelReservation(reservationId);
            System.out.println("Reservation cancelled successfully!");
        } catch (HotelException e) {
            System.out.println(ErrorHandler.handle(e, LOGGER));
        }
    }

    private static void searchReservations() {
        System.out.println("\n--- Search Reservations ---");
        System.out.println("1. Search by Customer");
        System.out.println("2. Search by Date Range");
        System.out.println("3. Search by Customer and Date Range");

        int choice = getIntInput("Enter your choice: ");

        switch (choice) {
            case 1 -> searchByCustomer();
            case 2 -> searchByDateRange();
            case 3 -> searchByCustomerAndDateRange();
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void searchByCustomer() {
        String customerId = getStringInput("Enter customer ID: ");
        displayReservations(hotelManager.getReservationService().getReservationsByCustomer(customerId),
            "Reservations for Customer: " + customerId);
    }

    private static void searchByDateRange() {
        LocalDate startDate = getDateInput("Enter start date (yyyy-MM-dd): ");
        LocalDate endDate = getDateInput("Enter end date (yyyy-MM-dd): ");
        if (startDate.isAfter(endDate)) {
            System.out.println("Error: Start date must be before or equal to end date.");
            return;
        }
        displayReservations(hotelManager.getReservationService().getReservationsByDateRange(startDate, endDate),
            "Reservations between " + startDate + " and " + endDate);
    }

    private static void searchByCustomerAndDateRange() {
        String customerId = getStringInput("Enter customer ID: ");
        LocalDate startDate = getDateInput("Enter start date (yyyy-MM-dd): ");
        LocalDate endDate = getDateInput("Enter end date (yyyy-MM-dd): ");
        if (startDate.isAfter(endDate)) {
            System.out.println("Error: Start date must be before or equal to end date.");
            return;
        }
        var allReservations = hotelManager.getReservationService().getReservationsByCustomer(customerId);

        var filteredReservations = allReservations.stream()
            .filter(r -> !r.getCheckInDate().isAfter(endDate) && !r.getCheckOutDate().isBefore(startDate))
            .toList();

        displayReservations(filteredReservations,
            "Reservations for Customer: " + customerId + " between " + startDate + " and " + endDate);
    }

    private static void displayReservations(List<Reservation> reservations, String title) {
        System.out.println("\n--- " + title + " ---");

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
        } else {
            System.out.printf("%-15s %-15s %-10s %-15s %-15s %-15s%n",
                "Reservation ID", "Customer ID", "Room", "Check-in", "Check-out", "Status");
            System.out.println("-".repeat(90));
            Map<Integer, Double> roomPriceByNumber = new HashMap<>();
            for (Room room : hotelManager.getRoomService().getAllRooms()) {
                roomPriceByNumber.put(room.getRoomNumber(), room.getPricePerNight());
            }

            for (Reservation reservation : reservations) {
                double price = roomPriceByNumber.getOrDefault(reservation.getRoomNumber(), 0.0);
                long totalPrice = reservation.calculateTotalPrice(price);

                System.out.printf("%-15s %-15s %-10d %-15s %-15s %-15s (Total: $%d)%n",
                    reservation.getReservationId(),
                    reservation.getCustomerId(),
                    reservation.getRoomNumber(),
                    reservation.getCheckInDate().format(dateFormatter),
                    reservation.getCheckOutDate().format(dateFormatter),
                    reservation.getStatus().getDisplayName(),
                    totalPrice);
            }
        }
    }

    private static void checkAvailability() {
        System.out.println("\n--- Check Availability ---");

        LocalDate checkIn = getDateInput("Enter check-in date (yyyy-MM-dd): ");
        LocalDate checkOut = getDateInput("Enter check-out date (yyyy-MM-dd): ");
        if (checkIn.isAfter(checkOut)) {
            System.out.println("Error: Check-in date must be before or equal to check-out date.");
            return;
        }

        System.out.println("1. Check specific room");
        System.out.println("2. Check all available rooms");

        int choice = getIntInput("Enter your choice: ");

        switch (choice) {
            case 1 -> {
                int roomNumber = getIntInput("Enter room number: ");
                var roomService = hotelManager.getRoomService();
                if (roomService.getRoomByNumber(roomNumber).isEmpty()) {
                    System.out.println("Error: Room not found.");
                    return;
                }
                boolean available = hotelManager.getReservationService()
                    .isRoomAvailable(roomNumber, checkIn, checkOut);

                System.out.println("Room " + roomNumber + " is " +
                    (available ? "AVAILABLE" : "NOT AVAILABLE") +
                    " for the selected dates.");
            }
            case 2 -> {
                var availableRooms = hotelManager.getAvailableRoomsForDateRange(checkIn, checkOut);

                System.out.println("\nAvailable rooms for " + checkIn + " to " + checkOut + ":");
                if (availableRooms.isEmpty()) {
                    System.out.println("No rooms available for the selected dates.");
                } else {
                    printRoomTable(availableRooms, false);
                }
            }
            default -> System.out.println("Invalid choice.");
        }
    }

    private static void printRoomTable(List<Room> rooms, boolean includeAvailability) {
        if (includeAvailability) {
            System.out.printf("%-10s %-20s %-15s %-10s%n", "Room No.", "Type", "Price/Night", "Available");
            System.out.println("-".repeat(60));
            for (Room room : rooms) {
                System.out.printf("%-10d %-20s $%-14.2f %-10s%n",
                    room.getRoomNumber(),
                    room.getRoomType().getDescription(),
                    room.getPricePerNight(),
                    room.isAvailable() ? "Yes" : "No");
            }
            return;
        }

        System.out.printf("%-10s %-20s %-15s%n", "Room No.", "Type", "Price/Night");
        System.out.println("-".repeat(50));
        for (Room room : rooms) {
            System.out.printf("%-10d %-20s $%-14.2f%n",
                room.getRoomNumber(),
                room.getRoomType().getDescription(),
                room.getPricePerNight());
        }
    }

    private static Customer createCustomerFromInput(String customerId) {
        String firstName = getStringInput("Enter first name: ");
        String lastName = getStringInput("Enter last name: ");
        String email = getStringInput("Enter email: ");
        String phoneNumber = getStringInput("Enter phone number: ");
        return new Customer(customerId, firstName, lastName, email, phoneNumber);
    }

    // Input helper methods
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static double getDoubleInput(String prompt, double defaultValue) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                if (input.trim().isEmpty()) {
                    return defaultValue;
                }
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    private static String getStringInput(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private static LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String input = scanner.nextLine();
                return LocalDate.parse(input, dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format. Please use yyyy-MM-dd (e.g., 2024-12-25).");
            }
        }
    }
}


