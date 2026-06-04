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

    public HotelManager(RoomRepository roomRepository, 
                       ReservationRepository reservationRepository,
                       CustomerRepository customerRepository) {
        this.roomService = new RoomService(roomRepository);
        this.reservationService = new ReservationService(reservationRepository, roomRepository);
        this.customerRepository = customerRepository;
    }

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

    public List<Room> getAvailableRoomsForDateRange(LocalDate checkIn, LocalDate checkOut) {
        return roomService.getAllRooms().stream()
                .filter(room -> reservationService.isRoomAvailable(room.getRoomNumber(), checkIn, checkOut))
                .toList();
    }
}
