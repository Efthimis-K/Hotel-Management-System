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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * "View All Customers" — a read-only {@link TableView} of every customer.
 * Mirrors the console's "View All Customers" operation.
 */
public class ViewAllCustomersView implements View {

    public static final String TITLE = "View All Customers";

    private static final Logger LOGGER = Logger.getLogger(ViewAllCustomersView.class.getName());

    private final HotelManager hotelManager;
    private final NavigationManager navigationManager;
    private final VBox root;
    private final ObservableList<Customer> customerData = FXCollections.observableArrayList();
    private final TableView<Customer> tableView = new TableView<>();

    public ViewAllCustomersView(HotelManager hotelManager, NavigationManager navigationManager) {
        this.hotelManager = hotelManager;
        this.navigationManager = navigationManager;
        this.root = buildView();
        try {
            refresh();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load customers on view construction", e);
        }
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
        VBox box = ViewUtils.listContainer(TITLE);
        setupTable();
        box.getChildren().addAll(
                tableView,
                ViewUtils.buttonRow(ViewUtils.secondaryButton("Refresh", this::refresh))
        );
        VBox.setVgrow(tableView, Priority.ALWAYS);
        return box;
    }

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

    private void refresh() {
        try {
            customerData.setAll(hotelManager.getAllCustomers());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to refresh customer list", e);
        }
    }
}
