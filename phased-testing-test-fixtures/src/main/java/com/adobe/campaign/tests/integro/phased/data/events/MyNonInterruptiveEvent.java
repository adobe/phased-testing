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

import java.util.Date;

public class MyNonInterruptiveEvent extends NonInterruptiveEvent {
    public static final int WAIT_TIME_MS = 500;
    private Thread eventThread;
    private static final Logger log = LogManager.getLogger();

    public MyNonInterruptiveEvent() {

        eventThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(WAIT_TIME_MS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public boolean startEvent() {
        eventThread.start();
        log.info("started");
        return eventThread.isAlive();
    }

    @Override
    public boolean isFinished() {
        return !eventThread.isAlive();
    }

    @Override
    public boolean waitTillFinished() {
        log.info("finishing");

        try {
            if (eventThread.isAlive()) {
                this.eventThread.join();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return isFinished();
    }
}
