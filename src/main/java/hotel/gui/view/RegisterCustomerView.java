package hotel.gui.view;

import java.util.logging.Level;
import java.util.logging.Logger;

import hotel.exception.HotelException;
import hotel.gui.NavigationManager;
import hotel.model.Customer;
import hotel.service.HotelManager;
import hotel.util.ValidationUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * "Register Customer" — inline form mirroring the console's "Register Customer"
 * operation. Validates email and phone format live; submit is disabled until
 * inputs are valid.
 */
public class RegisterCustomerView implements View {

    public static final String TITLE = "Register Customer";

    private static final Logger LOGGER = Logger.getLogger(RegisterCustomerView.class.getName());

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final TextField customerIdField = new TextField();
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final Label status = ViewUtils.statusBar();
    private final javafx.scene.control.Button submitButton
            = ViewUtils.primaryButton("Register", this::submit);

    public RegisterCustomerView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    @Override
    public Node getView() {
        return root;
    }

    private VBox buildView() {
        VBox box = ViewUtils.formContainer(TITLE);

        customerIdField.setPromptText("e.g. C001");
        firstNameField.setPromptText("First name");
        lastNameField.setPromptText("Last name");
        emailField.setPromptText("name@example.com");
        phoneField.setPromptText("+1234567890");

        // Live validation.
        customerIdField.textProperty().addListener((o, a, b) -> updateSubmitState());
        firstNameField.textProperty().addListener((o, a, b) -> updateSubmitState());
        lastNameField.textProperty().addListener((o, a, b) -> updateSubmitState());
        emailField.textProperty().addListener((o, a, b) -> updateSubmitState());
        phoneField.textProperty().addListener((o, a, b) -> updateSubmitState());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Customer ID:"), 0, 0);
        grid.add(customerIdField, 1, 0);
        grid.add(new Label("First Name:"), 0, 1);
        grid.add(firstNameField, 1, 1);
        grid.add(new Label("Last Name:"), 0, 2);
        grid.add(lastNameField, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Phone:"), 0, 4);
        grid.add(phoneField, 1, 4);

        box.getChildren().addAll(
                grid,
                ViewUtils.buttonRow(submitButton, ViewUtils.secondaryButton("Clear", this::clear)),
                status
        );
        VBox.setMargin(status, new Insets(8, 0, 0, 0));

        updateSubmitState();
        return box;
    }

    private void updateSubmitState() {
        submitButton.setDisable(!isValid());
    }

    private boolean isValid() {
        if (customerIdField.getText().isBlank()) {
            return false;
        }
        if (firstNameField.getText().isBlank()) {
            return false;
        }
        if (lastNameField.getText().isBlank()) {
            return false;
        }
        if (!ValidationUtils.getEmailValidator()
                .isValid(emailField.getText().trim())) {
            return false;
        }
        if (!ValidationUtils.getPhonePattern().matcher(phoneField.getText().trim()).matches()) {
            return false;
        }
        return true;
    }

    private void submit() {
        String id = customerIdField.getText().trim();
        try {
            if (hotelManager.getCustomerById(id).isPresent()) {
                ViewUtils.setStatus(status,
                        "Customer with ID '" + id + "' already exists.",
                        ViewUtils.StatusKind.ERROR);
                return;
            }
            Customer customer = new Customer(
                    id,
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    emailField.getText().trim(),
                    phoneField.getText().trim()
            );
            hotelManager.registerCustomer(customer);
            LOGGER.log(Level.INFO, "Customer registered via GUI: {0}", customer);
            ViewUtils.setStatus(status,
                    "Customer registered successfully! ID: " + customer.getCustomerId()
                    + " (" + customer.getFullName() + ").",
                    ViewUtils.StatusKind.SUCCESS);
            clear();
        } catch (HotelException e) {
            ViewUtils.showError(status, e, LOGGER);
        }
    }

    private void clear() {
        customerIdField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        customerIdField.requestFocus();
    }
}
