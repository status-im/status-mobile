package utility;


import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;


import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;


public abstract class AppiumDriverBuilder<SELF, DRIVER extends AppiumDriver> {

    public  String jobName = System.getenv("JOB_NAME");
    public  String buildNumber = System.getenv("BUILD_NUMBER");
    protected URL endpoint;
    DesiredCapabilities capabilities = new DesiredCapabilities();

    public static AndroidDriverBuilder forAndroid() throws MalformedURLException {
        return new AndroidDriverBuilder();
    }

    public static class AndroidDriverBuilder extends AppiumDriverBuilder<AndroidDriverBuilder, AndroidDriver> {



    public AndroidDriver build()  {
        capabilities.setCapability("appiumVersion", "1.6.3");
        capabilities.setCapability("deviceName","Samsung Galaxy S4 Emulator");
        capabilities.setCapability("deviceOrientation", "portrait");
        capabilities.setCapability("browserName", "");
        capabilities.setCapability("platformVersion","4.4");
        capabilities.setCapability("platformName","Android");

        jobName = (null == jobName) ? "Local run " : jobName;
        buildNumber = (null == buildNumber) ? new Timestamp(System.currentTimeMillis()).toString() : buildNumber;
        capabilities.setCapability("build", jobName + "__" + buildNumber);

        //read url to apk file from Jenkins property or from maven parameter
        // example of maven run: mvn -DapkUrl=http://artifacts.status.im:8081/artifactory/nightlies-local/im.status.ethereum-baebbe.apk test
        capabilities.setCapability("app", System.getProperty("apkUrl"));
        return new AndroidDriver<WebElement>(endpoint, capabilities);
        }
    }

    public SELF withEndpoint(URL endpoint) {
        this.endpoint = endpoint;

        return (SELF) this;
    }

    public abstract DRIVER build();

}

