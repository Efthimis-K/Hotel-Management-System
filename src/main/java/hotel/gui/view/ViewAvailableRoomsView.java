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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * "View Available Rooms" — pick a check-in and check-out date and see all rooms
 * available for that range. Mirrors the console's "View Available Rooms"
 * operation. The result is shown in the same view (no new window).
 */
public class ViewAvailableRoomsView implements View {

    public static final String TITLE = "View Available Rooms";

    private static final Logger LOGGER = Logger.getLogger(ViewAvailableRoomsView.class.getName());
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final DatePicker checkInPicker = new DatePicker();
    private final DatePicker checkOutPicker = new DatePicker();
    private final Label status = ViewUtils.statusBar();
    private final ObservableList<Room> roomData = FXCollections.observableArrayList();
    private final TableView<Room> tableView = new TableView<>();
    private final javafx.scene.control.Button searchButton
            = ViewUtils.primaryButton("Search", this::search);

    public ViewAvailableRoomsView(HotelManager hotelManager, NavigationManager navigationManager) {
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

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        checkInPicker.setPromptText("yyyy-MM-dd");
        checkInPicker.setPrefWidth(240);
        checkOutPicker.setPromptText("yyyy-MM-dd");
        checkOutPicker.setPrefWidth(240);

        grid.add(new Label("Check-in date:"), 0, 0);
        grid.add(checkInPicker, 1, 0);
        grid.add(new Label("Check-out date:"), 0, 1);
        grid.add(checkOutPicker, 1, 1);

        setupTable();

        box.getChildren().addAll(
                grid,
                ViewUtils.buttonRow(searchButton),
                status,
                tableView
        );
        VBox.setMargin(status, new Insets(8, 0, 0, 0));
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return box;
    }

    private void setupTable() {
        TableColumn<Room, Integer> roomNumberCol = new TableColumn<>("Room Number");
        roomNumberCol.setCellValueFactory(cellData -> {
            Room room = cellData.getValue();
            return room != null ? new javafx.beans.property.SimpleIntegerProperty(room.getRoomNumber()).asObject() : null;
        });
        roomNumberCol.setPrefWidth(100);

        TableColumn<Room, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(cellData -> {
            Room room = cellData.getValue();
            return room != null ? new javafx.beans.property.SimpleStringProperty(room.getRoomType().getDescription()) : null;
        });
        typeCol.setPrefWidth(220);

        TableColumn<Room, Double> priceCol = new TableColumn<>("Price/Night");
        priceCol.setCellValueFactory(cellData -> {
            Room room = cellData.getValue();
            return room != null ? new javafx.beans.property.SimpleDoubleProperty(room.getPricePerNight()).asObject() : null;
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

    private void search() {
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
            List<Room> available = hotelManager.getAvailableRoomsForDateRange(checkIn, checkOut);
            roomData.setAll(available);
            String title = "Available rooms for " + checkIn.format(FORMATTER)
                    + " to " + checkOut.format(FORMATTER);
            ViewUtils.setStatus(status,
                    title + " — " + available.size() + " room(s) found.",
                    ViewUtils.StatusKind.INFO);
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Failed to fetch available rooms", e);
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
}
