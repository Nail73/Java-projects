package com.saucedemo;

import com.saucedemo.core.BaseSeleniumPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class Overview extends BaseSeleniumPage {

    @FindBy(id = "finish")
    private WebElement finish;

    public Overview() {
        PageFactory.initElements(BaseSeleniumPage.driver, this);
    }

    public Complete end() {
        finish.click();
        return new Complete();
    }
}
