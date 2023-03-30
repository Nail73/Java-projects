package com.saucedemo;

import com.saucedemo.core.BaseSeleniumTest;
import io.qameta.allure.AllureId;
import org.junit.jupiter.api.Test;

public class CheckingSiteWithFailedAuthorization extends BaseSeleniumTest {

    @Test
    public void checkLoginError() {
        String login = "test";
        String password = "test";
        LoginPageError loginPageError = new LoginPageError();
        loginPageError.auth(login, password);
    }
}
