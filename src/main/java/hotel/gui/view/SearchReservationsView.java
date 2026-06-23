package hotel.gui.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hotel.gui.NavigationManager;
import hotel.model.Reservation;
import hotel.model.Room;
import hotel.service.HotelManager;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * "Search Reservations" — single-screen search with three radio-button modes
 * (by customer / by date range / by both). Mirrors the console's "Search
 * Reservations" sub-menu but keeps everything on one page (no nested
 * navigation).
 */
public class SearchReservationsView implements View {

    public static final String TITLE = "Search Reservations";

    private static final Logger LOGGER = Logger.getLogger(SearchReservationsView.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;

    private final ToggleGroup modeGroup = new ToggleGroup();
    private final RadioButton byCustomerRadio = new RadioButton("By Customer");
    private final RadioButton byDateRadio = new RadioButton("By Date Range");
    private final RadioButton byBothRadio = new RadioButton("By Customer and Date Range");

    private final TextField customerIdField = new TextField();
    private final DatePicker startDatePicker = new DatePicker();
    private final DatePicker endDatePicker = new DatePicker();

    private final ObservableList<Reservation> reservationData = FXCollections.observableArrayList();
    private final TableView<Reservation> tableView = new TableView<>();
    private final Label status = ViewUtils.statusBar();
    private final javafx.scene.control.Button searchButton
            = ViewUtils.primaryButton("Search", this::search);
    // Removed roomPriceCache; price will be fetched directly from RoomService

    public SearchReservationsView(HotelManager hotelManager, NavigationManager navigationManager) {
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

        // --- Mode radio buttons ---
        byCustomerRadio.setToggleGroup(modeGroup);
        byDateRadio.setToggleGroup(modeGroup);
        byBothRadio.setToggleGroup(modeGroup);
        byCustomerRadio.setSelected(true);
        modeGroup.selectedToggleProperty().addListener(this::onModeChanged);

        Label modeHeader = new Label("Search mode");
        modeHeader.getStyleClass().add("section-header");

        // --- Input grid ---
        customerIdField.setPromptText("e.g. C001");
        startDatePicker.setPromptText("yyyy-MM-dd");
        endDatePicker.setPromptText("yyyy-MM-dd");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Customer ID:"), 0, 0);
        grid.add(customerIdField, 1, 0);
        grid.add(new Label("Start date:"), 0, 1);
        grid.add(startDatePicker, 1, 1);
        grid.add(new Label("End date:"), 0, 2);
        grid.add(endDatePicker, 1, 2);

        // --- Results table ---
        setupTable();

        box.getChildren().addAll(
                modeHeader,
                byCustomerRadio,
                byDateRadio,
                byBothRadio,
                grid,
                ViewUtils.buttonRow(searchButton, ViewUtils.secondaryButton("Reset", this::reset)),
                status,
                tableView
        );
        VBox.setMargin(status, new Insets(8, 0, 0, 0));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        applyModeVisibility();
        return box;
    }

    private void onModeChanged(ObservableValue<? extends Toggle> obs, Toggle oldVal, Toggle newVal) {
        applyModeVisibility();
    }

    private void applyModeVisibility() {
        boolean needCustomer = byCustomerRadio.isSelected() || byBothRadio.isSelected();
        boolean needDate = byDateRadio.isSelected() || byBothRadio.isSelected();
        customerIdField.setDisable(!needCustomer);
        startDatePicker.setDisable(!needDate);
        endDatePicker.setDisable(!needDate);
        if (!needCustomer) {
            customerIdField.clear();
        }
        if (!needDate) {
            startDatePicker.setValue(null);
            startDatePicker.getEditor().clear();
            endDatePicker.setValue(null);
            endDatePicker.getEditor().clear();
        }
        searchButton.setDisable(false);
    }

    private void setupTable() {
        TableColumn<Reservation, String> idCol = new TableColumn<>("Reservation ID");
        idCol.setCellValueFactory(c -> {
            Reservation v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getReservationId());
        });
        idCol.setPrefWidth(120);

        TableColumn<Reservation, String> custCol = new TableColumn<>("Customer ID");
        custCol.setCellValueFactory(c -> {
            Reservation v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getCustomerId());
        });
        custCol.setPrefWidth(100);

        TableColumn<Reservation, Integer> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(c -> {
            Reservation v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleIntegerProperty(v.getRoomNumber()).asObject();
        });
        roomCol.setPrefWidth(70);

