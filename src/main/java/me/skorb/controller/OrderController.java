package me.skorb.controller;

import javafx.scene.control.Tab;
import me.skorb.event.EventBus;
import me.skorb.event.OpenNewOrderTabEvent;
import me.skorb.event.OpenOrdersTabEvent;
import me.skorb.view.tabs.NewOrderTab;
import me.skorb.view.tabs.OrdersTab;
import me.skorb.view.tabs.TabManager;

public class OrderController {

    public OrderController() {
        EventBus.addListener(OpenNewOrderTabEvent.class, event -> openNewOrderTab());
        EventBus.addListener(OpenOrdersTabEvent.class, event -> openOrdersTab());
    }

    private void openNewOrderTab() {
        NewOrderTab newOrderTab = new NewOrderTab();
        Tab tab = newOrderTab.createNewOrderTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

    private void openOrdersTab() {
        OrdersTab ordersTab = new OrdersTab();
        Tab tab = ordersTab.createOrdersTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

}
