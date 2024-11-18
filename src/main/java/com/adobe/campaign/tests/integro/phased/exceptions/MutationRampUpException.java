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
public class MutationRampUpException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = -5305055623086270877L;

    public MutationRampUpException(String in_msg, Throwable e) {
        super(in_msg, e);
    }

    public MutationRampUpException(String in_msg) {
        super(in_msg);
    }

    public MutationRampUpException() {
        this("Unexpected Expection occurred when raping up the mutation tests.");
    }
}
