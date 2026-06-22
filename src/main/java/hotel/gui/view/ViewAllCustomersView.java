package hotel.gui.view;

import hotel.gui.NavigationManager;
import hotel.model.Customer;
import hotel.service.HotelManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/**
 * "View All Customers" — a read-only {@link TableView} of every customer.
 * Mirrors the console's "View All Customers" operation.
 */
public class ViewAllCustomersView implements View {

    public static final String TITLE = "View All Customers";

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final ObservableList<Customer> customerData = FXCollections.observableArrayList();
    private final TableView<Customer> tableView = new TableView<>();

    /**
     * Initializes the view with the specified managers and loads the initial customer list.
     *
     * @param hotelManager the manager providing access to customer data
     * @param navigationManager the manager for navigation operations
     */
    public ViewAllCustomersView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
        refresh();
    }

    /**
     * Gets the title of this view.
     *
     * @return the view title
     */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Provides the root node for this view.
     *
     * @return the root JavaFX node for display
     */
    @Override
    public Node getView() {
        return root;
    }

    /**
     * Constructs the UI layout containing the customer table and refresh button.
     *
     * @return a VBox with the customer table and refresh controls
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
     * Configures the customer table columns and binds the customer data to the table view.
     */
    private void setupTable() {
        TableColumn<Customer, String> idCol = new TableColumn<>("Customer ID");
        idCol.setCellValueFactory(c -> {
            Customer v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getCustomerId());
        });
        idCol.setPrefWidth(100);

        TableColumn<Customer, String> firstCol = new TableColumn<>("First Name");
        firstCol.setCellValueFactory(c -> {
            Customer v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getFirstName());
        });
        firstCol.setPrefWidth(140);

        TableColumn<Customer, String> lastCol = new TableColumn<>("Last Name");
        lastCol.setCellValueFactory(c -> {
            Customer v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getLastName());
        });
        lastCol.setPrefWidth(140);

        TableColumn<Customer, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c -> {
            Customer v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getEmail());
        });
        emailCol.setPrefWidth(220);

        TableColumn<Customer, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(c -> {
            Customer v = c.getValue();
            return v == null ? null : new javafx.beans.property.SimpleStringProperty(v.getPhoneNumber());
        });
        phoneCol.setPrefWidth(140);

        tableView.getColumns().addAll(idCol, firstCol, lastCol, emailCol, phoneCol);
        tableView.setItems(customerData);
    }

    /**
     * Refreshes the displayed customer data by fetching the current list from the hotel manager.
     */
    private void refresh() {
        customerData.setAll(hotelManager.getAllCustomers());
    }
}
