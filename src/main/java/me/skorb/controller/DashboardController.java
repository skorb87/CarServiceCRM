package me.skorb.controller;

import javafx.scene.control.Tab;
import me.skorb.event.EventBus;
import me.skorb.event.OpenDashboardTabEvent;
import me.skorb.view.tabs.DashboardTab;
import me.skorb.view.tabs.TabManager;

public class DashboardController {

    public DashboardController() {
        EventBus.addListener(OpenDashboardTabEvent.class, event -> openDashboardTab());
    }

    private void openDashboardTab() {
        DashboardTab dashboardTab = new DashboardTab();
        Tab tab = dashboardTab.createDashboardTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

}
