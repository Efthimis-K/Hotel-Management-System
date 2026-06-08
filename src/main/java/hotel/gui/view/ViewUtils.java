package hotel.gui.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.logging.Logger;

import hotel.gui.GuiMain;

/**
 * Static helpers for building common UI components in the single-window
 * GUI. Centralized here so every view looks consistent and shares the same
 * inline-feedback convention.
 */
public final class ViewUtils {

    private ViewUtils() {
        // utility class
    }

    /**
     * Build a standard "menu view" container: a vertical stack with a title
     * and an optional subtitle. Children added afterwards are stacked below.
     */
    public static VBox menuContainer(String title, String subtitle) {
        VBox box = new VBox(10);
        box.getStyleClass().add("menu-view");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("menu-title");
        box.getChildren().add(titleLabel);

        if (subtitle != null && !subtitle.isEmpty()) {
            Label subtitleLabel = new Label(subtitle);
            subtitleLabel.getStyleClass().add("menu-subtitle");
            subtitleLabel.setWrapText(true);
            box.getChildren().add(subtitleLabel);
        }
        return box;
    }

    /**
     * Build a standard "form view" container. Children are typically a
     * {@code GridPane} (added by the caller) and a status bar.
     */
    public static VBox formContainer(String title) {
        VBox box = new VBox(10);
        box.getStyleClass().add("form-view");
        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("menu-title");
            box.getChildren().add(titleLabel);
        }
        return box;
    }

    /**
     * Build a standard "list view" container with a title.
     */
    public static VBox listContainer(String title) {
        VBox box = new VBox(10);
        box.getStyleClass().add("list-view");
        if (title != null && !title.isEmpty()) {
            Label titleLabel = new Label(title);
            titleLabel.getStyleClass().add("menu-title");
            box.getChildren().add(titleLabel);
        }
        return box;
    }

    /**
     * Create a menu-style button with the given label and click handler.
     */
    public static Button menuButton(String text, Runnable onClick) {
        Button btn = new Button(text);
        btn.getStyleClass().add("menu-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> onClick.run());
        return btn;
    }

    /**
     * Create a "danger" menu-style button (red border).
     */
    public static Button dangerMenuButton(String text, Runnable onClick) {
        Button btn = menuButton(text, onClick);
        btn.getStyleClass().add("danger");
        return btn;
    }

    /**
     * Create a primary form button (green).
     */
    public static Button primaryButton(String text, Runnable onClick) {
        Button btn = new Button(text);
        btn.getStyleClass().add("primary");
        btn.setOnAction(e -> onClick.run());
        return btn;
    }

    /**
     * Create a secondary form button (gray).
     */
    public static Button secondaryButton(String text, Runnable onClick) {
        Button btn = new Button(text);
        btn.getStyleClass().add("secondary");
        btn.setOnAction(e -> onClick.run());
        return btn;
    }

    /**
     * Create a danger form button (red).
     */
    public static Button dangerButton(String text, Runnable onClick) {
        Button btn = new Button(text);
        btn.getStyleClass().add("danger");
        btn.setOnAction(e -> onClick.run());
        return btn;
    }

    /**
     * Create an empty status bar {@link Label} that can be styled and
     * updated. The label is initially hidden (no text, not visible).
     * Use {@link #setStatus(Label, String, StatusKind)} to populate it.
     */
    public static Label statusBar() {
        Label label = new Label();
        label.setWrapText(true);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setVisible(false);
        label.setManaged(false);
        return label;
    }

    public enum StatusKind { INFO, SUCCESS, ERROR, WARNING }

    /**
     * Set the text and style of a status bar label. The label is automatically
     * shown (or hidden if text is null/blank).
     */
    public static void setStatus(Label status, String text, StatusKind kind) {
        status.getStyleClass().removeAll(
                "status-info", "status-success", "status-error", "status-warning");
        if (text == null || text.isBlank()) {
            status.setText("");
            status.setVisible(false);
            status.setManaged(false);
            return;
        }
        status.setText(text);
        switch (kind) {
            case SUCCESS -> status.getStyleClass().add("status-success");
            case ERROR -> status.getStyleClass().add("status-error");
            case WARNING -> status.getStyleClass().add("status-warning");
            default -> status.getStyleClass().add("status-info");
        }
        status.setVisible(true);
        status.setManaged(true);
    }

    /**
     * Show a caught exception's user-friendly message in the status bar.
     */
    public static void showError(Label status, Throwable t, Logger logger) {
        setStatus(status, GuiMain.renderError(t, logger), StatusKind.ERROR);
    }

    /**
     * Build a horizontal button row with the given buttons, right-aligned.
     * Returns an {@link HBox} ready to add to a form.
     */
    public static HBox buttonRow(Node... buttons) {
        Region spacer = new Region();
        HBox row = new HBox(8, spacer);
        row.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        for (Node b : buttons) {
            row.getChildren().add(b);
        }
        return row;
    }

    /**
     * Make a node grow to fill available vertical space (e.g. a table inside
     * a list view).
     */
    public static void growVertical(Node node) {
        if (node instanceof VBox v) {
            VBox.setVgrow(node, Priority.ALWAYS);
        } else if (node.getParent() instanceof VBox) {
            VBox.setVgrow(node, Priority.ALWAYS);
        }
    }

    /** Small helper to add consistent insets to a child inside a container. */
    public static <T extends Node> T withInsets(T node, Insets insets) {
        if (node instanceof javafx.scene.layout.Region r) {
            r.setPadding(insets);
        }
        return node;
    }
}
