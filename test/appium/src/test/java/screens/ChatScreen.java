package screens;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.By;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.util.Assert;


public class ChatScreen extends AbstractScreen {

    public ChatScreen(AppiumDriver<WebElement> driver){
        super(driver);
    }


    @AndroidFindBy(id="android:id/button1")
    public WebElement btnContinue;

    @AndroidFindBy(accessibility="request-password")
    public WebElement requestPassword;

    @AndroidFindBy(accessibility="chat-message-input")
    public WebElement inputChatMessage;

    @AndroidFindBy(accessibility="chat-send-button")
    public WebElement btnSend;

    @AndroidFindBy(accessibility="request-phone")
    public WebElement btnRequestPhone;

    @AndroidFindBy(accessibility="chat-cancel-response-button")
    public WebElement btnCancelPasswordRequest;

    public void continueForRootedDevice()
    {
        if (isElementPresent(By.id("android:id/button1"))) {
            btnContinue.click();
        }
    }

    public void createPassword(String password){
        requestPassword.click();
        for (int i=0; i<2; i++){
            inputChatMessage.sendKeys(password);
            btnSend.click();
        }
    }

    public void verifyPasswordIsSet(){
        //button "tap to enter phone number" is shown
        wait.until(ExpectedConditions.elementToBeClickable(btnRequestPhone));

        //screen contains text "Phew that was hard" TODO: replace hardcoded string "Find a bug" check
        Assert.isTrue(driver.getPageSource().contains("Find a bug"),"Text Phew that was hard is not found on screen");
    }

    public void verifyPasswordRequestIsVisible(){
        //button "tap to enter phone number" is shown
        wait.until(ExpectedConditions.elementToBeClickable(requestPassword));
        //screen contains text "Phew that was hard" TODO: replace hardcoded string "Welcome to Status " check
        Assert.isTrue(driver.getPageSource().contains("Welcome to Status"),"Welcome to Status is not found on screen");
    }


    public boolean isElementPresent(By locator) {
        try {
            return driver.findElements(locator).size() > 0;
        } catch (InvalidSelectorException ex) {
            throw ex;
        }
    }
}