        TableColumn<Reservation, String> checkInCol = new TableColumn<>("Check-in");
        checkInCol.setCellValueFactory(c -> {
            Reservation v = c.getValue();
            return v == null ? null
                    : new javafx.beans.property.SimpleStringProperty(v.getCheckInDate().format(FORMATTER));
        });
        checkInCol.setPrefWidth(110);

        TableColumn<Reservation, String> checkOutCol = new TableColumn<>("Check-out");
        checkOutCol.setCellValueFactory(c -> {
            Reservation v = c.getValue();
            return v == null ? null
                    : new javafx.beans.property.SimpleStringProperty(v.getCheckOutDate().format(FORMATTER));
        });
        checkOutCol.setPrefWidth(110);

        TableColumn<Reservation, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(c -> {
            Reservation v = c.getValue();
            return v == null ? null
                    : new javafx.beans.property.SimpleStringProperty(v.getStatus().getDisplayName());
        });
        statusCol.setPrefWidth(100);

        TableColumn<Reservation, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(c -> {
            Reservation v = c.getValue();
            if (v == null) {
                return null;
            }
            return new javafx.beans.property.SimpleStringProperty(computeTotal(v));
        });
        totalCol.setCellFactory(tc -> new TableCell<Reservation, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? "" : value);
            }
        });
        totalCol.setPrefWidth(90);

        tableView.getColumns().addAll(idCol, custCol, roomCol, checkInCol, checkOutCol, statusCol, totalCol);
        tableView.setItems(reservationData);
    }

    private String computeTotal(Reservation reservation) {
        // Directly retrieve the room price for the reservation's room number.
        double price = hotelManager.getRoomService()
                .getRoomByNumber(reservation.getRoomNumber())
                .map(Room::getPricePerNight)
                .orElse(0.0);
        return "$" + reservation.calculateTotalPrice(price);
    }

    private void search() {
        try {
            List<Reservation> results;
            String title;
            if (byCustomerRadio.isSelected()) {
                String cid = customerIdField.getText().trim();
                if (cid.isEmpty()) {
                    ViewUtils.setStatus(status, "Please enter a customer ID.", ViewUtils.StatusKind.WARNING);
                    return;
                }
                results = hotelManager.getReservationService().getReservationsByCustomer(cid);
                title = "Reservations for Customer: " + cid;
            } else if (byDateRadio.isSelected()) {
                LocalDate start = readDate(startDatePicker, "start");
                LocalDate end = readDate(endDatePicker, "end");
                if (start == null || end == null) {
                    return;
                }
                if (start.isAfter(end)) {
                    ViewUtils.setStatus(status, "Start date must be on or before end date.",
                            ViewUtils.StatusKind.ERROR);
                    return;
                }
                results = hotelManager.getReservationService().getReservationsByDateRange(start, end);
                title = "Reservations between " + start.format(FORMATTER) + " and " + end.format(FORMATTER);
            } else { // by both
                String cid = customerIdField.getText().trim();
                LocalDate start = readDate(startDatePicker, "start");
                LocalDate end = readDate(endDatePicker, "end");
                if (cid.isEmpty() || start == null || end == null) {
                    ViewUtils.setStatus(status, "Please fill in customer ID, start, and end date.",
                            ViewUtils.StatusKind.WARNING);
                    return;
                }
                if (start.isAfter(end)) {
                    ViewUtils.setStatus(status, "Start date must be on or before end date.",
                            ViewUtils.StatusKind.ERROR);
                    return;
                }
                List<Reservation> all = hotelManager.getReservationService().getReservationsByCustomer(cid);
                results = all.stream()
                        .filter(r -> r.overlapsWith(start, end))
                        .toList();
                title = "Reservations for Customer: " + cid + " between "
                        + start.format(FORMATTER) + " and " + end.format(FORMATTER);
            }
            reservationData.setAll(results);
            ViewUtils.setStatus(status,
                    title + " — " + results.size() + " reservation(s) found.",
                    ViewUtils.StatusKind.INFO);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Search reservations failed", e);
            ViewUtils.showError(status, e, LOGGER);
        }
    }

    private LocalDate readDate(DatePicker picker, String label) {
        LocalDate date = picker.getValue();
        if (date == null) {
            ViewUtils.setStatus(status, "Please select a " + label + " date.", ViewUtils.StatusKind.WARNING);
            return null;
        }
        return date;
    }

    private void reset() {
        customerIdField.clear();
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        reservationData.clear();
        ViewUtils.setStatus(status, null, ViewUtils.StatusKind.INFO);
        byCustomerRadio.setSelected(true);
    }
}
