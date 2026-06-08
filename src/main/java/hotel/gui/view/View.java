package hotel.gui.view;

import javafx.scene.Node;

/**
 * A navigable, self-contained view in the single-window GUI.
 * <p>
 * Every view returns a {@link Node} (typically a {@code VBox}) that is
 * displayed in the center of the main {@code BorderPane}, plus a title used
 * in the breadcrumb.
 * <p>
 * Views should <strong>not</strong> open new windows, dialogs, or stages.
 * All feedback should be rendered inline using components such as
 * {@link ViewUtils#statusBar}.
 */
public interface View {
    /** @return the title of this view for the breadcrumb. */
    String getTitle();

    /** @return the root node of this view, displayed in the main window's center. */
    Node getView();
}
