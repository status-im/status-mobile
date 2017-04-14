package tests;

import app.StatusApp;
import com.saucelabs.common.SauceOnDemandSessionIdProvider;
import io.appium.java_client.android.AndroidDriver;
import org.junit.*;
import org.junit.rules.TestName;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import utility.AppiumDriverBuilder;
import java.net.MalformedURLException;
import java.net.URL;
import com.saucelabs.junit.SauceOnDemandTestWatcher;
import com.saucelabs.common.SauceOnDemandAuthentication;

public abstract class AbstractTest implements SauceOnDemandSessionIdProvider {

    private AndroidDriver<WebElement> driver;
    protected  StatusApp app;

    public static final String USERNAME = System.getenv("SAUCE_USERNAME");
    public static final String ACCESS_KEY = System.getenv("SAUCE_ACCESS_KEY");
    public static final String URL = "https://" + USERNAME + ":" + ACCESS_KEY + "@ondemand.saucelabs.com:443/wd/hub";
   // public  String buildTag = System.getenv("JOB_NAME") + "__" + System.getenv("BUILD_NUMBER");

    protected  String sessionId;

    public SauceOnDemandAuthentication authentication = new SauceOnDemandAuthentication(USERNAME, ACCESS_KEY);

    @Rule
    public SauceOnDemandTestWatcher resultReportingTestWatcher = new SauceOnDemandTestWatcher(this, authentication);

    @Rule
    public TestName name = new TestName() {
        public String getMethodName() {
            return String.format("%s", super.getMethodName());
        }
    };


    @Before
    public  void SetupRemote() throws MalformedURLException {
        driver = AppiumDriverBuilder.forAndroid()
                .withEndpoint(new URL(URL))
                .build();

        app = new StatusApp(driver);
        this.sessionId = (((RemoteWebDriver) driver).getSessionId()).toString();
    }

    @After
    public  void teardown(){
        //close the app
        driver.quit();
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

}
