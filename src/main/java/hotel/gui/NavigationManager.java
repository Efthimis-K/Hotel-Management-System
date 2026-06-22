package hotel.gui;

import hotel.gui.view.View;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Central controller for the single-window GUI. Owns the {@link BorderPane}'s
 * center area, manages a back/forward history of views, and keeps the
 * breadcrumb + navigation bar in sync.
 * <p>
 * Views are simple {@link Node}s and are pushed onto a back stack. The forward
 * stack is cleared whenever a new view is navigated to (mirroring typical
 * browser behaviour). All navigation happens on the same {@code Stage} — no
 * new windows, dialogs, or stages are ever created.
 */
public class NavigationManager {

    private static final String SEPARATOR = " \u203A "; // single right-pointing angle quotation mark

    private final BorderPane root;
    private final Label breadcrumbLabel;
    private final Button backButton;
    private final Button forwardButton;
    private final Button homeButton;

    private final Deque<Entry> backStack = new ArrayDeque<>();
    private final Deque<Entry> forwardStack = new ArrayDeque<>();

    private record Entry(Node node, String title) {}

    /**
     * @param root           the main {@link BorderPane} whose center will be
     *                       swapped on every navigation.
     * @param breadcrumbLabel the label used to render the current location
     *                        (e.g. "Main Menu \u203A Room Management").
     * @param backButton     the persistent Back button in the navigation bar.
     * @param forwardButton  the persistent Forward button in the navigation bar.
     * @param homeButton     the persistent Home button in the navigation bar.
     */
    public NavigationManager(BorderPane root,
                             Label breadcrumbLabel,
                             Button backButton,
                             Button forwardButton,
                             Button homeButton) {
        this.root = root;
        this.breadcrumbLabel = breadcrumbLabel;
        this.backButton = backButton;
        this.forwardButton = forwardButton;
        this.homeButton = homeButton;

        backButton.setOnAction(e -> goBack());
        forwardButton.setOnAction(e -> goForward());
        homeButton.setOnAction(e -> goHome());

        backButton.setDisable(true);
        forwardButton.setDisable(true);
    }

    /**
     * Navigate to a new view, pushing the current view onto the back stack.
     * The forward stack is cleared.
     *
     * @param view  the new view to display in the center area.
     * @param title the human-readable title for the breadcrumb.
     */
    public void navigateTo(Node view, String title) {
        // Snapshot the current view, if any, so Back works.
        Node current = root.getCenter();
        String currentTitle = breadcrumbLabel.getText();
        if (current != null && currentTitle != null && !currentTitle.isEmpty()) {
            backStack.push(new Entry(current, currentTitle));
        }
        forwardStack.clear();
        show(view, title);
    }

    /**
     * Convenience overload for {@link View} instances. Uses the view's
     * {@link View#getTitle()} for the breadcrumb and {@link View#getView()}
     * for the content.
     */
    public void navigateTo(View view) {
        navigateTo(view.getView(), view.getTitle());
    }

    /** Pop the back stack and display the previous view. No-op if empty. */
    public void goBack() {
        if (backStack.isEmpty()) {
            return;
        }
        Entry previous = backStack.pop();
        // Push the current view onto the forward stack so Forward can return.
        Node current = root.getCenter();
        String currentTitle = breadcrumbLabel.getText();
        if (current != null && currentTitle != null && !currentTitle.isEmpty()) {
            forwardStack.push(new Entry(current, currentTitle));
        }
        show(previous.node(), previous.title());
    }

    /** Pop the forward stack and display the next view. No-op if empty. */
    public void goForward() {
        if (forwardStack.isEmpty()) {
            return;
        }
        Entry next = forwardStack.pop();
        Node current = root.getCenter();
        String currentTitle = breadcrumbLabel.getText();
        if (current != null && currentTitle != null && !currentTitle.isEmpty()) {
            backStack.push(new Entry(current, currentTitle));
        }
        show(next.node(), next.title());
    }

    /**
     * Return to the home view (the first view that was displayed). Clears the
     * forward stack.
     */
    public void goHome() {
        if (backStack.isEmpty()) {
            updateNavButtons();
            return;
        }

        Node current = root.getCenter();
        String currentTitle = breadcrumbLabel.getText();
        if (current != null && currentTitle != null && !currentTitle.isEmpty()) {
            forwardStack.push(new Entry(current, currentTitle));
        }

        // The backStack has entries in order: [newest, ..., oldest=home] (front to back)
        // We want forwardStack to have: [oldest-after-home, ..., newest] so forward
        // navigation goes in the original order.
        // Move all entries from backStack to forwardStack in reverse order (bottom to top).
        Deque<Entry> temp = new ArrayDeque<>();
        while (!backStack.isEmpty()) {
            // pollLast() gets from bottom (oldest first)
            temp.push(backStack.pollLast());
        }
        // temp now has: [home, view1, view2, ..., newest] (front to back)
        // The first entry is home, the rest should go to forwardStack
        Entry home = temp.pollFirst(); // Remove and get home
        // Move remaining entries to forwardStack (they'll be in correct order: view1, view2, ...)
        while (!temp.isEmpty()) {
            forwardStack.push(temp.pollFirst());
        }

        if (home != null) {
            show(home.node(), home.title());
            // After going home, the "back" history is empty.
            backStack.clear();
        }
        updateNavButtons();
    }

    /** Clear all navigation history. The current view stays as-is. */
    public void clearHistory() {
        backStack.clear();
        forwardStack.clear();
        updateNavButtons();
    }

    private void show(Node view, String title) {
        root.setCenter(view);
        breadcrumbLabel.setText(title);
        updateNavButtons();
    }

    private void updateNavButtons() {
        backButton.setDisable(backStack.isEmpty());
        forwardButton.setDisable(forwardStack.isEmpty());
    }

    /**
     * Convenience: build the standard top breadcrumb bar.
     */
    public static HBox createBreadcrumbBar(Label breadcrumbLabel) {
        breadcrumbLabel.getStyleClass().add("breadcrumb-label");
        HBox bar = new HBox(breadcrumbLabel);
        bar.getStyleClass().add("breadcrumb-bar");
        bar.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
        return bar;
    }

    /**
     * Convenience: build the standard bottom navigation bar (Home / Back / Forward).
     */
    public static HBox createNavBar(Button homeButton, Button backButton, Button forwardButton) {
        homeButton.setText("Home");
        backButton.setText("\u2190 Back");   // ←
        forwardButton.setText("Forward \u2192"); // →

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox bar = new HBox(8, homeButton, backButton, forwardButton, spacer);
        bar.getStyleClass().add("nav-bar");
        bar.setPadding(new javafx.geometry.Insets(8, 12, 8, 12));
        return bar;
    }

    /** Separator used in breadcrumb rendering. Exposed for views that want to compose their own title. */
    public static String separator() {
        return SEPARATOR;
    }
}
