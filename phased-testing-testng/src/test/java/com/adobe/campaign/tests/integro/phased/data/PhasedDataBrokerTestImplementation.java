/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adobe.campaign.tests.integro.phased.PhasedDataBroker;
import com.adobe.campaign.tests.integro.phased.utils.GeneralTestUtils;

public class PhasedDataBrokerTestImplementation implements PhasedDataBroker {
    File cacheDir;
    
    protected static Logger log = LogManager.getLogger();
    
    public PhasedDataBrokerTestImplementation() {
        cacheDir = GeneralTestUtils.createCacheDirectory("dataBroker");
    }
    
    /**
     * This method should store the given file in the location you have designated.
     *
     * Author : gandomi
     *
     * @param in_file
     *
     */
    @Override
    public void store(File in_file) {
        File l_tempFile = GeneralTestUtils.createEmptyCacheFile(cacheDir, in_file.getName());
        
        try {
            Files.copy(Paths.get(in_file.getPath()), Paths.get(l_tempFile.getPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    /**
     * This method will fetch the file name from the location you have designated.
     *
     * Author : gandomi
     *
     * @param in_fileName
     * @return
     *
     */
    @Override
    public File fetch(String in_fileName) {
        
        return new File(cacheDir,in_fileName);
    }

    
    /**
     * Just for test purposes
     *
     * Author : gandomi
     *
     * @param iin_stdStoreFile
     *
     */
    public void deleteData(String iin_stdStoreFile) {
        File l_myFile = new File(cacheDir, iin_stdStoreFile);
        if (l_myFile.exists()) {
            l_myFile.delete();
        } else {
            log.warn("The giiven file "+iin_stdStoreFile+" was not found.");
        }
        
    }

}
