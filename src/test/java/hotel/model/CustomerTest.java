package hotel.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import hotel.exception.ValidationException;

class CustomerTest {

    @Test
    void constructorAcceptsValidCustomerData() {
        Customer customer = new Customer("CUST-1", "Ada", "Lovelace", "ada@example.com", "+12345678901");

        assertEquals("Ada Lovelace", customer.getFullName());
        assertEquals("ada@example.com", customer.getEmail());
        assertEquals("+12345678901", customer.getPhoneNumber());
    }

    @Test
    void setEmailRejectsInvalidFormat() {
        Customer customer = new Customer();

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> customer.setEmail("not-an-email")
        );

        assertEquals("Invalid email format", exception.getMessage());
    }

    @Test
    void setPhoneNumberRejectsInvalidFormat() {
        Customer customer = new Customer();

        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> customer.setPhoneNumber("1234")
        );

        assertEquals("Invalid phone number format", exception.getMessage());
    }

    @Test
    void setCustomerIdRejectsNull() {
        Customer customer = new Customer();
        assertThrows(ValidationException.class, () -> customer.setCustomerId(null));
    }

    @Test
    void setCustomerIdRejectsBlank() {
        Customer customer = new Customer();
        assertThrows(ValidationException.class, () -> customer.setCustomerId("   "));
    }

    @Test
    void setFirstNameRejectsBlank() {
        Customer customer = new Customer();
        assertThrows(ValidationException.class, () -> customer.setFirstName(""));
    }

    @Test
    void setLastNameRejectsNull() {
        Customer customer = new Customer();
        assertThrows(ValidationException.class, () -> customer.setLastName(null));
    }

    @Test
    void validationExceptionIsAlsoHotelException() {
        Customer customer = new Customer();
        ValidationException ex = assertThrows(
            ValidationException.class,
            () -> customer.setEmail("bad")
        );
        assertTrue(ex instanceof hotel.exception.HotelException);
    }
}
