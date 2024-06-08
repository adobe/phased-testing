/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NI_Event1 {
    protected static Logger log = LogManager.getLogger();
    public void execute()  {
        log.info("Running Event");
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
