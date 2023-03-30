package com.saucedemo;

import com.saucedemo.core.BaseSeleniumPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class YourCart extends BaseSeleniumPage {

    @FindBy(id = "checkout")
    private WebElement checkout;

    public YourCart() {
        PageFactory.initElements(BaseSeleniumPage.driver,this);
    }

    public ClickCheckout pay (){
        checkout.click();
        return new ClickCheckout();
    }
}
