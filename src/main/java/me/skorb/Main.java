package me.skorb;

import javafx.application.Application;
import me.skorb.view.frames.MainFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("me.skorb.Main: Starting the application...");

        Application.launch(MainFrame.class, args);
    }

}
