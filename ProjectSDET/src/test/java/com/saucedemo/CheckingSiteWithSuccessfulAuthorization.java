package com.saucedemo;

import com.saucedemo.core.BaseSeleniumTest;
import org.junit.jupiter.api.Test;

public class CheckingSiteWithSuccessfulAuthorization extends BaseSeleniumTest {

    @Test
    public void checkLoginAndBuy() {
        String login = "standard_user";
        String password = "secret_sauce";
        LoginPage loginPage = new LoginPage();
        loginPage.auth(login, password).findProduct().pay().createOrder("firstname", "lastname", "test").end().goodBuy();
    }
}
