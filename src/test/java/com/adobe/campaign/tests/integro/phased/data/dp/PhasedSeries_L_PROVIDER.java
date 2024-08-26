/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.dp;

import org.testng.annotations.DataProvider;

public class PhasedSeries_L_PROVIDER {
    public static final String PROVIDER_B = "provider_b";
    public static final String PROVIDER_A = "provider_a";

    @DataProvider(name = "create")
    public Object[][] createData() {
        return new Object[][] {{PROVIDER_A},{PROVIDER_B}};
    }
    
    @DataProvider(name = "createPrivate")
    private Object[][] createDataPrivate() {
        return new Object[][] {{PROVIDER_A},{PROVIDER_B}};
    }
}
