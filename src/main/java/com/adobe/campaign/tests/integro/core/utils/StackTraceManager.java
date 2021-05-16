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
package com.adobe.campaign.tests.integro.core.utils;

/**
 * Manages accesss to the stack trace
 *
 *
 * Author : gandomi
 *
 */
public class StackTraceManager {

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
        StringBuilder sb = new StringBuilder(l_calledElement.getClassName());

        sb.append('.');
        sb.append(l_calledElement.getMethodName());

        final String l_methodFullName = sb.toString();
        return l_methodFullName;
    }

}
