import org.bouncycastle.jcajce.provider.asymmetric.EC;
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class RegistrationFormTest {
    private WebDriver driver;

    @BeforeClass
    public void setUp() {

        System.setProperty("webdriver.http.factory", "jdk-http-client");
        // Установка пути к драйверу Chrome
        System.setProperty("webdriver.chrome.driver", "C:/Users/oper91/Downloads/chromedriver-win32/chromedriver.exe");

        // Создание экземпляра ChromeDriver
        driver = new ChromeDriver();

        // Максимизация окна браузера
        driver.manage().window().maximize();

        // Ожидание элементов не дольше 10 секунд
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    }

    @Test
    public void fillRegistrationFormTest() {
        // Переход на страницу входа
        driver.get("https://demoqa.com/automation-practice-form");

        // Заполнение полей формы
        driver.findElement(By.id("firstName")).sendKeys("John");
        driver.findElement(By.id("lastName")).sendKeys("Doe");
        driver.findElement(By.id("userEmail")).sendKeys("johndoe@example.com");
        driver.findElement(By.cssSelector("[for='gender-radio-1']")).click();
        driver.findElement(By.id("userNumber")).sendKeys("0123456789");
        driver.findElement(By.id("subjectsInput")).sendKeys("Math");
        WebDriverWait wait = null;
        wait.until(ExpectedConditions.elementToBeClickable(By.id("hobbies-checkbox-1"))).click();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("hobbies-checkbox-1"))).click();
        driver.findElement(By.id("uploadPicture")).sendKeys("C:/Users/oper91/Downloads/chromedriver-win32/img.jpg");
        driver.findElement(By.id("currentAddress")).sendKeys("123 Main St");
        driver.findElement(By.id("state")).click();
        driver.findElement(By.cssSelector("#state .css-1hwfws3")).click();
        driver.findElement(By.id("city")).click();
        driver.findElement(By.cssSelector("#city .css-26l3qy-menu")).click();
        driver.findElement(By.id("submit")).click();

        // Ожидание появления всплывающего окна
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("example-modal-sizes-title-lg")));

        // Проверка заголовка всплывающего окна
        WebElement popupTitle = driver.findElement(By.id("submit"));
        Assert.assertEquals(popupTitle.getText(), "Thanks for submitting the form");

        // Проверка значений в таблице на окне
        String submittedFirstName = driver.findElement(By.cssSelector("body > div.fade.modal.show > div > div > div.modal-body > div > table > tbody > tr:nth-child(1) > td:nth-child(1)")).getText();
        Assert.assertEquals(submittedFirstName, "John");

        String submittedLastName = driver.findElement(By.cssSelector("body > div.fade.modal.show > div > div > div.modal-body > div > table > tbody > tr:nth-child(1) > td:nth-child(2)")).getText();
        Assert.assertEquals(submittedLastName, "Doe");

        String submittedEmail = driver.findElement(By.cssSelector("body > div.fade.modal.show > div > div > div.modal-body > div > table > tbody > tr:nth-child(2) > td:nth-child(2)")).getText();
        Assert.assertEquals(submittedEmail, "johndoe@example.com");
    }

    @AfterClass
    public void tearDown() {
        // Закрытие браузера
        driver.quit();
    }
}