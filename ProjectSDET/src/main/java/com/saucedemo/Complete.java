package com.saucedemo;

import com.saucedemo.core.BaseSeleniumPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class Complete extends BaseSeleniumPage {

    @FindBy(xpath = "//*[@id=\"checkout_complete_container\"]/h2")
    private WebElement text;

    public Complete() {
        PageFactory.initElements(BaseSeleniumPage.driver, this);
    }

    public String goodBuy() {
        return "Thank you for your order!";
    }
}
