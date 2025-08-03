package me.skorb.view.tabs;

import javafx.scene.control.TabPane;

/*
* Stores the TabPane instance globally.
* Allows any controller to access the same TabPane
* */
public class TabManager {

    private static TabManager instance;

    private final TabPane tabPane;

    private TabManager() {
        tabPane = new TabPane();
    }

    public static TabManager getInstance() {
        if (instance == null) {
            instance = new TabManager();
        }
        return instance;
    }

    public TabPane getTabPane() {
        return tabPane;
    }
}
