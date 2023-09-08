package pickleib.web.driver;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import pickleib.driver.DriverFactory;
import pickleib.exceptions.PickleibException;
import pickleib.web.utilities.PickleibLogUtils;
import utils.LogUtilities;
import utils.Printer;
import utils.PropertyUtility;
import utils.StringUtilities;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import static utils.StringUtilities.Color.*;

@SuppressWarnings("unused")
public class WebDriverFactory implements DriverFactory {

    static StringUtilities strUtils = new StringUtilities();

    /**
     * determines frameWidth value
     */
    static int frameWidth = Integer.parseInt(PropertyUtility.getProperty("frame-width","1920"));

    /**
     * determines frameHeight value
     */
    static int frameHeight = Integer.parseInt(PropertyUtility.getProperty("frame-height","1080"));

    /**
     * session runs headless if true
     */
    static boolean headless = Boolean.parseBoolean(PropertyUtility.getProperty("headless", "false"));

    /**
     * maximizes a session window if true
     */
    static boolean maximise = Boolean.parseBoolean(PropertyUtility.getProperty("driver-maximize", "false"));

    /**
     * determines driverTimeout duration
     */
    static long driverTimeout = Long.parseLong(PropertyUtility.getProperty("driver-timeout", "15000"))/1000;

    /**
     * cookies are deleted if true
     */
    static boolean deleteCookies = Boolean.parseBoolean(PropertyUtility.getProperty("delete-cookies", "false"));

    /**
     * Selenium Grid is used if true
     */
    static boolean useSeleniumGrid = Boolean.parseBoolean(PropertyUtility.getProperty("selenium-grid", "false"));

    /**
     * enables insecure local host if true
     */
    static boolean insecureLocalHost = Boolean.parseBoolean(PropertyUtility.getProperty("insecure-localhost", "false"));

    /**
     * disables browser notifications if true
     */
    static boolean disableNotifications = Boolean.parseBoolean(PropertyUtility.getProperty("disable-notifications", "true"));

    /**
     * determines page load strategy
     */
    static PageLoadStrategy loadStrategy = PageLoadStrategy.fromString(PropertyUtility.getProperty("load-strategy", "normal"));

    /**
     * determines usage of web driver manager
     */
    static Boolean useWDM = Boolean.parseBoolean(PropertyUtility.getProperty("web-driver-manager", "false"));

    /**
     * determines usage of web driver manager
     */
    static Boolean allowRemoteOrigin = Boolean.parseBoolean(PropertyUtility.getProperty("allow-remote-origin", "true"));;

    /**
     * The logging level used by Pickleib.
     * This value can be set in the properties file with the key "selenium-log-level".
     * If not specified in the properties file, the default value is "off".
     */
    static String logLevel = PropertyUtility.getProperty("selenium-log-level", "off");;

    /**
     * The URL of the Selenium Grid hub.
     * This value can be set in the properties file with the key "hub-url".
     * If not specified in the properties file, the default value is an empty string.
     */
    static String hubUrl = PropertyUtility.getProperty("hub-url","");

    /**
     * The browser used for tests.
     * This value can be set in the properties file with the key "browser".
     * If not specified in the properties file, the default value is "chrome".
     */
    static String browser = PropertyUtility.getProperty("browser", "chrome");

    /**
     * DriverFactory Logger.
     */
    private static final Printer log = new Printer(WebDriverFactory.class);

    /**
     * Logging utilities.
     */
    private static final LogUtilities logUtils = new LogUtilities();

