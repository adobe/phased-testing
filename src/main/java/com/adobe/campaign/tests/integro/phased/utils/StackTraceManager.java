/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.utils;

/**
 * Manages access to the stack trace
 *
 *
 * Author : gandomi
 *
 */
public final class StackTraceManager {

    private StackTraceManager() {
        //Utility class. Defeat instantiation
    }

    /**
     * This method fetches the method that called the current method. Example :
     * if you have the following stack trace:
     * <p>
     * A to B to C
     * <p>
     * If you call fetchCalledBy() from C it will return B
     * 
     * @return A stack trace element corresponding to the method that called the
     *         caller
     */
    public static StackTraceElement fetchCalledBy() {
        /*
        3 - because The stack trace contains :
            0. getStackTrace()
            1. StackTraceElement.fetchCalledBy()
            2. The method from which this method "StackTraceElement fetchCalledBy()" is called
            3. ---  The method that did the call  ---
        */
        return Thread.currentThread().getStackTrace()[3];
    }

    /**
     * This method fetches the full name of the method that called the current
     * method. Example : if you have the following stack trace:
     * <p>
     * A to B to C
     * <p>
     * If you call fetchCalledBy() from C it will return B
     *
     * Author : gandomi
     *
     * @return Returns a String representation of the caller of the method that
     *         called this method
     *
     */
    public static String fetchCalledByFullName() {
        StackTraceElement l_calledElement = Thread.currentThread().getStackTrace()[3];
        return l_calledElement.getClassName() + '.'
            + l_calledElement.getMethodName();
    }

}
