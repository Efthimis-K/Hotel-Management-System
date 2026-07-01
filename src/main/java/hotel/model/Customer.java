package hotel.model;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import hotel.exception.ValidationException;
import hotel.util.ValidationUtils;

public class Customer {

    private String customerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    public Customer() {
    }

    public Customer(String customerId, String firstName, String lastName, String email, String phoneNumber) {
        setCustomerId(customerId);
        setFirstName(firstName);
        setLastName(lastName);
        setEmail(email);
        setPhoneNumber(phoneNumber);
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            throw ValidationException.forField("customerId", "must not be null or blank");
        }
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw ValidationException.forField("firstName", "must not be null or blank");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw ValidationException.forField("lastName", "must not be null or blank");
        }
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || !ValidationUtils.getEmailValidator().isValid(email)) {
            throw new ValidationException("Invalid email format");
        }
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || !ValidationUtils.getPhonePattern().matcher(phoneNumber).matches()) {
            throw new ValidationException("Invalid phone number format");
        }
        this.phoneNumber = phoneNumber;
    }

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Customer customer = (Customer) o;
        return Objects.equals(customerId, customer.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId);
    }

    @Override
    public String toString() {
        return "Customer{"
                + "customerId='" + customerId + '\''
                + ", firstName='" + firstName + '\''
                + ", lastName='" + lastName + '\''
                + ", email='" + email + '\''
                + ", phoneNumber='" + phoneNumber + '\''
                + '}';
    }
}
