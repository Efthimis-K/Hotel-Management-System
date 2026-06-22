package hotel.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import hotel.exception.DuplicateResourceException;
import hotel.exception.ResourceNotFoundException;
import hotel.exception.StorageException;
import hotel.model.Customer;
import hotel.storage.DatabaseManager;

public class CustomerRepository {

    /**
     * Inserts a new customer record into the database.
     *
     * @throws DuplicateResourceException if a customer with the same ID already exists
     * @throws StorageException if a database error occurs
     */
    public void addCustomer(Customer customer) {
        String sql = "INSERT INTO customers (customer_id, first_name, last_name, email, phone_number) VALUES (?, ?, ?, ?, ?)";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getCustomerId());
            stmt.setString(2, customer.getFirstName());
            stmt.setString(3, customer.getLastName());
            stmt.setString(4, customer.getEmail());
            stmt.setString(5, customer.getPhoneNumber());
            stmt.executeUpdate();
        } catch (SQLException e) {
            if (e.getSQLState() != null && e.getSQLState().contains("SQLITE_CONSTRAINT_UNIQUE")) {
                throw DuplicateResourceException.forResource("Customer", "ID", customer.getCustomerId());
            }
            throw StorageException.forFile("data/customers.json", e);
        }
    }

    /**
     * Retrieves all customers from the database.
     *
     * @return a list of all customers
     * @throws StorageException if a database error occurs
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT customer_id, first_name, last_name, email, phone_number FROM customers";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/customers.json", e);
        }
        return customers;
    }

    /**
     * Retrieves a customer by their ID.
     *
     * @return an Optional containing the customer if found, otherwise an empty Optional
     */
    public Optional<Customer> getCustomerById(String customerId) {
        if (customerId == null) {
            return Optional.empty();
        }
        String sql = "SELECT customer_id, first_name, last_name, email, phone_number FROM customers WHERE customer_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/customers.json", e);
        }
        return Optional.empty();
    }

    /**
     * Updates a customer's information in the database.
     *
     * @param customer the customer object containing updated information
     * @throws ResourceNotFoundException if no customer with the specified ID exists
     * @throws StorageException if a database error occurs
     */
    public void updateCustomer(Customer customer) {
        String sql = "UPDATE customers SET first_name = ?, last_name = ?, email = ?, phone_number = ? WHERE customer_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customer.getFirstName());
            stmt.setString(2, customer.getLastName());
            stmt.setString(3, customer.getEmail());
            stmt.setString(4, customer.getPhoneNumber());
            stmt.setString(5, customer.getCustomerId());
            int updatedRows = stmt.executeUpdate();
            if (updatedRows == 0) {
                throw ResourceNotFoundException.forResource("Customer", "ID", customer.getCustomerId());
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/customers.json", e);
        }
    }

    /**
     * Deletes a customer from the database.
     *
     * @param customerId the ID of the customer to delete
     * @throws ResourceNotFoundException if no customer with the specified ID exists
     * @throws StorageException if a database error occurs during deletion
     */
    public void deleteCustomer(String customerId) {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        Connection conn = DatabaseManager.getInstance().getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerId);
            int deletedRows = stmt.executeUpdate();
            if (deletedRows == 0) {
                throw ResourceNotFoundException.forResource("Customer", "ID", customerId);
            }
        } catch (SQLException e) {
            throw StorageException.forFile("data/customers.json", e);
        }
    }

    /**
     * Creates a Customer object from the current row of a ResultSet.
     *
     * @param rs the ResultSet positioned at a customer row
     * @return a Customer object with values from the current row
     * @throws SQLException if an error occurs while reading from the ResultSet
     */
    private Customer mapCustomer(ResultSet rs) throws SQLException {
        return new Customer(
                rs.getString("customer_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("phone_number")
        );
    }
}
