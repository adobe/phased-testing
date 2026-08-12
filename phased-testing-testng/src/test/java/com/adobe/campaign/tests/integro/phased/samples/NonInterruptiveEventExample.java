/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.samples;

import com.adobe.campaign.tests.integro.phased.NonInterruptiveEvent;

public class NonInterruptiveEventExample extends NonInterruptiveEvent {
    @Override
    public boolean startEvent() {
        return false;
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean waitTillFinished() {
        return false;
    }
}
