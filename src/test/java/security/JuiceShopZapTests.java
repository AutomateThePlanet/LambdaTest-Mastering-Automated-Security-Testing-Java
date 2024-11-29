package security;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import security.ZAPService;

import java.time.Duration;

class JuiceShopZapTests {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://juice-shop.herokuapp.com/";

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--proxy-server=http://localhost:8088");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();

        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);

        // Dismiss welcome banner
        driver.manage().deleteAllCookies();
        driver.navigate().refresh();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testCrawlJuiceShopForSecurityProblems() {
        // Interact with the Juice Shop page
        clickElement(By.xpath("//mat-icon[normalize-space(text())='search']"));
        WebElement searchElement = waitForElement(By.xpath("//*[@id='mat-input-0']"));
        new Actions(driver).moveToElement(searchElement).sendKeys("Apple Juice").perform();
        clickElement(By.xpath("//img[@alt='Apple Juice (1000ml)']"));

        // Start ZAP scan and generate report
        ZAPService.scanCurrentPage(URL);
        ZAPService.generateHtmlReport("ZAP_Scan_Report.html");

        // Assertions
        ZAPService.assertAlertsArePresent();
        ZAPService.assertNoHighRiskAlerts();
        ZAPService.assertAllAlertsHaveSolutions();
//        ZAPService.assertAlertsBelowRiskLevel("Medium");
    }

    private void clickElement(By locator) {
        WebElement element = waitForElement(locator);
        element.click();
    }

    private WebElement waitForElement(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }
}