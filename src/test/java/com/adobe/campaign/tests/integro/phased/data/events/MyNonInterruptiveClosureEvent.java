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
