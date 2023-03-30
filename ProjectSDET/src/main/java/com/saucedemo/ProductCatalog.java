package com.saucedemo;

import com.saucedemo.core.BaseSeleniumPage;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ProductCatalog extends BaseSeleniumPage {
    @FindBy(xpath = "//*[@id=\"add-to-cart-sauce-labs-backpack\"]")
    private WebElement addToCart;
    @FindBy(xpath = "//*[@id=\"shopping_cart_container\"]/a")
    private WebElement clickBasket;


    public ProductCatalog() {
        PageFactory.initElements(BaseSeleniumPage.driver, this);
    }

    public YourCart findProduct() {
        addToCart.click();
        clickBasket.click();
        return new YourCart();
    }
}
