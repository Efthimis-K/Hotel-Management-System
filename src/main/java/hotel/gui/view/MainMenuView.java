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

    /**
     * Initializes the Main Menu view with the specified hotel and navigation managers.
     *
     * @param hotelManager the HotelManager for hotel operations
     * @param navigationManager the NavigationManager for view navigation
     */
    public MainMenuView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
    }

    /**
     * Gets the title of the main menu view.
     *
     * @return the main menu title
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Retrieves the main menu view node.
     *
     * @return The JavaFX node containing the main menu.
     */
    @Override
    public Node getView() {
        return root;
    }

    /**
     * Returns the Main Menu view's title.
     *
     * @return the Main Menu title
     */
    public static String titleStatic() {
        return TITLE;
    }

    /**
     * Constructs the main menu interface with navigation buttons.
     *
     * @return A VBox containing the menu UI.
     */
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

    /**
     * Opens the Room Management menu.
     */
    private void openRoomManagement() {
        navigationManager.navigateTo(new RoomManagementMenuView(hotelManager, navigationManager));
    }

    /**
     * Navigates to the Customer Management menu.
     */
    private void openCustomerManagement() {
        navigationManager.navigateTo(new CustomerManagementMenuView(hotelManager, navigationManager));
    }

    /**
     * Navigates to the Reservation Management menu.
     */
    private void openReservationManagement() {
        navigationManager.navigateTo(new ReservationManagementMenuView(hotelManager, navigationManager));
    }

    /**
     * Terminates the application.
     */
    private void exitApplication() {
        javafx.application.Platform.exit();
    }
}
