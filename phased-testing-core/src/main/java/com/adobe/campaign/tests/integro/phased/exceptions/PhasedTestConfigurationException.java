/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.exceptions;

/**
 * Exceptions that are thrown when preparing the tests. Exceptions of this type
 * should be thrown before the tests are run
 *
 * Author : gandomi
 *
 */
public class PhasedTestConfigurationException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = -5305055623086270877L;

    public PhasedTestConfigurationException(String in_msg, Throwable e) {
        super(in_msg, e);
    }

    public PhasedTestConfigurationException(String in_msg) {
        super(in_msg);
    }

    public PhasedTestConfigurationException() {
        this("Unexpected Phased Expection");
    }
}
