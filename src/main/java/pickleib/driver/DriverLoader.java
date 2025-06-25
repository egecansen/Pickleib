package pickleib.driver;

import collections.Pair;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import pickleib.mobile.driver.PickleibAppiumDriver;
import pickleib.utilities.PropertyLoader;
import pickleib.web.driver.PickleibWebDriver;
import pickleib.web.driver.WebDriverFactory;

public class DriverLoader {

    public DriverLoader() {
        PropertyLoader.load();
    }
    
    public RemoteWebDriver loadWebDriver(){
        PickleibWebDriver.initialize();
        return PickleibWebDriver.get();
    }

    public RemoteWebDriver loadWebDriver(WebDriverFactory.BrowserType browserType){
        PickleibWebDriver.initialize(browserType);
        return PickleibWebDriver.get();
    }
    
    public AppiumDriver loadPlatformDriver(){
        PickleibAppiumDriver.initialize();
        return PickleibAppiumDriver.get();
    }
    
    public void load(DriverFactory.DriverType... driverTypes){
        for (DriverFactory.DriverType type:driverTypes) {
            switch (type){
                case appium -> PickleibAppiumDriver.initialize();
                case selenium -> PickleibWebDriver.initialize();
                default -> throw new EnumConstantNotPresentException(DriverFactory.DriverType.class, type.name());
            }
        }
    }
}
