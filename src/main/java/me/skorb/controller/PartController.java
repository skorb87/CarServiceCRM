package me.skorb.controller;

import javafx.scene.control.Tab;
import me.skorb.event.EventBus;
import me.skorb.event.OpenPartsTabEvent;
import me.skorb.view.tabs.PartsTab;
import me.skorb.view.tabs.TabManager;

public class PartController {

    public PartController() {
        EventBus.addListener(OpenPartsTabEvent.class, event -> openPartsTab());
    }

    private void openPartsTab() {
        PartsTab partsTab = new PartsTab();
        Tab tab = partsTab.createPartsTab();
        tab.setClosable(true);
        TabManager.getInstance().getTabPane().getTabs().add(tab);
        TabManager.getInstance().getTabPane().getSelectionModel().select(tab);
    }


}
