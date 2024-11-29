package security;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class JuiceShopZapTestsCloud {
    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://juice-shop.herokuapp.com/";

    @BeforeAll
    public void setUp() throws Exception {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();

        Map<String, Object> ltOptions = new HashMap<>();
        ltOptions.put("user", System.getenv("LT_USERNAME"));
        ltOptions.put("accessKey", System.getenv("LT_ACCESSKEY"));
        ltOptions.put("resolution", "1920x1080");
        ltOptions.put("platform", "Windows 10");
        ltOptions.put("build", "SecurityTests");
        ltOptions.put("name", "JuiceShopZapTestsCloud");
        ltOptions.put("tunnel", true);

        options.setCapability("LT:Options", ltOptions);

        this.driver = new RemoteWebDriver(new URL("https://hub.lambdatest.com/wd/hub"), options);
        this.driver.manage().window().maximize();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    public void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @Order(1)
    public void testCrawlJuiceShopForSecurityProblems() {
        driver.get(URL);
        waitForElement(By.xpath("//mat-icon[normalize-space(text())='search']")).click();
        WebElement searchElement = waitForElement(By.xpath("//*[@id='mat-input-0']"));
        searchElement.sendKeys("Apple Juice");
        waitForElement(By.xpath("//img[@alt='Apple Juice (1000ml)']")).click();

        ZAPService.scanCurrentPage(URL);
        ZAPService.generateHtmlReport("ZAP_Scan_Report.html");

        ZAPService.assertAlertsArePresent();
        ZAPService.assertNoHighRiskAlerts();
        ZAPService.assertAlertsBelowRiskLevel("Medium");
        ZAPService.assertAllAlertsHaveSolutions();
    }

    private WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}
