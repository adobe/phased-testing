package com.adobe.campaign.tests.integro.phased.mutational.data.permutational;

import com.adobe.campaign.tests.integro.phased.PhasedTest;
import com.adobe.campaign.tests.integro.phased.PhasedTestManager;
import com.adobe.campaign.tests.integro.phased.events.PhasedParent;
import org.testng.annotations.Test;

@PhasedTest
@Test(groups = "bbb")
public class ShoppingCartDemo extends PhasedParent {
    public void loginToSite(String param) {
        PhasedTestManager.produce("authToken","123456");
    }

    public void searchProduct(String param) {
        PhasedTestManager.produce("product","product1");
    }

    public void addProductToCart(String param) {
        PhasedTestManager.consume("product");
        PhasedTestManager.produce("cart","cart1");
    }

    public void checkout(String param) {
        PhasedTestManager.consume("authToken");

        PhasedTestManager.consume("cart");
        PhasedTestManager.consume("basket");
    }
}
