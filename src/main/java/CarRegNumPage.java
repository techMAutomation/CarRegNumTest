import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

public class CarRegNumPage extends ReadFiles {

    /** I have defined WebElements, but these are not working */

    @FindBy(id = "vrm-input")
    private WebElement carRegistrationTextField;
    @FindBy(xpath = "//form/a")
    private WebElement fullHistoryCheckButton;

    private static WebDriver webDriver;
    private String inputValue;

    /**  - Test Initialisation
     * @throws InterruptedException
     */
    @BeforeTest
    public void initializeTest() throws InterruptedException {

        // Reads input file and gets all the Reg Pattern matched Car RegNums
        readInputFile();

        // Initialises the Chrome Driver
        WebDriverManager.chromedriver().setup();
        webDriver = new ChromeDriver();
        webDriver.manage().window().maximize();

        // Launches the site
        webDriver.get("https://cartaxcheck.co.uk");
        Thread.sleep(120);

        // Waiting until to find the RegNum TextField
        WebDriverWait wait = new WebDriverWait(webDriver, 180);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("vrm-input")));
    }

    /**
     * In this website, in the same page entering different REG Num is not replacing it in the end of the url
     * and so everytime passing the new reg num in the url
     * @param regName
     * @return
     */
    private String getUpdatedUrl(String regName) {
        System.out.println(webDriver.getCurrentUrl());
        String[] url = webDriver.getCurrentUrl().split("=");
        url[1] = "=" + regName;
        String pageUrl = url[0] + url[1];
        System.out.println(" Updated PageUrl :: " + pageUrl);
        return pageUrl;
    }

    /**
     * Enters Car RegNumber
     * @param regNum
     * @throws InterruptedException
     */
    private void enterCarRegistrationNumber(String regNum) throws InterruptedException {
        Thread.sleep(120);
        webDriver.findElement(By.id("vrm-input")).click();
        webDriver.findElement(By.id("vrm-input")).clear();
        webDriver.findElement(By.id("vrm-input")).sendKeys(regNum);
        inputValue = webDriver.findElement(By.id("vrm-input")).getAttribute("value");
        System.out.println(" TextField value :: " + inputValue);
    }

    /**
     * Clicks on 'Full History Check' button in the WebSite
     * @throws InterruptedException
     */
    public void clickFullHistoryCheckButton() throws InterruptedException {
        Thread.sleep(120);

        // If the Size of the WebElements is greater than Zero then only clicks on the button
        if (webDriver.findElements(By.xpath("//form/a")).size() > 0) {
            webDriver.findElement(By.xpath("//form/a")).click();
            System.out.println(" -- Clicked on 'Full History Check' button -- ");
            Thread.sleep(100);
        }
    }

    /**
     * Based on Input file gets 'RegNum' based on Reg Pattern and inputs to the Site
     * @param regNum
     * @return
     * @throws InterruptedException
     */
    public boolean getCarAttributesAndCompareWithOutputFile(String regNum) throws InterruptedException {
        boolean check = false;

        // If the RegNum contains SPACES then joining it
        if (regNum.contains(" ")) {
            String[] newReg = regNum.split(" ");
            regNum = newReg[0] + newReg[1];
            System.out.println(" -- Removed spaces in RegNum :: " + regNum);
        }

        enterCarRegistrationNumber(regNum);
        clickFullHistoryCheckButton();
        Thread.sleep(200);

        // If the RegNum matches then CarAttributes list will be passed to readOutFile. If it matches then returns true
        try {
          if (webDriver.findElements(By.xpath("//*[@id=\"m\"]/div[2]/div/div/div[1]/div/p")).size() == 1) {
            String carAttributes = webDriver.findElement(By.xpath("//*[@id=\"m\"]/div[2]/div/div/div[1]/div/p")).getText();
            if (!(carAttributes.length() == 0)) {
                System.out.println(" -- Car attributes :: " + carAttributes);
                String carAttr = getCarAttributes(carAttributes);
                check = readOutputFile(carAttr);
            }
          }
        } catch (Exception e) {
            // If the RegNum doesn't not match then CarAttributes list will not be displayed then returns false
            Thread.sleep(150);
            if (webDriver.findElements(By.xpath("//h5/span[contains(text(), 'Vehicle Not Found')]")).size() > 0) { }
        }
        return check;
    }

    /**
     * Reorder Car Attributes retrieved from WebSite when RegNum matches
     * @param originalStr
     * @return
     */
    private String getCarAttributes(String originalStr) {
        String split[] = originalStr.split(" ", 4);
        String newSt = inputValue + "," + split[2] + "," + split[3].trim() + "," + split[0] + "," + split[1];
        System.out.println(" -- Car Attributes updated string :: " + newSt);
        return newSt;
    }

    /**
     * Closes the WebDriver after all the tests are run
     */
    @AfterTest
    public void closeDriver() {
        if (webDriver != null) {
            webDriver.quit();
            webDriver = null;
            System.out.println(" ** Closed WebDriver ** ");
        }
    }

    /**
     * Runs each test and returns result
     * @throws InterruptedException
     */
    @Test
    public void TestResults() throws InterruptedException {

        System.out.println(" ************* TEST 1 ************ ");
        assertTrue(getCarAttributesAndCompareWithOutputFile("DN09HRM"));

        System.out.println(" ************* TEST 2 ************ ");
        webDriver.get(getUpdatedUrl("KT17DLX"));
        Thread.sleep(120);
        assertTrue(getCarAttributesAndCompareWithOutputFile("KT17DLX"));

        System.out.println(" ************* TEST 3 ************ ");
        webDriver.get(getUpdatedUrl("SG18HTN"));
        webDriver.navigate().refresh();
        Thread.sleep(120);
        assertTrue(getCarAttributesAndCompareWithOutputFile("SG18 HTN"));

        System.out.println(" ************* TEST 4 ************ ");
        webDriver.get(getUpdatedUrl("BW57BOW"));
        webDriver.navigate().refresh();
        Thread.sleep(120);
        assertFalse(getCarAttributesAndCompareWithOutputFile("BW57 BOW"));
    }
}
