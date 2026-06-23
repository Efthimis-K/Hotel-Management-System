package hotel.gui.view;

import hotel.gui.NavigationManager;
import hotel.service.HotelManager;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Sub-menu for customer management. Mirrors the console's "Customer
 * Management" sub-menu: Register Customer, View All Customers, Back to
 * Main Menu.
 */
public class CustomerManagementMenuView implements View {

    private static final String TITLE = "Customer Management";

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;

    public CustomerManagementMenuView(HotelManager hotelManager, NavigationManager navigationManager) {
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
                "Select a customer operation.");

        box.getChildren().addAll(
                ViewUtils.menuButton("1. Register Customer", this::openRegisterCustomer),
                ViewUtils.menuButton("2. View All Customers", this::openViewAllCustomers),
                ViewUtils.menuButton("3. Back to Main Menu", () ->
                        navigationManager.navigateTo(
                                new MainMenuView(hotelManager, navigationManager).getView(),
                                MainMenuView.titleStatic()))
        );
        return box;
    }

    private void openRegisterCustomer() {
        navigationManager.navigateTo(new RegisterCustomerView(hotelManager, navigationManager));
    }

    private void openViewAllCustomers() {
        navigationManager.navigateTo(new ViewAllCustomersView(hotelManager, navigationManager));
    }
}
