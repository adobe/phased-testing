/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
/**
 * 
 */
package com.adobe.campaign.tests.integro.phased.exceptions;

/**
 *
 *
 * Author : gandomi
 *
 */
public class PhasedTestException extends RuntimeException {

    public PhasedTestException(String string) {
        super(string);
    }

    public PhasedTestException(String string, Throwable e) {
        super(string,e);
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5403505332132282642L;
    
    

}
