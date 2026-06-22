package hotel.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import hotel.model.Customer;
import hotel.model.Room;
import hotel.repository.CustomerRepository;
import hotel.repository.ReservationRepository;
import hotel.repository.RoomRepository;

public class HotelManager {
    private final RoomService roomService;
    private final ReservationService reservationService;
    private final CustomerRepository customerRepository;

    /**
     * Constructs a HotelManager with the specified repositories.
     *
     * @param roomRepository        the repository for room data
     * @param reservationRepository the repository for reservation data
     * @param customerRepository    the repository for customer data
     */
    public HotelManager(RoomRepository roomRepository, 
                       ReservationRepository reservationRepository,
                       CustomerRepository customerRepository) {
        this.roomService = new RoomService(roomRepository);
        this.reservationService = new ReservationService(reservationRepository, roomRepository);
        this.customerRepository = customerRepository;
    }

    /**
     * Provides access to the room service.
     *
     * @return the room service
     */
    public RoomService getRoomService() {
        return roomService;
    }

    public ReservationService getReservationService() {
        return reservationService;
    }

    public CustomerRepository getCustomerRepository() {
        return customerRepository;
    }

    public void registerCustomer(Customer customer) {
        customerRepository.addCustomer(customer);
    }

    public Optional<Customer> getCustomerById(String customerId) {
        return customerRepository.getCustomerById(customerId);
    }

    public List<Customer> getAllCustomers() {
        return customerRepository.getAllCustomers();
    }

    /**
     * Retrieves all available rooms for the specified date range.
     *
     * @param checkIn  the check-in date
     * @param checkOut the check-out date
     * @return         a list of rooms available for the specified date range
     */
    public List<Room> getAvailableRoomsForDateRange(LocalDate checkIn, LocalDate checkOut) {
        return roomService.getAllRooms().stream()
                .filter(room -> reservationService.isRoomAvailable(room.getRoomNumber(), checkIn, checkOut))
                .toList();
    }
}
