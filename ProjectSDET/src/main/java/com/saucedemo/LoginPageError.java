package com.saucedemo;

import com.saucedemo.core.BaseSeleniumPage;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class LoginPageError extends BaseSeleniumPage {
    @FindBy(css = "#user-name")
    private WebElement loginField;

    @FindBy(css = "#password")
    private WebElement passwordField;

    public LoginPageError() {
        BaseSeleniumPage.driver.get("https://www.saucedemo.com/");
        PageFactory.initElements(BaseSeleniumPage.driver, this);
    }

    public String auth(String login, String password) {
        loginField.sendKeys(login);
        passwordField.sendKeys(password, Keys.ENTER);
        return "Epic sadface: Username and password do not match any user in this service";
    }

}
