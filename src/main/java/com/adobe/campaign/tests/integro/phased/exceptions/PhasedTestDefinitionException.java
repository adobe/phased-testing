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
 * Exceptions that are thrown when detecting issues in the test definitions. Exceptions of this type
 * should be thrown before the tests are run
 *
 * Author : gandomi
 *
 */
public class PhasedTestDefinitionException extends RuntimeException {

    public PhasedTestDefinitionException(String in_msg, Throwable e) {
        super(in_msg, e);
    }

    public PhasedTestDefinitionException(String in_msg) {
        super(in_msg);
    }



}
