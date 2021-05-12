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
package com.adobe.campaign.tests.integro.phased.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.adobe.campaign.tests.integro.core.utils.GeneralTestUtils;
import com.adobe.campaign.tests.integro.phased.PhasedDataBroker;

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