    /**
     * Initializes and returns a driver of specified type
     * @param browserType driver type
     * @return returns driver
     */
    public static RemoteWebDriver getDriver(BrowserType browserType){
        RemoteWebDriver driver;

        try {
            if (browserType == null) browserType = BrowserType.fromString(browser);

            if (useSeleniumGrid){
                ImmutableCapabilities capabilities = new ImmutableCapabilities("browserName", browserType.getDriverKey());
                driver = new RemoteWebDriver(new URL(hubUrl), capabilities);
            }
            else {driver = driverSwitch(headless, useWDM, insecureLocalHost, disableNotifications, allowRemoteOrigin, loadStrategy, browserType);}

            assert driver != null;
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(driverTimeout));
            if (deleteCookies) driver.manage().deleteAllCookies();
            if (maximise) driver.manage().window().maximize();
            else driver.manage().window().setSize(new Dimension(frameWidth, frameHeight));
            driver.setLogLevel(logUtils.getLevel(logLevel));
            log.important(browserType.getDriverName() + GRAY.getValue() + " was selected");
            return driver;
        }
        catch (IOException malformedURLException) {throw new RuntimeException(malformedURLException);}
        catch (Exception gamma) {
            if(gamma.toString().contains("Could not start a new session. Possible causes are invalid address of the remote server or browser start-up failure")){
                log.info("Please make sure the "+PURPLE+"Selenium Grid "+GRAY+"is on & verify the port that its running on at 'resources/test.properties'."+RESET);
                throw new RuntimeException(gamma);
            }
            else
                throw new RuntimeException(strUtils.highlighted(YELLOW, "Something went wrong while selecting a driver ")+strUtils.highlighted(RED, "\n\t"+gamma), gamma);
        }
    }

    /**
     * Selects the driver type and assigns desired capabilities
     *
     * @param headless session runs headless if true
     * @param useWDM WebDriverManager is used if true
     * @param insecureLocalHost enables insecure local host if true
     * @param disableNotifications disables browser notifications if true
     * @param loadStrategy determines page load strategy
     * @param browserType driver type
     * @return returns the configured driver
     */
    static RemoteWebDriver driverSwitch(
            Boolean headless,
            Boolean useWDM,
            Boolean insecureLocalHost, 
            Boolean disableNotifications,
            Boolean allowRemoteOrigin,
            PageLoadStrategy loadStrategy,
            BrowserType browserType){
        if (useWDM) log.warning("Using WebDriverManager...");
        try {
            switch (browserType) {
                case CHROME -> {
                    ChromeOptions chromeOptions = new ChromeOptions();
                    if (disableNotifications) chromeOptions.addArguments("disable-notifications");
                    if (insecureLocalHost){
                        chromeOptions.addArguments("--allow-insecure-localhost");
                        chromeOptions.addArguments("--ignore-certificate-errors");
                    }
                    chromeOptions.setPageLoadStrategy(loadStrategy);
                    chromeOptions.setAcceptInsecureCerts(insecureLocalHost);
                    if (allowRemoteOrigin) chromeOptions.addArguments("--remote-allow-origins=*");
                    if (headless) chromeOptions.addArguments("--headless=new");
                    if (useWDM) WebDriverManager.chromedriver().setup();
                    return new ChromeDriver(chromeOptions);
                }
                case FIREFOX -> {
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (insecureLocalHost){
                        firefoxOptions.addArguments("--allow-insecure-localhost");
                        firefoxOptions.addArguments("--ignore-certificate-errors");
                    }
                    firefoxOptions.setPageLoadStrategy(loadStrategy);
                    firefoxOptions.setAcceptInsecureCerts(insecureLocalHost);
                    if (allowRemoteOrigin) firefoxOptions.addArguments("--remote-allow-origins=*");
                    if (disableNotifications) firefoxOptions.addArguments("disable-notifications");
                    if (headless) firefoxOptions.addArguments("--headless=new");
                    if (useWDM) WebDriverManager.firefoxdriver().setup();
                    return new FirefoxDriver(firefoxOptions);
                }
                case SAFARI -> {
                    SafariOptions safariOptions = new SafariOptions();
                    if (useWDM) WebDriverManager.safaridriver().setup();
                    return new SafariDriver(safariOptions);
                }
                default -> {
                    throw new PickleibException("No such driver was defined.");
                }
            }
        }
        catch (SessionNotCreatedException sessionException){
            log.warning(sessionException.getLocalizedMessage());
            if (!useWDM) return driverSwitch(headless, true, insecureLocalHost, disableNotifications, allowRemoteOrigin, loadStrategy, browserType);
            else return null;
        }
    }

    /**
     * Available driver types
     */
    public enum BrowserType {
        CHROME("Chrome"),
        FIREFOX("Firefox"),
        SAFARI("Safari"),
        OPERA("Opera");

        final String driverName;

        BrowserType(String driverName){
            this.driverName = driverName;
        }

        public String getDriverName() {
            return driverName;
        }
        public String getDriverKey() {
            return driverName.toLowerCase();
        }

        /**
         * Returns a driver type matching a given text (Non-case-sensitive)
         * @param text desired driver
         * @return returns matching a driver type
         */
        public static BrowserType fromString(String text) {
            if (text != null)
                for (BrowserType browserType :values())
                    if (browserType.name().equalsIgnoreCase(text))
                        return browserType;
            return null;
        }
    }

    public static void setFrameWidth(int frameWidth) {
        WebDriverFactory.frameWidth = frameWidth;
    }

    public static void setFrameHeight(int frameHeight) {
        WebDriverFactory.frameHeight = frameHeight;
    }

    public static void setHeadless(boolean headless) {
        WebDriverFactory.headless = headless;
    }

    public static void setMaximise(boolean maximise) {
        WebDriverFactory.maximise = maximise;
    }

    public static void setDriverTimeout(long driverTimeout) {
        WebDriverFactory.driverTimeout = driverTimeout;
    }

    public static void setDeleteCookies(boolean deleteCookies) {
        WebDriverFactory.deleteCookies = deleteCookies;
    }

    public static void setUseSeleniumGrid(boolean useSeleniumGrid) {
        WebDriverFactory.useSeleniumGrid = useSeleniumGrid;
    }

    public static void setInsecureLocalHost(boolean insecureLocalHost) {
        WebDriverFactory.insecureLocalHost = insecureLocalHost;
    }

    public static void setDisableNotifications(boolean disableNotifications) {
        WebDriverFactory.disableNotifications = disableNotifications;
    }

    public static void setLoadStrategy(PageLoadStrategy loadStrategy) {
        WebDriverFactory.loadStrategy = loadStrategy;
    }

    public static void setUseWDM(Boolean useWDM) {
        WebDriverFactory.useWDM = useWDM;
    }

    public static void setAllowRemoteOrigin(Boolean allowRemoteOrigin) {
        WebDriverFactory.allowRemoteOrigin = allowRemoteOrigin;
    }

    public static void setLogLevel(String logLevel) {
        WebDriverFactory.logLevel = logLevel;
    }

    public static void setHubUrl(String hubUrl) {
        WebDriverFactory.hubUrl = hubUrl;
    }

    public static void setBrowser(String browser) {
        WebDriverFactory.browser = browser;
    }

    public static StringUtilities getStrUtils() {
        return strUtils;
    }

    public static int getFrameWidth() {
        return frameWidth;
    }

    public static int getFrameHeight() {
        return frameHeight;
    }

    public static boolean isHeadless() {
        return headless;
    }

    public static boolean isMaximise() {
        return maximise;
    }

    public static long getDriverTimeout() {
        return driverTimeout;
    }

    public static boolean isDeleteCookies() {
        return deleteCookies;
    }

    public static boolean isUseSeleniumGrid() {
        return useSeleniumGrid;
    }

    public static boolean isInsecureLocalHost() {
        return insecureLocalHost;
    }

    public static boolean isDisableNotifications() {
        return disableNotifications;
    }

    public static PageLoadStrategy getLoadStrategy() {
        return loadStrategy;
    }

    public static Boolean getUseWDM() {
        return useWDM;
    }

    public static Boolean getAllowRemoteOrigin() {
        return allowRemoteOrigin;
    }

    public static String getLogLevel() {
        return logLevel;
    }

    public static String getHubUrl() {
        return hubUrl;
    }

    public static String getBrowser() {
        return browser;
    }
}
