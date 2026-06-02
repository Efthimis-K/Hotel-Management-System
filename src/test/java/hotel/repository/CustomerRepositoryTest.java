package hotel.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import hotel.model.Customer;

class CustomerRepositoryTest {

    @Test
    void customerFullNameIsComputedProperty() {
        Customer customer = new Customer("CUST-001", "John", "Doe", "john.doe@example.com", "+1234567890");
        
        assertEquals("John Doe", customer.getFullName());
        assertEquals("John", customer.getFirstName());
        assertEquals("Doe", customer.getLastName());
    }

    @Test
    void customerCanBeCreatedWithAllFields() {
        Customer customer = new Customer("CUST-002", "Jane", "Smith", "jane.smith@example.com", "+0987654321");
        
        assertNotNull(customer);
        assertEquals("CUST-002", customer.getCustomerId());
        assertEquals("Jane", customer.getFirstName());
        assertEquals("Smith", customer.getLastName());
        assertEquals("jane.smith@example.com", customer.getEmail());
        assertEquals("+0987654321", customer.getPhoneNumber());
    }
}
