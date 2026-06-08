package hotel.gui.view;

import hotel.gui.NavigationManager;
import hotel.service.HotelManager;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Sub-menu for reservation management. Mirrors the console's "Reservation
 * Management" sub-menu: Create, Cancel, Search, Check Availability, Back.
 */
public class ReservationManagementMenuView implements View {

    private static final String TITLE = "Reservation Management";

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;

    public ReservationManagementMenuView(HotelManager hotelManager, NavigationManager navigationManager) {
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
        VBox box = ViewUtils.menuContainer(
                TITLE,
                "Select a reservation operation.");

        box.getChildren().addAll(
                ViewUtils.menuButton("1. Create Reservation", this::openCreateReservation),
                ViewUtils.menuButton("2. Cancel Reservation", this::openCancelReservation),
                ViewUtils.menuButton("3. Search Reservations", this::openSearchReservations),
                ViewUtils.menuButton("4. Check Availability", this::openCheckAvailability),
                ViewUtils.menuButton("5. Back to Main Menu", () ->
                        navigationManager.navigateTo(
                                new MainMenuView(hotelManager, navigationManager).getView(),
                                MainMenuView.titleStatic()))
        );
        return box;
    }

    private void openCreateReservation() {
        navigationManager.navigateTo(new CreateReservationView(hotelManager, navigationManager));
    }

    private void openCancelReservation() {
        navigationManager.navigateTo(new CancelReservationView(hotelManager, navigationManager));
    }

    private void openSearchReservations() {
        navigationManager.navigateTo(new SearchReservationsView(hotelManager, navigationManager));
    }

    private void openCheckAvailability() {
        navigationManager.navigateTo(new CheckAvailabilityView(hotelManager, navigationManager));
    }
}
