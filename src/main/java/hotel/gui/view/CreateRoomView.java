package hotel.gui.view;

import hotel.exception.HotelException;
import hotel.gui.NavigationManager;
import hotel.model.Room;
import hotel.model.RoomType;
import hotel.service.HotelManager;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Inline form for creating a new room. Mirrors the console's
 * "Create Room" operation. Renders entirely in the main window — no
 * dialogs are opened. Submitting the form either displays a success
 * status or an error status inline.
 */
public class CreateRoomView implements View {

    public static final String TITLE = "Create Room";

    private static final Logger LOGGER = Logger.getLogger(CreateRoomView.class.getName());

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final TextField roomNumberField = new TextField();
    private final ComboBox<RoomType> typeCombo = new ComboBox<>();
    private final TextField priceField = new TextField();
    private final Label status = ViewUtils.statusBar();
    private final javafx.scene.control.Button submitButton =
            ViewUtils.primaryButton("Create Room", this::submit);

    public CreateRoomView(HotelManager hotelManager, NavigationManager navigationManager) {
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

        roomNumberField.setPromptText("e.g. 101");

        typeCombo.getItems().addAll(RoomType.values());
        typeCombo.setValue(RoomType.SINGLE);
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                priceField.setText(String.valueOf(newVal.getDefaultPrice()));
            }
        });
        priceField.setText(String.valueOf(RoomType.SINGLE.getDefaultPrice()));
        priceField.setPromptText("Price per night");

        // Live validation: enable submit only when all inputs are valid.
        roomNumberField.textProperty().addListener((o, a, b) -> updateSubmitState());
        priceField.textProperty().addListener((o, a, b) -> updateSubmitState());
        typeCombo.valueProperty().addListener((o, a, b) -> updateSubmitState());

        grid.add(new Label("Room Number:"), 0, 0);
        grid.add(roomNumberField, 1, 0);
        grid.add(new Label("Room Type:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("Price/Night:"), 0, 2);
        grid.add(priceField, 1, 2);

        javafx.scene.control.Button clearButton = ViewUtils.secondaryButton("Clear", this::clear);
        box.getChildren().addAll(
                grid,
                ViewUtils.buttonRow(submitButton, clearButton),
                status
        );
        VBox.setMargin(status, new Insets(8, 0, 0, 0));

        updateSubmitState();
        return box;
    }

    private void updateSubmitState() {
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
        submitButton.setDisable(!valid);
    }

    private void submit() {
        try {
            int roomNumber = Integer.parseInt(roomNumberField.getText().trim());
            RoomType roomType = typeCombo.getValue();
            double price = Double.parseDouble(priceField.getText().trim());
            Room room = new Room(roomNumber, roomType, price);
            hotelManager.getRoomService().createRoom(room);
            LOGGER.log(Level.INFO, "Room created via GUI: {0}", room);
            ViewUtils.setStatus(status,
                    "Room created successfully! Room " + room.getRoomNumber()
                            + " (" + room.getRoomType().getDescription() + ").",
                    ViewUtils.StatusKind.SUCCESS);
            clear();
        } catch (HotelException e) {
            ViewUtils.showError(status, e, LOGGER);
        } catch (NumberFormatException e) {
            ViewUtils.setStatus(status, "Please enter valid numeric values.", ViewUtils.StatusKind.ERROR);
        }
    }

    private void clear() {
        roomNumberField.clear();
        typeCombo.setValue(RoomType.SINGLE);
        priceField.setText(String.valueOf(RoomType.SINGLE.getDefaultPrice()));
        roomNumberField.requestFocus();
    }
}
