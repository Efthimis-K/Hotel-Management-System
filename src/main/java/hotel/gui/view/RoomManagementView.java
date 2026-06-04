package hotel.gui.view;

import hotel.exception.HotelException;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.service.RoomService;
import hotel.util.ErrorHandler;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.logging.Logger;

public class RoomManagementView {
    private static final Logger LOGGER = Logger.getLogger(RoomManagementView.class.getName());
    private final RoomService roomService;
    private ObservableList<Room> roomData;
    private TableView<Room> tableView;

    public RoomManagementView(RoomService roomService) {
        this.roomService = roomService;
        this.roomData = FXCollections.observableArrayList();
        this.tableView = new TableView<>();
        initialize();
    }

    private void initialize() {
        setupTableColumns();
        refreshRooms();
    }

    private void setupTableColumns() {
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

    private void refreshRooms() {
        roomData.setAll(roomService.getAllRooms());
    }

    private void showAddRoomDialog() {
        Dialog<Room> dialog = new Dialog<>();
        dialog.setTitle("Add New Room");
        dialog.setHeaderText("Enter room details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField roomNumberField = new TextField();
        roomNumberField.setPromptText("Room number");

        ComboBox<RoomType> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll(RoomType.values());
        typeCombo.setPromptText("Select room type");

        TextField priceField = new TextField();
        priceField.setPromptText("Price per night");

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNumberField, 1, 0);
        grid.add(new Label("Room Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Price/Night:"), 0, 2);
        grid.add(priceField, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        typeCombo.setValue(RoomType.SINGLE);
        priceField.setText(String.valueOf(typeCombo.getValue().getDefaultPrice()));

        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                priceField.setText(String.valueOf(newVal.getDefaultPrice()));
            }
        });

        Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        roomNumberField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs(dialog, roomNumberField, typeCombo, priceField, okButton));
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> validateInputs(dialog, roomNumberField, typeCombo, priceField, okButton));
        priceField.textProperty().addListener((obs, oldVal, newVal) -> validateInputs(dialog, roomNumberField, typeCombo, priceField, okButton));

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
                    RoomType roomType = typeCombo.getValue();
                    double price = Double.parseDouble(priceField.getText().trim());
                    return new Room(roomNumber, roomType, price);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(room -> {
            try {
                roomService.createRoom(room);
                refreshRooms();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Room added successfully!");
            } catch (HotelException e) {
                showAlert(Alert.AlertType.ERROR, "Error", ErrorHandler.handle(e, LOGGER));
            }
        });
    }

    private void validateInputs(Dialog<Room> dialog, TextField roomNumberField, ComboBox<RoomType> typeCombo, TextField priceField, Node okButton) {
        boolean valid = true;
        try {
            Integer.parseInt(roomNumberField.getText().trim());
        } catch (NumberFormatException e) {
            valid = false;
        }
        if (typeCombo.getValue() == null) {
            valid = false;
        }
        try {
            Double.parseDouble(priceField.getText().trim());
        } catch (NumberFormatException e) {
            valid = false;
        }
        okButton.setDisable(!valid);
    }

    private void toggleAvailability() {
        Room selectedRoom = tableView.getSelectionModel().getSelectedItem();
        if (selectedRoom == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Please select a room to toggle availability.");
            return;
        }

        try {
            roomService.updateRoomAvailability(selectedRoom.getRoomNumber(), !selectedRoom.isAvailable());
            tableView.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Success", "Room availability updated successfully!");
        } catch (HotelException e) {
            showAlert(Alert.AlertType.ERROR, "Error", ErrorHandler.handle(e, LOGGER));
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Node getView() {
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> refreshRooms());

        Button addButton = new Button("Add Room");
        addButton.setOnAction(e -> showAddRoomDialog());

        Button toggleButton = new Button("Toggle Availability");
        toggleButton.setOnAction(e -> toggleAvailability());

        HBox buttonBox = new HBox(10, refreshButton, addButton, toggleButton);
        buttonBox.setPadding(new Insets(5));

        root.getChildren().addAll(buttonBox, tableView);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        return root;
    }
}