package hotel.gui.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import hotel.exception.HotelException;
import hotel.gui.NavigationManager;
import hotel.model.Customer;
import hotel.model.Reservation;
import hotel.model.Room;
import hotel.service.HotelManager;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * "Create Reservation" — inline form mirroring the console's "Create
 * Reservation" operation. If the supplied customer ID does not exist, an inline
 * "register new customer" sub-form is revealed, matching the console's
 * "Customer not found. Registering a new customer" flow.
 * <p>
 * The total price is shown live once a room and date range are entered.
 */
public class CreateReservationView implements View {

    public static final String TITLE = "Create Reservation";

    private static final Logger LOGGER = Logger.getLogger(CreateReservationView.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{7,15}$");

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;

    private final TextField customerIdField = new TextField();
    private final Label customerStatus = new Label();
    private final TextField firstNameField = new TextField();
    private final TextField lastNameField = new TextField();
    private final TextField emailField = new TextField();
    private final TextField phoneField = new TextField();
    private final GridPane newCustomerGrid = new GridPane();
    private final TextField roomNumberField = new TextField();
    private final DatePicker checkInPicker = new DatePicker();
    private final DatePicker checkOutPicker = new DatePicker();
    private final Label totalPriceLabel = new Label();
    private final Label status = ViewUtils.statusBar();
    private final javafx.scene.control.Button submitButton
            = ViewUtils.primaryButton("Create Reservation", this::submit);

    private boolean newCustomerMode = false;

    public CreateReservationView(HotelManager hotelManager, NavigationManager navigationManager) {
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

        // --- Customer ID with live lookup ---
        customerIdField.setPromptText("e.g. C001");
        customerIdField.textProperty().addListener((o, a, b) -> onCustomerIdChanged());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Customer ID:"), 0, 0);
        grid.add(customerIdField, 1, 0);
        grid.add(customerStatus, 1, 1);

        // --- Inline "register new customer" form (initially hidden) ---
        firstNameField.setPromptText("First name");
        lastNameField.setPromptText("Last name");
        emailField.setPromptText("name@example.com");
        phoneField.setPromptText("+1234567890");
        firstNameField.textProperty().addListener((o, a, b) -> updateSubmitState());
        lastNameField.textProperty().addListener((o, a, b) -> updateSubmitState());
        emailField.textProperty().addListener((o, a, b) -> updateSubmitState());
        phoneField.textProperty().addListener((o, a, b) -> updateSubmitState());

        newCustomerGrid.setHgap(10);
        newCustomerGrid.setVgap(10);
        newCustomerGrid.add(new Label("First Name:"), 0, 0);
        newCustomerGrid.add(firstNameField, 1, 0);
        newCustomerGrid.add(new Label("Last Name:"), 0, 1);
        newCustomerGrid.add(lastNameField, 1, 1);
        newCustomerGrid.add(new Label("Email:"), 0, 2);
        newCustomerGrid.add(emailField, 1, 2);
        newCustomerGrid.add(new Label("Phone:"), 0, 3);
        newCustomerGrid.add(phoneField, 1, 3);
        newCustomerGrid.setVisible(false);
        newCustomerGrid.setManaged(false);

        // --- Room and dates ---
        roomNumberField.setPromptText("e.g. 101");
        checkInPicker.setPromptText("yyyy-MM-dd");
        checkInPicker.setPrefWidth(240);
        checkOutPicker.setPromptText("yyyy-MM-dd");
        checkOutPicker.setPrefWidth(240);

        roomNumberField.textProperty().addListener((o, a, b) -> updateTotalPrice());
        checkInPicker.editorProperty().get().textProperty()
                .addListener((o, a, b) -> updateTotalPrice());
        checkOutPicker.editorProperty().get().textProperty()
                .addListener((o, a, b) -> updateTotalPrice());

        GridPane reservationGrid = new GridPane();
        reservationGrid.setHgap(10);
        reservationGrid.setVgap(10);
        reservationGrid.add(new Label("Room Number:"), 0, 0);
        reservationGrid.add(roomNumberField, 1, 0);
        reservationGrid.add(new Label("Check-in date:"), 0, 1);
        reservationGrid.add(checkInPicker, 1, 1);
        reservationGrid.add(new Label("Check-out date:"), 0, 2);
        reservationGrid.add(checkOutPicker, 1, 2);
        reservationGrid.add(new Label("Total price:"), 0, 3);
        reservationGrid.add(totalPriceLabel, 1, 3);

        totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        box.getChildren().addAll(
                grid,
                new Separator(),
                newCustomerGrid,
                new Separator(),
                reservationGrid,
                ViewUtils.buttonRow(submitButton, ViewUtils.secondaryButton("Reset", this::reset)),
                status
        );
        VBox.setMargin(status, new Insets(8, 0, 0, 0));
        updateSubmitState();
        return box;
    }

    private void onCustomerIdChanged() {
        String id = customerIdField.getText().trim();
        if (id.isEmpty()) {
            customerStatus.setText("");
            setNewCustomerMode(false);
            updateSubmitState();
            return;
        }
        Optional<Customer> existing = hotelManager.getCustomerById(id);
        if (existing.isPresent()) {
            customerStatus.setText("Customer found: " + existing.get().getFullName());
            customerStatus.setStyle("-fx-text-fill: #1e8449;");
            setNewCustomerMode(false);
        } else {
            customerStatus.setText("Customer not found. Fill the new-customer details below to register.");
            customerStatus.setStyle("-fx-text-fill: #b7950b;");
            setNewCustomerMode(true);
        }
        updateSubmitState();
    }

    private void setNewCustomerMode(boolean enabled) {
        newCustomerMode = enabled;
        newCustomerGrid.setVisible(enabled);
        newCustomerGrid.setManaged(enabled);
        if (enabled) {
            firstNameField.requestFocus();
        }
    }

    private void updateTotalPrice() {
        totalPriceLabel.setText(computeTotalPriceText());
    }

    private String computeTotalPriceText() {
        try {
            int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();
            if (checkIn != null && checkOut != null && checkOut.isAfter(checkIn)) {
                Optional<Room> room = hotelManager.getRoomService().getRoomByNumber(roomNumber);
                if (room.isPresent()) {
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                    long total = (long) (nights * room.get().getPricePerNight());
                    return String.format("$%d (%d night%s @ $%.2f/night)",
                            total, nights, nights == 1 ? "" : "s", room.get().getPricePerNight());
                }
                return "(room " + roomNumber + " not found)";
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        return "(enter room number and valid dates)";
    }

    private void updateSubmitState() {
        boolean baseValid = !customerIdField.getText().isBlank()
                && !roomNumberField.getText().isBlank();
        if (baseValid) {
            try {
                Integer.parseInt(roomNumberField.getText().trim());
            } catch (NumberFormatException e) {
                baseValid = false;
            }
        }
        if (newCustomerMode) {
            baseValid = baseValid
                    && !firstNameField.getText().isBlank()
                    && !lastNameField.getText().isBlank()
                    && org.apache.commons.validator.routines.EmailValidator.getInstance()
                            .isValid(emailField.getText().trim())
                    && PHONE_PATTERN.matcher(phoneField.getText().trim()).matches();
        }
        submitButton.setDisable(!baseValid);
    }

    private void submit() {
        try {
            String id = customerIdField.getText().trim();
            if (newCustomerMode) {
                Customer customer = new Customer(
                        id,
                        firstNameField.getText().trim(),
                        lastNameField.getText().trim(),
                        emailField.getText().trim(),
                        phoneField.getText().trim()
                );
                hotelManager.registerCustomer(customer);
                LOGGER.log(Level.INFO, "Auto-registered new customer during reservation: {0}", customer);
            } else if (hotelManager.getCustomerById(id).isEmpty()) {
                ViewUtils.setStatus(status,
                        "Customer with ID '" + id + "' was not found. Please register them first.",
                        ViewUtils.StatusKind.ERROR);
                return;
            }

            int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
            LocalDate checkIn = checkInPicker.getValue();
            LocalDate checkOut = checkOutPicker.getValue();
            if (checkIn == null || checkOut == null) {
                ViewUtils.setStatus(status,
                        "Please select check-in and check-out dates.",
                        ViewUtils.StatusKind.ERROR);
                return;
            }

            Reservation reservation = hotelManager.getReservationService()
                    .createReservation(id, roomNumber, checkIn, checkOut);

            Optional<Room> room = hotelManager.getRoomService().getRoomByNumber(roomNumber);
            long total = room.map(r -> reservation.calculateTotalPrice(r.getPricePerNight())).orElse(0L);

            ViewUtils.setStatus(status,
                    String.format("Reservation created successfully! ID: %s, Room %d, %s to %s, Total: $%d",
                            reservation.getReservationId(),
                            roomNumber,
                            checkIn.format(FORMATTER),
                            checkOut.format(FORMATTER),
                            total),
                    ViewUtils.StatusKind.SUCCESS);
            reset();
        } catch (HotelException e) {
            ViewUtils.showError(status, e, LOGGER);
        } catch (NumberFormatException e) {
            ViewUtils.setStatus(status,
                    "Please enter a valid room number.",
                    ViewUtils.StatusKind.ERROR);
        }
    }

    private void reset() {
        customerIdField.clear();
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        roomNumberField.clear();
        checkInPicker.getEditor().clear();
        checkOutPicker.getEditor().clear();
        setNewCustomerMode(false);
        customerStatus.setText("");
        totalPriceLabel.setText("");
        customerIdField.requestFocus();
    }
}
