/*
 * MIT License
 *
 * © Copyright 2020 Adobe. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
