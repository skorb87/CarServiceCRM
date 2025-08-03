package me.skorb.controller;

import javafx.scene.control.Tab;
import me.skorb.event.EventBus;
import me.skorb.event.OpenCustomersTabEvent;
import me.skorb.event.OpenNewCustomerTabEvent;
import me.skorb.view.tabs.CustomersTab;
import me.skorb.view.tabs.NewCustomerTab;
import me.skorb.view.tabs.TabManager;

public class CustomerController {

    public CustomerController() {
        EventBus.addListener(OpenCustomersTabEvent.class, event -> openCustomersTab());
        EventBus.addListener(OpenNewCustomerTabEvent.class, event -> openNewCustomerTab());
    }

    private void openCustomersTab() {
        CustomersTab customersTab = new CustomersTab();
        Tab tab = customersTab.createCustomersTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

    private void openNewCustomerTab() {
        NewCustomerTab newCustomerTab = new NewCustomerTab();
        Tab tab = newCustomerTab.createNewCustomerTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

}
