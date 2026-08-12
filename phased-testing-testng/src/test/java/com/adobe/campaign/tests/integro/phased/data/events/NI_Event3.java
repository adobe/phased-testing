/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.events;

import com.adobe.campaign.tests.integro.phased.NonInterruptiveEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NI_Event3 extends NonInterruptiveEvent {
    protected static Logger log = LogManager.getLogger();

    public NI_Event3() throws IllegalAccessException {
        throw new IllegalAccessException("Forced Exception");
    }


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
