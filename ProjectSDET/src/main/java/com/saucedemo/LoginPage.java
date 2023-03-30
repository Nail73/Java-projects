package com.saucedemo;

import com.saucedemo.core.BaseSeleniumPage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPage extends BaseSeleniumPage {
    @FindBy(id = "user-name")
    private WebElement loginField;

    @FindBy(id = "password")
    private WebElement passwordField;

    public LoginPage() {
        BaseSeleniumPage.driver.get("https://www.saucedemo.com/");
        PageFactory.initElements(BaseSeleniumPage.driver, this);
    }

    public ProductCatalog auth(String login, String password) {
        loginField.sendKeys(login);
        passwordField.sendKeys(password, Keys.ENTER);
        return new ProductCatalog();
    }

}
