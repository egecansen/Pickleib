package pickleib.web.utilities;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v115.log.Log;
import org.openqa.selenium.devtools.v85.network.Network;
import pickleib.web.driver.PickleibWebDriver;
import utils.PropertyUtility;
import java.util.Optional;
import static pickleib.utilities.element.ElementAcquisition.strUtils;
import static pickleib.web.driver.PickleibWebDriver.driver;
import static pickleib.web.driver.PickleibWebDriver.log;
import static utils.StringUtilities.Color.PURPLE;

public class PickleibLogUtils extends Thread {

    static boolean networkEvents = Boolean.parseBoolean(PropertyUtility.getProperty("networkEvents", "false"));

    @Override
     public void run() {
        if (networkEvents) monitorNetworkEvents((ChromeDriver) driver);
     }

    public static void monitorNetworkEvents(ChromeDriver driver) {
        DevTools devTools = (driver).getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.send(Log.enable());
        devTools.addListener(Network.requestWillBeSent(), request -> {
            log.info("Request URL: " + strUtils.highlighted(PURPLE, request.getRequest().getUrl()));
        });
        devTools.addListener(Network.requestWillBeSent(), request -> {
            log.info("Request Method: " + strUtils.highlighted(PURPLE, request.getRequest().getMethod()));
        });
        devTools.addListener(Network.requestWillBeSent(), request -> {
            log.info("Request Headers: "); request.getRequest().getHeaders().toJson().forEach((k, v) -> log.info(strUtils.markup(PURPLE, k + ":" + v)));
        });
    }
}
