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

public class NIESynchronousEvent extends NonInterruptiveEvent {
    public static int WAIT_TIME_MS = 1;
    public static int START_STEP_VALUE = 3;
    public static int WTF_STEP_VALUE = 13;
    public static int TDE_STEP_VALUE = 11;

    private static final Logger log = LogManager.getLogger();

    public NIESynchronousEvent() {
    }

    @Override
    public boolean startEvent()  {
        try {
            log.info("before sleep");
            Thread.sleep(WAIT_TIME_MS);
            TestNIE_Synchroneous.testElement = START_STEP_VALUE;
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
    public boolean waitTillFinished() {
        log.info("In WTF Setting synchronous value to {}", WTF_STEP_VALUE);
        TestNIE_Synchroneous.testElement = WTF_STEP_VALUE;
        return isFinished();
    }

    @Override
    public boolean tearDownEvent() {
        TestNIE_Synchroneous.testElement = TDE_STEP_VALUE;
        return true;
    }

}
