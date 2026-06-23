package hotel.gui.view;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import hotel.gui.NavigationManager;
import hotel.model.Room;
import hotel.service.HotelManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * "View All Rooms" — a read-only {@link TableView} of every room, showing
 * number, type, price, and availability. Mirrors the console's "View All Rooms"
 * operation. A Refresh button reloads from the service.
 */
public class ViewAllRoomsView implements View {

    public static final String TITLE = "View All Rooms";

    private static final Logger LOGGER = Logger.getLogger(ViewAllRoomsView.class.getName());

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final ObservableList<Room> roomData = FXCollections.observableArrayList();
    private final TableView<Room> tableView = new TableView<>();

    public ViewAllRoomsView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
        // Load rooms asynchronously to avoid blocking the UI thread during construction
        refresh();
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
        VBox box = ViewUtils.listContainer(TITLE);
        setupTable();
        box.getChildren().addAll(
                tableView,
                ViewUtils.buttonRow(ViewUtils.secondaryButton("Refresh", this::refresh))
        );
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
        typeCol.setPrefWidth(200);

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

        TableColumn<Room, String> availableCol = new TableColumn<>("Available");
        availableCol.setCellValueFactory(cellData -> {
            Room room = cellData.getValue();
            return room != null ? new javafx.beans.property.SimpleStringProperty(room.isAvailable() ? "Yes" : "No") : null;
        });
        availableCol.setPrefWidth(100);

        tableView.getColumns().addAll(roomNumberCol, typeCol, priceCol, availableCol);
        tableView.setItems(roomData);
    }

    private void refresh() {
        Task<List<Room>> task = new Task<>() {
            @Override
            protected List<Room> call() throws Exception {
                // Perform the potentially long-running database query off the UI thread
                return hotelManager.getRoomService().getAllRooms();
            }
        };

        task.setOnSucceeded(event -> {
            // This runs on the JavaFX Application Thread, safe to update UI components
            roomData.setAll(task.getValue());
        });

        task.setOnFailed(event -> {
            LOGGER.log(Level.WARNING, "Failed to refresh room list", task.getException());
        });

        // Execute the task on a background thread
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
