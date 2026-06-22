package hotel.gui.view;

import hotel.gui.NavigationManager;
import hotel.model.Room;
import hotel.service.HotelManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * "View All Rooms" — a read-only {@link TableView} of every room, showing
 * number, type, price, and availability. Mirrors the console's "View All
 * Rooms" operation. A Refresh button reloads from the service.
 */
public class ViewAllRoomsView implements View {

    public static final String TITLE = "View All Rooms";

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final ObservableList<Room> roomData = FXCollections.observableArrayList();
    private final TableView<Room> tableView = new TableView<>();

    /**
     * Constructs a read-only view displaying all hotel rooms.
     *
     * @param hotelManager      provides access to room data
     * @param navigationManager manager for navigation between views
     */
    public ViewAllRoomsView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
        refresh();
    }

    /**
     * Returns the title of this view.
     *
     * @return the view title
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Provides the root node of the view.
     *
     * @return the root VBox containing the view hierarchy
     */
    @Override
    public Node getView() {
        return root;
    }

    /**
     * Builds the main user interface for the view.
     *
     * @return the root view container
     */
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

    /**
     * Configures the table view with columns for room number, type, price per night, and availability, and binds the table to the room data source.
     */
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

    /**
     * Refreshes the table with the latest room data from the service.
     */
    private void refresh() {
        roomData.setAll(hotelManager.getRoomService().getAllRooms());
    }
}
