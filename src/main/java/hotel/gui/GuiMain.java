package hotel.gui;

import hotel.gui.view.MainMenuView;
import hotel.repository.CustomerRepository;
import hotel.repository.ReservationRepository;
import hotel.repository.RoomRepository;
import hotel.service.HotelManager;
import hotel.storage.DatabaseInitializer;
import hotel.util.BackgroundTaskService;
import hotel.util.ErrorHandler;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Entry point for the single-window Hotel Management System GUI.
 * <p>
 * The application uses one {@link Stage} for its entire lifetime. All
 * operations (room management, customer management, reservation management,
 * and every sub-operation) are rendered in the center area of a single
 * {@link BorderPane}. A persistent breadcrumb at the top and a Home / Back /
 * Forward bar at the bottom provide navigation.
 * <p>
 * No new windows, dialogs, or stages are ever opened by view code — every
 * interaction stays within this single window.
 */
public class GuiMain extends Application {

    private HotelManager hotelManager;
    private NavigationManager navigationManager;
    private static final String HOME_TITLE = "Main Menu";

    @Override
    public void start(Stage primaryStage) {
        try {
            initializeRepositories();

            primaryStage.setTitle("Hotel Management System");

            // --- Layout: top breadcrumb, center dynamic content, bottom nav bar ---
            BorderPane root = new BorderPane();
            Label breadcrumbLabel = new Label(HOME_TITLE);
            Button homeButton = new Button();
            Button backButton = new Button();
            Button forwardButton = new Button();

            navigationManager = new NavigationManager(root, breadcrumbLabel, backButton, forwardButton, homeButton);

            root.setTop(NavigationManager.createBreadcrumbBar(breadcrumbLabel));
            root.setBottom(NavigationManager.createNavBar(homeButton, backButton, forwardButton));

            // --- Initial view: the main menu ---
            MainMenuView mainMenu = new MainMenuView(hotelManager, navigationManager);
            navigationManager.navigateTo(mainMenu.getView(), mainMenu.getTitle());

            // --- Wire up window-close confirmation ---
            primaryStage.setOnCloseRequest(event -> {
                Platform.exit();
                BackgroundTaskService.shutdown();
            });

            Scene scene = new Scene(root, 900, 650);
            applyStylesheet(scene);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            showFatalError("Failed to start application: " + e.getMessage());
        }
    }

    private void initializeRepositories() {
        DatabaseInitializer.initialize();
        RoomRepository roomRepository = new RoomRepository();
        ReservationRepository reservationRepository = new ReservationRepository();
        CustomerRepository customerRepository = new CustomerRepository();
        hotelManager = new HotelManager(roomRepository, reservationRepository, customerRepository);
    }

    /**
     * Fatal startup error: at this point the main window is not on screen,
     * so we have no choice but to show a modal {@link Alert}. After the
     * application has started, all other feedback is rendered inline.
     */
    private void showFatalError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Startup Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void applyStylesheet(Scene scene) {
        String css = GuiMain.class.getResource("gui.css") != null
                ? GuiMain.class.getResource("gui.css").toExternalForm()
                : null;
        if (css != null) {
            scene.getStylesheets().add(css);
        }
    }

    /**
     * Exposed for views that need the centralized error renderer (status bar).
     * Centralized here so all views share the same error-handling convention.
     */
    public static String renderError(Throwable t, java.util.logging.Logger logger) {
        return ErrorHandler.handle(t, logger);
    }

    public static void main(String[] args) {
        launch(args);
    }
}