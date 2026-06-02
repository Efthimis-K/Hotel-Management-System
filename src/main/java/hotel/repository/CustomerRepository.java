package hotel.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hotel.exception.DuplicateResourceException;
import hotel.exception.ResourceNotFoundException;
import hotel.model.Customer;
import hotel.util.JsonFileHandler;

public class CustomerRepository {
    private static final String FILE_PATH = "data/customers.json";
    private List<Customer> customers;

    public CustomerRepository() {
        loadCustomers();
    }

    private void loadCustomers() {
        customers = JsonFileHandler.loadFromFile(FILE_PATH, Customer.class);
        if (customers == null) {
            customers = new ArrayList<>();
        }
    }

    private void saveCustomers() {
        JsonFileHandler.saveToFile(customers, FILE_PATH);
    }

    public void addCustomer(Customer customer) {
        if (customers.stream().anyMatch(c -> c.getCustomerId().equals(customer.getCustomerId()))) {
            throw DuplicateResourceException.forResource("Customer", "ID", customer.getCustomerId());
        }
        customers.add(customer);
        saveCustomers();
    }

    public List<Customer> getAllCustomers() {
        return new ArrayList<>(customers);
    }

    public Optional<Customer> getCustomerById(String customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        return customers.stream().filter(c -> c.getCustomerId().equals(customerId)).findFirst();
    }

    public void updateCustomer(Customer customer) {
        boolean removed = customers.removeIf(c -> c.getCustomerId().equals(customer.getCustomerId()));
        if (!removed) {
            throw ResourceNotFoundException.forResource("Customer", "ID", customer.getCustomerId());
        }
        customers.add(customer);
        saveCustomers();
    }

    public void deleteCustomer(String customerId) {
        boolean removed = customers.removeIf(c -> c.getCustomerId().equals(customerId));
        if (!removed) {
            throw ResourceNotFoundException.forResource("Customer", "ID", customerId);
        }
        saveCustomers();
    }
}
