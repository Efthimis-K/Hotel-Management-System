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

    /**
     * Initializes the Customer Management menu view with the specified dependencies.
     */
    public CustomerManagementMenuView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
    }

    /**
     * Gets the menu title.
     *
     * @return the menu title
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Provides the customer management menu view.
     *
     * @return The JavaFX node displaying the customer management menu.
     */
    @Override
    public Node getView() {
        return root;
    }

    /**
     * Constructs the customer management menu with options to register a customer, view all customers, or return to the main menu.
     *
     * @return the customer management menu as a VBox
     */
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

    /**
     * Navigates to the Register Customer view.
     */
    private void openRegisterCustomer() {
        navigationManager.navigateTo(new RegisterCustomerView(hotelManager, navigationManager));
    }

    /**
     * Opens the view for displaying all customers.
     */
    private void openViewAllCustomers() {
        navigationManager.navigateTo(new ViewAllCustomersView(hotelManager, navigationManager));
    }
}
