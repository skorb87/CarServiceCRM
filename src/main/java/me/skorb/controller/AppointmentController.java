package me.skorb.controller;

import javafx.scene.control.Tab;
import me.skorb.event.EventBus;
import me.skorb.event.OpenAppointmentsTabEvent;
import me.skorb.event.OpenNewAppointmentTabEvent;
import me.skorb.view.tabs.AppointmentsTab;
import me.skorb.view.tabs.NewAppointmentTab;
import me.skorb.view.tabs.TabManager;

public class AppointmentController {

    public AppointmentController() {
        // Register event listeners that listen for appointment events and open corresponding tabs
        EventBus.addListener(OpenAppointmentsTabEvent.class, event -> openAppointmentsTab());
        EventBus.addListener(OpenNewAppointmentTabEvent.class, event -> openNewAppointmentTab());
    }

    private void openAppointmentsTab() {
        AppointmentsTab appointmentsTab = new AppointmentsTab();
        Tab tab = appointmentsTab.createAppointmentsTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

    private void openNewAppointmentTab() {
        NewAppointmentTab newAppointmentTab = new NewAppointmentTab();
        Tab tab = newAppointmentTab.createNewAppointmentTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }

}
