package hotel.gui.view;

import hotel.gui.NavigationManager;
import hotel.service.HotelManager;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * The top-level menu of the GUI. Mirrors the console's "Main Menu" with
 * four entries: Room Management, Customer Management, Reservation
 * Management, and Exit.
 * <p>
 * Each button navigates to the corresponding sub-menu view via
 * {@link NavigationManager#navigateTo(View)}. The "Exit" button closes
 * the application (still within the same single window — no new stages
 * are opened).
 */
public class MainMenuView implements View {

    public static final String TITLE = "Main Menu";

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;

    public MainMenuView(HotelManager hotelManager, NavigationManager navigationManager) {
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

    /** Static accessor for the home view's title, used by other views' "Back to Main Menu" buttons. */
    public static String titleStatic() {
        return TITLE;
    }

    private VBox buildView() {
        VBox box = ViewUtils.menuContainer(
                TITLE,
                "Choose an area to manage. Use the Back / Forward / Home buttons at the bottom to navigate.");

        box.getChildren().addAll(
                ViewUtils.menuButton("1. Room Management", this::openRoomManagement),
                ViewUtils.menuButton("2. Customer Management", this::openCustomerManagement),
                ViewUtils.menuButton("3. Reservation Management", this::openReservationManagement),
                ViewUtils.dangerMenuButton("4. Exit", this::exitApplication)
        );
        return box;
    }

    private void openRoomManagement() {
        navigationManager.navigateTo(new RoomManagementMenuView(hotelManager, navigationManager));
    }

    private void openCustomerManagement() {
        navigationManager.navigateTo(new CustomerManagementMenuView(hotelManager, navigationManager));
    }

    private void openReservationManagement() {
        navigationManager.navigateTo(new ReservationManagementMenuView(hotelManager, navigationManager));
    }

    private void exitApplication() {
        javafx.application.Platform.exit();
    }
}
