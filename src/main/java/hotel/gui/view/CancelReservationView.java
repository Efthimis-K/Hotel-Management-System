package hotel.gui.view;

import hotel.exception.HotelException;
import hotel.gui.NavigationManager;
import hotel.service.HotelManager;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

/**
 * "Cancel Reservation" — inline form mirroring the console's
 * "Cancel Reservation" operation. Submit is enabled only when an ID is
 * provided. Status feedback is rendered inline.
 */
public class CancelReservationView implements View {

    public static final String TITLE = "Cancel Reservation";

    private static final Logger LOGGER = Logger.getLogger(CancelReservationView.class.getName());

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final TextField reservationIdField = new TextField();
    private final Label status = ViewUtils.statusBar();
    private final javafx.scene.control.Button submitButton =
            ViewUtils.dangerButton("Cancel Reservation", this::submit);

    /**
     * Initializes the Cancel Reservation view with the specified service dependencies.
     */
    public CancelReservationView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
    }

    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Provides the root JavaFX node of this view.
     *
     * @return the root Node
     */
    @Override
    public Node getView() {
        return root;
    }

    /**
     * Constructs and assembles the cancel reservation form view.
     *
     * @return the assembled form view container
     */
    private VBox buildView() {
        VBox box = ViewUtils.formContainer(TITLE);

        reservationIdField.setPromptText("e.g. RES-1A2B3C4D");
        reservationIdField.textProperty().addListener((o, a, b) ->
                submitButton.setDisable(b == null || b.trim().isEmpty()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Reservation ID:"), 0, 0);
        grid.add(reservationIdField, 1, 0);

        box.getChildren().addAll(
                grid,
                ViewUtils.buttonRow(submitButton, ViewUtils.secondaryButton("Clear", this::clear)),
                status
        );
        VBox.setMargin(status, new Insets(8, 0, 0, 0));
        submitButton.setDisable(true);
        return box;
    }

    /**
     * Attempts to cancel the reservation and provides inline feedback.
     */
    private void submit() {
        String id = reservationIdField.getText().trim();
        try {
            hotelManager.getReservationService().cancelReservation(id);
            ViewUtils.setStatus(status,
                    "Reservation '" + id + "' cancelled successfully!",
                    ViewUtils.StatusKind.SUCCESS);
            reservationIdField.clear();
        } catch (HotelException e) {
            ViewUtils.showError(status, e, LOGGER);
        }
    }

    /**
     * Clears the reservation ID input field and resets the status display.
     */
    private void clear() {
        reservationIdField.clear();
        ViewUtils.setStatus(status, null, ViewUtils.StatusKind.INFO);
    }
}
