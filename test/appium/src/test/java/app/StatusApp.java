package app;

import io.appium.java_client.AppiumDriver;
import screens.ChatScreen;


public class StatusApp {

    private final AppiumDriver driver;

    public StatusApp(AppiumDriver driver) {
        this.driver = driver;
    }

    public ChatScreen ChatScreen() { return new ChatScreen(driver); }

}
