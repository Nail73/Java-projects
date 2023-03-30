package com.saucedemo;

import com.saucedemo.core.BaseSeleniumPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ClickCheckout extends BaseSeleniumPage {

    @FindBy(id = "first-name")
    private WebElement firstname;

    @FindBy(id = "last-name")
    private WebElement lastname;

    @FindBy(id = "postal-code")
    private WebElement postalCode;

    @FindBy(id = "continue")
    private WebElement continueClick;

    public ClickCheckout() {
        PageFactory.initElements(BaseSeleniumPage.driver, this);
    }

    public Overview createOrder(String firstnameValue, String lastnameValue, String postalCodeValue) {

        firstname.sendKeys(firstnameValue);
        lastname.sendKeys(lastnameValue);
        postalCode.sendKeys(postalCodeValue);
        continueClick.click();
        return new Overview();
    }
}
