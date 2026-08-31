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
import com.adobe.campaign.tests.integro.phased.data.mutational.TestMutationalNIE_Synchroneous;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NIEMutationalSynchronousEventWithException extends NonInterruptiveEvent {
    public static int WAIT_TIME_MS = 1;
    public static int START_STEP_VALUE = 3;
    public static int WTF_STEP_VALUE = 13;
    public static int TDE_STEP_VALUE = 11;
    public static int exceptionPlace = 0;

    private static final Logger log = LogManager.getLogger();

    public NIEMutationalSynchronousEventWithException() {
    }

    @Override
    public boolean startEvent()  {
        try {
            log.info("before Exceptions");
            Thread.sleep(WAIT_TIME_MS);
            TestMutationalNIE_Synchroneous.testElement = START_STEP_VALUE;

            if (exceptionPlace == 1) {
                throw new RuntimeException("Exception in startEvent");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        log.info("started");
        return true;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

    @Override
    public boolean waitTillStarted() {
        log.info("In WTF Setting synchronous value to {}", WTF_STEP_VALUE);

        if (exceptionPlace == 2) {
            throw new RuntimeException("Exception in WTF");
        }

        TestMutationalNIE_Synchroneous.testElement = WTF_STEP_VALUE;
        return isFinished();
    }

    @Override
    public boolean tearDownEvent() {
        TestMutationalNIE_Synchroneous.testElement = TDE_STEP_VALUE;

        if (exceptionPlace == 3) {
            throw new RuntimeException("Exception in TDE");
        }

        return true;
    }

}
