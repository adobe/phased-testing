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

public class MyNonInterruptiveClosureEvent extends NonInterruptiveEvent {
    public static final int WAIT_TIME_MS = 500;

    private int taskContainer;
    private static final Logger log = LogManager.getLogger();

    public MyNonInterruptiveClosureEvent() {

        taskContainer = 0;
    }

    @Override
    public boolean startEvent() {
        taskContainer = 1;
        log.info("started");
        return true;
    }

    @Override
    public boolean isFinished() {
        return taskContainer < 0;
    }

    @Override
    public boolean waitTillFinished() {

        while (taskContainer != -1) {
            try {
                Thread.sleep(WAIT_TIME_MS);
                taskContainer=-1;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return isFinished();
    }

    @Override
    public boolean tearDownEvent() {
        taskContainer--;
        return true;
    }

    public int getTaskContainer() {
        return taskContainer;
    }


}
