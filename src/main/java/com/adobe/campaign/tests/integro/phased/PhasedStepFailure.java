/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased;

public class PhasedStepFailure extends Throwable {

    public PhasedStepFailure(String string, Throwable t) {
        super(string, t);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -2830092346021261368L;

    public PhasedStepFailure(String skipMessageSKIPFAILURE) {
        super(skipMessageSKIPFAILURE);
    }
}
