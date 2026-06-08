package hotel.gui.view;

import hotel.gui.NavigationManager;
import hotel.service.HotelManager;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Sub-menu for room management. Mirrors the console's "Room Management"
 * sub-menu: Create Room, View All Rooms, View Available Rooms. A "Back to
 * Main Menu" button is provided as a convenience, but the user may also use
 * the persistent Back / Home buttons in the navigation bar.
 */
public class RoomManagementMenuView implements View {

    private static final String TITLE = "Room Management";

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;

    public RoomManagementMenuView(HotelManager hotelManager, NavigationManager navigationManager) {
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
                "Select a room operation.");

        box.getChildren().addAll(
                ViewUtils.menuButton("1. Create Room", this::openCreateRoom),
                ViewUtils.menuButton("2. View All Rooms", this::openViewAllRooms),
                ViewUtils.menuButton("3. View Available Rooms", this::openViewAvailableRooms),
                ViewUtils.menuButton("4. Back to Main Menu", () ->
                        navigationManager.navigateTo(
                                new MainMenuView(hotelManager, navigationManager).getView(),
                                MainMenuView.titleStatic()))
        );
        return box;
    }

    private void openCreateRoom() {
        navigationManager.navigateTo(new CreateRoomView(hotelManager, navigationManager));
    }

    private void openViewAllRooms() {
        navigationManager.navigateTo(new ViewAllRoomsView(hotelManager, navigationManager));
    }

    private void openViewAvailableRooms() {
        navigationManager.navigateTo(new ViewAvailableRoomsView(hotelManager, navigationManager));
    }
}
