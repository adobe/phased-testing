package com.adobe.campaign.tests.integro.phased.data.events;

import com.adobe.campaign.tests.integro.phased.NonInterruptiveEvent;

import java.util.Date;

public class MyNonInterruptiveEvent extends NonInterruptiveEvent {
    public static final int WAIT_TIME_MS = 500;

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
        return eventThread.isAlive();
    }

    @Override
    public boolean isFinished() {
        return !eventThread.isAlive();
    }

    @Override
    public boolean waitTillFinished() {
        while (eventThread.isAlive()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return isFinished();
    }
}
