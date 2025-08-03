package me.skorb.controller;

import javafx.scene.control.Tab;
import me.skorb.event.*;
import me.skorb.view.tabs.NewServiceTab;
import me.skorb.view.tabs.ServicesTab;
import me.skorb.view.tabs.TabManager;

public class ServiceController {

    public ServiceController() {
        EventBus.addListener(OpenNewServiceTabEvent.class, event -> openNewServiceTab());
        EventBus.addListener(OpenServicesTabEvent.class, event -> openServicesTab());
    }

    private void openNewServiceTab() {
        NewServiceTab newServiceTab = new NewServiceTab();
    }

    private void openServicesTab() {
        ServicesTab servicesTab = new ServicesTab();
        Tab tab = servicesTab.createServicesTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

}
