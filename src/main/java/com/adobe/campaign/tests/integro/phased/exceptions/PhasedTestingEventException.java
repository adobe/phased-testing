/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.exceptions;

public class PhasedTestingEventException extends RuntimeException {
    public PhasedTestingEventException(String message) {
        super(message);
    }
}
