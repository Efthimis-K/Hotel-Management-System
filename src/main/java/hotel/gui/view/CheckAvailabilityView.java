package hotel.gui.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hotel.gui.NavigationManager;
import hotel.model.Room;
import hotel.service.HotelManager;
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
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * "Check Availability" — pick a date range and then check either a specific
 * room or all available rooms. Mirrors the console's "Check Availability"
 * operation. The result is shown in the same view (no new window is opened).
 */
public class CheckAvailabilityView implements View {

    public static final String TITLE = "Check Availability";

    private static final Logger LOGGER = Logger.getLogger(CheckAvailabilityView.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;

    private final DatePicker checkInPicker = new DatePicker();
    private final DatePicker checkOutPicker = new DatePicker();
    private final ToggleGroup modeGroup = new ToggleGroup();
    private final RadioButton specificRoomRadio = new RadioButton("Check specific room");
    private final RadioButton allRoomsRadio = new RadioButton("Check all available rooms");
    private final TextField roomNumberField = new TextField();
    private final Label resultLabel = new Label();
    private final ObservableList<Room> roomData = FXCollections.observableArrayList();
    private final TableView<Room> tableView = new TableView<>();
    private final Label status = ViewUtils.statusBar();
    private final javafx.scene.control.Button checkButton
            = ViewUtils.primaryButton("Check", this::check);

    /**
     * Initializes the Check Availability view with required service dependencies.
     */
    public CheckAvailabilityView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
    }

    /**
     * Provides the title of this view.
     *
     * @return the view title
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Provides the visual component for the check availability interface.
     *
     * @return The root container node.
     */
    @Override
    public Node getView() {
        return root;
    }

    /**
     * Constructs the layout for the check availability form.
     *
     * @return the root container with all configured UI components
     */
    private VBox buildView() {
        VBox box = ViewUtils.formContainer(TITLE);

        checkInPicker.setPromptText("yyyy-MM-dd");
        checkOutPicker.setPromptText("yyyy-MM-dd");
        roomNumberField.setPromptText("e.g. 101");

        specificRoomRadio.setToggleGroup(modeGroup);
        allRoomsRadio.setToggleGroup(modeGroup);
        specificRoomRadio.setSelected(true);
        modeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> updateMode());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Check-in date:"), 0, 0);
        grid.add(checkInPicker, 1, 0);
        grid.add(new Label("Check-out date:"), 0, 1);
        grid.add(checkOutPicker, 1, 1);
        grid.add(specificRoomRadio, 0, 2);
        grid.add(roomNumberField, 1, 2);
        grid.add(allRoomsRadio, 0, 3);

        resultLabel.setWrapText(true);
        resultLabel.setMaxWidth(Double.MAX_VALUE);

        setupTable();

        box.getChildren().addAll(
                grid,
                resultLabel,
                ViewUtils.buttonRow(checkButton),
                status,
                tableView
        );
        VBox.setMargin(status, new Insets(8, 0, 0, 0));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return box;
    }

    /**
     * Synchronizes the room number field state with the selected availability check mode.
     */
    ```
    private void updateMode() {
        roomNumberField.setDisable(allRoomsRadio.isSelected());
        if (allRoomsRadio.isSelected()) {
            roomNumberField.clear();
        }
    }

    /**
     * Initializes the table view with columns for room number, type, and price per night.
     */
    private void setupTable() {
        TableColumn<Room, Integer> roomNumberCol = new TableColumn<>("Room Number");
        roomNumberCol.setCellValueFactory(c -> {
            Room v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleIntegerProperty(v.getRoomNumber()).asObject();
        });
        roomNumberCol.setPrefWidth(100);

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(c -> {
            Room v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getRoomType().getDescription());
        });
        typeCol.setPrefWidth(220);

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(c -> {
            Room v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleDoubleProperty(v.getPricePerNight()).asObject();
        });
        priceCol.setCellFactory(tc -> new TableCell<Room, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                setText(empty || price == null ? "" : String.format("$%.2f", price));
            }
        });
        priceCol.setPrefWidth(120);

        tableView.getColumns().addAll(roomNumberCol, typeCol, priceCol);
        tableView.setItems(roomData);
    }

    private void check() {
        LocalDate checkIn = readDate(checkInPicker, "check-in");
        LocalDate checkOut = readDate(checkOutPicker, "check-out");
        if (checkIn == null || checkOut == null) {
            return;
        }
        if (checkIn.isAfter(checkOut)) {
            ViewUtils.setStatus(status, "Check-in date must be on or before check-out date.",
                    ViewUtils.StatusKind.ERROR);
            return;
        }
        try {
            if (specificRoomRadio.isSelected()) {
                int roomNumber;
                try {
                    roomNumber = Integer.parseInt(roomNumberField.getText().trim());
                } catch (NumberFormatException e) {
                    ViewUtils.setStatus(status, "Please enter a valid room number.",
                            ViewUtils.StatusKind.ERROR);
                    return;
                }
                if (hotelManager.getRoomService().getRoomByNumber(roomNumber).isEmpty()) {
                    ViewUtils.setStatus(status, "Room " + roomNumber + " was not found.",
                            ViewUtils.StatusKind.ERROR);
                    resultLabel.setText("");
                    roomData.clear();
                    return;
                }
                boolean available = hotelManager.getReservationService()
                        .isRoomAvailable(roomNumber, checkIn, checkOut);
                String verdict = "Room " + roomNumber + " is "
                        + (available ? "AVAILABLE" : "NOT AVAILABLE")
                        + " for " + checkIn.format(FORMATTER) + " to " + checkOut.format(FORMATTER) + ".";
                resultLabel.setText(verdict);
                resultLabel.setStyle(available
                        ? "-fx-text-fill: #1e8449; -fx-font-weight: bold; -fx-padding: 8;"
                        : "-fx-text-fill: #c0392b; -fx-font-weight: bold; -fx-padding: 8;");
                ViewUtils.setStatus(status, verdict, available
                        ? ViewUtils.StatusKind.SUCCESS
                        : ViewUtils.StatusKind.WARNING);
                roomData.clear();
            } else {
                List<Room> available = hotelManager.getAvailableRoomsForDateRange(checkIn, checkOut);
                roomData.setAll(available);
                resultLabel.setText("Available rooms for " + checkIn.format(FORMATTER)
                        + " to " + checkOut.format(FORMATTER) + ": " + available.size() + " room(s).");
                resultLabel.setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-padding: 8;");
                ViewUtils.setStatus(status,
                        "Found " + available.size() + " available room(s).",
                        ViewUtils.StatusKind.INFO);
            }
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Check availability failed", e);
            ViewUtils.showError(status, e, LOGGER);
        }
    }

    /**
     * Retrieves a date from the picker, validating that a selection was made.
     *
     * If no date is selected, displays a warning status message and returns {@code null}.
     *
     * @param label a descriptive label for the date (e.g., "check-in", "check-out"),
     *              used in the validation warning message
     * @return      the selected date, or {@code null} if no date was selected
     */
    private LocalDate readDate(DatePicker picker, String label) {
        LocalDate date = picker.getValue();
        if (date == null) {
            ViewUtils.setStatus(status, "Please select a " + label + " date.", ViewUtils.StatusKind.WARNING);
            return null;
        }
        return date;
    }
}
