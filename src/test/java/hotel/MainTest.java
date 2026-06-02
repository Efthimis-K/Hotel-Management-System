package hotel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Scanner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import hotel.exception.ResourceNotFoundException;
import hotel.exception.ValidationException;
import hotel.model.Customer;
import hotel.model.Reservation;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.service.HotelManager;
import hotel.service.ReservationService;
import hotel.service.RoomService;

class MainTest {

    private PrintStream originalOut;
    private Object originalScanner;
    private Object originalHotelManager;
    private ByteArrayOutputStream output;

    @BeforeEach
    void setUp() throws Exception {
        originalOut = System.out;
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output, true, StandardCharsets.UTF_8));

        originalScanner = getMainField("scanner");
        originalHotelManager = getMainField("hotelManager");
    }

    @AfterEach
    void tearDown() throws Exception {
        System.setOut(originalOut);
        setMainField("scanner", originalScanner);
        setMainField("hotelManager", originalHotelManager);
    }

    @Test
    void createReservationContinuesWhenCustomerAlreadyExists() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(3);
        LocalDate checkOut = checkIn.plusDays(2);
        HotelManager hotelManager = mock(HotelManager.class);
        RoomService roomService = mock(RoomService.class);
        ReservationService reservationService = mock(ReservationService.class);
        Room room = new Room(101, RoomType.SINGLE, 80.0);
        Reservation reservation = new Reservation("RES-EXIST", "CUST-1", 101, checkIn, checkOut);

        when(hotelManager.getCustomerById("CUST-1"))
            .thenReturn(Optional.of(new Customer("CUST-1", "Ada", "Lovelace", "ada@example.com", "+12345678901")));
        when(hotelManager.getRoomService()).thenReturn(roomService);
        when(hotelManager.getReservationService()).thenReturn(reservationService);
        when(roomService.getRoomByNumber(101)).thenReturn(Optional.of(room));
        when(reservationService.createReservation("CUST-1", 101, checkIn, checkOut)).thenReturn(reservation);

        setMainField("hotelManager", hotelManager);
        setMainField("scanner", scannerFor(
            "CUST-1",
            "101",
            checkIn.toString(),
            checkOut.toString()
        ));

        invokeCreateReservation();

        verify(hotelManager, never()).registerCustomer(any());
        verify(reservationService).createReservation("CUST-1", 101, checkIn, checkOut);
        assertTrue(getOutput().contains("Reservation created successfully!"));
    }

    @Test
    void createReservationRegistersMissingCustomerThenCreatesReservation() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(4);
        LocalDate checkOut = checkIn.plusDays(3);
        HotelManager hotelManager = mock(HotelManager.class);
        RoomService roomService = mock(RoomService.class);
        ReservationService reservationService = mock(ReservationService.class);
        Room room = new Room(202, RoomType.DOUBLE, 110.0);
        Reservation reservation = new Reservation("RES-NEW", "CUST-NEW", 202, checkIn, checkOut);

        when(hotelManager.getCustomerById("CUST-NEW")).thenReturn(Optional.empty());
        when(hotelManager.getRoomService()).thenReturn(roomService);
        when(hotelManager.getReservationService()).thenReturn(reservationService);
        when(roomService.getRoomByNumber(202)).thenReturn(Optional.of(room));
        when(reservationService.createReservation("CUST-NEW", 202, checkIn, checkOut)).thenReturn(reservation);

        setMainField("hotelManager", hotelManager);
        setMainField("scanner", scannerFor(
            "CUST-NEW",
            "Grace",
            "Hopper",
            "grace@example.com",
            "+10987654321",
            "202",
            checkIn.toString(),
            checkOut.toString()
        ));

        invokeCreateReservation();

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(hotelManager).registerCustomer(customerCaptor.capture());
        verify(reservationService).createReservation("CUST-NEW", 202, checkIn, checkOut);

        Customer savedCustomer = customerCaptor.getValue();
        assertEquals("CUST-NEW", savedCustomer.getCustomerId());
        assertEquals("Grace", savedCustomer.getFirstName());
        assertEquals("Hopper", savedCustomer.getLastName());
        assertEquals("grace@example.com", savedCustomer.getEmail());
        assertEquals("+10987654321", savedCustomer.getPhoneNumber());
        assertTrue(getOutput().contains("Customer registered successfully! Continuing with reservation."));
    }

    @Test
    void createReservationStopsWhenInlineCustomerDataIsInvalid() throws Exception {
        HotelManager hotelManager = mock(HotelManager.class);

        when(hotelManager.getCustomerById("CUST-BAD")).thenReturn(Optional.empty());

        setMainField("hotelManager", hotelManager);
        setMainField("scanner", scannerFor(
            "CUST-BAD",
            "Alan",
            "Turing",
            "not-an-email",
            "+12345678901"
        ));

        invokeCreateReservation();

        verify(hotelManager, never()).registerCustomer(any());
        verify(hotelManager, never()).getRoomService();
        assertTrue(getOutput().contains("Invalid input: Invalid email format"),
            "Output should contain the validation message: " + getOutput());
    }

    @Test
    void createReservationStillRejectsMissingRoomAfterCustomerCreation() throws Exception {
        HotelManager hotelManager = mock(HotelManager.class);
        RoomService roomService = mock(RoomService.class);

        when(hotelManager.getCustomerById("CUST-ROOM")).thenReturn(Optional.empty());
        when(hotelManager.getRoomService()).thenReturn(roomService);
        when(roomService.getRoomByNumber(404)).thenReturn(Optional.empty());

        setMainField("hotelManager", hotelManager);
        setMainField("scanner", scannerFor(
            "CUST-ROOM",
            "Katherine",
            "Johnson",
            "katherine@example.com",
            "+12345678902",
            "404"
        ));

        invokeCreateReservation();

        verify(hotelManager).registerCustomer(any(Customer.class));
        verify(hotelManager, never()).getReservationService();
        assertTrue(getOutput().contains("Error: Room not found."));
    }

    @Test
    void createReservationStillShowsReservationValidationErrorsAfterCustomerLookup() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(6);
        LocalDate checkOut = checkIn.plusDays(2);
        HotelManager hotelManager = mock(HotelManager.class);
        RoomService roomService = mock(RoomService.class);
        ReservationService reservationService = mock(ReservationService.class);
        Room room = new Room(303, RoomType.SUITE, 180.0);

        when(hotelManager.getCustomerById("CUST-VALID"))
            .thenReturn(Optional.of(new Customer("CUST-VALID", "Margaret", "Hamilton", "margaret@example.com", "+12345678903")));
        when(hotelManager.getRoomService()).thenReturn(roomService);
        when(hotelManager.getReservationService()).thenReturn(reservationService);
        when(roomService.getRoomByNumber(303)).thenReturn(Optional.of(room));
        when(reservationService.createReservation("CUST-VALID", 303, checkIn, checkOut))
            .thenThrow(new ValidationException("Room is not available for the selected dates"));

        setMainField("hotelManager", hotelManager);
        setMainField("scanner", scannerFor(
            "CUST-VALID",
            "303",
            checkIn.toString(),
            checkOut.toString()
        ));

        invokeCreateReservation();

        verify(hotelManager, never()).registerCustomer(any());
        verify(reservationService).createReservation(eq("CUST-VALID"), eq(303), eq(checkIn), eq(checkOut));
        assertTrue(getOutput().contains("Invalid input: Room is not available for the selected dates"),
            "Output should contain validation prefix: " + getOutput());
    }

    @Test
    void createReservationReportsResourceNotFoundCleanly() throws Exception {
        LocalDate checkIn = LocalDate.now().plusDays(6);
        LocalDate checkOut = checkIn.plusDays(2);
        HotelManager hotelManager = mock(HotelManager.class);
        RoomService roomService = mock(RoomService.class);
        ReservationService reservationService = mock(ReservationService.class);
        Room room = new Room(404, RoomType.SINGLE, 80.0);

        when(hotelManager.getCustomerById("CUST-NF"))
            .thenReturn(Optional.of(new Customer("CUST-NF", "Test", "User", "test@example.com", "+12345678905")));
        when(hotelManager.getRoomService()).thenReturn(roomService);
        when(hotelManager.getReservationService()).thenReturn(reservationService);
        when(roomService.getRoomByNumber(404)).thenReturn(Optional.of(room));
        when(reservationService.createReservation("CUST-NF", 404, checkIn, checkOut))
            .thenThrow(ResourceNotFoundException.forResource("Room", "number", 404));

        setMainField("hotelManager", hotelManager);
        setMainField("scanner", scannerFor(
            "CUST-NF",
            "404",
            checkIn.toString(),
            checkOut.toString()
        ));

        invokeCreateReservation();

        assertTrue(getOutput().contains("Room with number 404 not found"),
            "Output should contain not-found message: " + getOutput());
    }

    private static Scanner scannerFor(String... lines) {
        String input = String.join(System.lineSeparator(), lines) + System.lineSeparator();
        return new Scanner(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }

    private void invokeCreateReservation() throws Exception {
        Method method = Main.class.getDeclaredMethod("createReservation");
        method.setAccessible(true);
        method.invoke(null);
    }

    private Object getMainField(String fieldName) throws Exception {
        Field field = Main.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(null);
    }

    private void setMainField(String fieldName, Object value) throws Exception {
        Field field = Main.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(null, value);
    }

    private String getOutput() {
        return output.toString(StandardCharsets.UTF_8);
    }
}
