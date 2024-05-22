package pickleib.mobile.driver;

import context.ContextStore;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.remote.options.BaseOptions;
import org.json.simple.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;
import pickleib.driver.DriverFactory;
import utils.Printer;
import java.net.URL;

import static pickleib.mobile.driver.ServiceFactory.service;
import static utils.StringUtilities.Color.*;
import static utils.StringUtilities.*;

public class AppiumDriverFactory implements DriverFactory {

    static Printer log = new Printer(AppiumDriverFactory.class);
    static String deviceName;

    public static AppiumDriver getDriver(String deviceName, JSONObject capabilities){
        AppiumDriverFactory.deviceName = deviceName;
        if (Boolean.parseBoolean(String.valueOf(capabilities.get("remote")))) return getRemoteDriver(capabilities);
        else return getDriver(capabilities);
    }

    public static AppiumDriver getDriver(JSONObject capabilities){
        DesiredCapabilities desiredCapabilities = getConfig(capabilities);
        desiredCapabilities.setCapability("app", contextCheck("UPLOAD-" + capabilities.get("app")));
        try {
            URL url;
            if (service == null) {
                String address = ContextStore.get("address", "0.0.0.0");
                String port = ContextStore.get("port", "4723");
                url = new URL("http://" + address + ":" + port + "/wd/hub");
            }
            else url = service.getUrl();
            log.important(deviceName + markup(GRAY, " was selected"));
            return new AppiumDriver(url, desiredCapabilities);
        }
        catch (Exception gamma) {
            if(gamma.toString().contains("Could not start a new session. Possible causes are invalid address of the remote server or browser start-up failure")){
                log.info("Please make sure " + markup(PURPLE, "Appium ") + "is on & verify the port that its running on at 'resources/test.properties'.");
                throw new RuntimeException(markup(YELLOW, gamma.getMessage()));
            }
            else throw new RuntimeException(markup(YELLOW, "Something went wrong while selecting a driver") + "\n" + markup(RED, gamma.getMessage()));
        }
    }

    public static AppiumDriver getRemoteDriver(JSONObject capabilities){
        BaseOptions baseOptions = getBaseOptions(capabilities);
        try {
            String userName = String.valueOf(baseOptions.getCapability("username"));
            String accessKey = String.valueOf(baseOptions.getCapability("access_key"));

            String urlString = String.format("https://%s:%s@hub.browserstack.com/wd/hub", userName , accessKey);
            log.info("Url: " + highlighted(BLUE, urlString));
            URL url = new URL(urlString);
            log.important(deviceName + markup(GRAY, " was selected"));
            return new AppiumDriver(url, baseOptions);
        }
        catch (Exception gamma) {
            if(gamma.toString().contains("Could not start a new session. Possible causes are invalid address of the remote server or browser start-up failure")){
                log.info("Please make sure " + markup(PURPLE, "Appium ") + "is on & verify the port that its running on at 'resources/test.properties'.");
                throw new RuntimeException(markup(YELLOW, gamma.getMessage()));
            }
            else throw new RuntimeException(markup(YELLOW, "Something went wrong while selecting a driver") + "\n" + markup(RED, gamma.getMessage()));
        }
    }

    public static DesiredCapabilities getConfig(JSONObject capabilities) {
        log.info("Setting capabilities...");
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        for (Object key : capabilities.keySet()) desiredCapabilities.setCapability((String) key, capabilities.get(key));
        return desiredCapabilities;
    }

    public static BaseOptions getBaseOptions(JSONObject capabilities){
        BaseOptions options = new BaseOptions();
        for (Object key : capabilities.keySet()) options.setCapability((String) key, capabilities.get(key));
        return options;
    }
}
