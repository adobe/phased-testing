/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

import java.io.File;

public interface PhasedDataBroker {

    /**
     * This method should store the given file in the location you have designated.
     *
     * Author : gandomi
     *
     * @param in_file A file in which we should store the phase cache
     *
     */
    void store(File in_file);

    /**
     * This method will fetch the file name from the location you have designated.
     *
     * Author : gandomi
     *
     * @param in_fileName A path to the file from which we are to fetch the phase cache for the current phase
     * @return The file that was fetched
     *
     */
    File fetch(String in_fileName);

}