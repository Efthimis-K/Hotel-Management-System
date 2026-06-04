package hotel.gui;

import hotel.gui.view.RoomManagementView;
import hotel.repository.CustomerRepository;
import hotel.repository.ReservationRepository;
import hotel.repository.RoomRepository;
import hotel.service.HotelManager;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class GuiMain extends Application {
    private HotelManager hotelManager;

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeRepositories();
            primaryStage.setTitle("Hotel Management System");
            BorderPane root = new BorderPane();
            RoomManagementView roomManagementView = new RoomManagementView(hotelManager.getRoomService());
            root.setCenter(roomManagementView.getView());
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            showErrorAlert("Startup Error", "Failed to start application: " + e.getMessage());
        }
    }

    private void initializeRepositories() {
        RoomRepository roomRepository = new RoomRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        CustomerRepository customerRepository = new CustomerRepository();
        hotelManager = new HotelManager(roomRepository, reservationRepository, customerRepository);
    }

    private void showErrorAlert(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}