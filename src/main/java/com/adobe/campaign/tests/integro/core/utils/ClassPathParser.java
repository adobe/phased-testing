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

public class ClassPathParser {

    /**
     * This method extracts the full class path from a given full path of a
     * method.Example: a.b.c.D.e will return 'a.b.c.D'
     * 
     * Author : gandomi
     *
     * @param in_fullMethodPath
     *            A full path of a method. This consists of class path + method
     *            name
     * @return null if the value cannot be extracted
     */
    public static String extractClass(String in_fullMethodPath) {

        int l_lastDotLocation = in_fullMethodPath.lastIndexOf(".");

        if (l_lastDotLocation == -1)
            return null;

        return in_fullMethodPath.substring(0, l_lastDotLocation);
    }

    /**
     * This method extracts the method name from a given full path of a method
     * 
     * Author : gandomi
     *
     * @param in_fullMethodPath
     *            A full path of a method. This consists of class path + method
     *            name
     * @return null if the value cannot be extracted
     */
    public static String extractMethod(String in_fullMethodPath) {
        int l_lastDotLocation = in_fullMethodPath.lastIndexOf(".");

        if (l_lastDotLocation == -1)
            return null;

        return in_fullMethodPath.substring(l_lastDotLocation + 1);
    }

    /**
     * Extracts the package from the method full path Author : gandomi
     *
     * @param in_method
     * @return NULL if there is no package
     */
    public static String extractPackage(String in_method) {

        return extractClass(extractClass(in_method));
    }

    /**
     * This method checks if a class exists
     * 
     * Author : gandomi
     *
     * @param in_className
     * @return
     */
    public static boolean isClassInProject(String in_className) {
        try {
            Class.forName(in_className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * This method extracts the Class name from a given full method name.
     * Example: a.b.c.D.e will return 'D'
     * 
     * Author : gandomi
     *
     * @param in_fullMethodPath
     * @return
     */
    public static String extractClassName(String in_fullMethodPath) {
        String l_fullClassPath = extractClass(in_fullMethodPath);
        if (l_fullClassPath == null) {
            return l_fullClassPath;
        }
        String[] l_pathItems = l_fullClassPath.split("[.]");

        if (l_pathItems.length == 0)
            return in_fullMethodPath;
        else
            return l_pathItems[(l_pathItems.length - 1)];
    }

    /*
     * 
     * This method fetches the method that called the current method. Example : if you have the following stack trace:
     * A -> B -> C
     * If you call fetchCalledBy() from C it will return B
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
     *
     * Author : gandomi
     *
     * @param l_calledElement
     * @return
     *
     */
    public static String fetchCalledByFullName() {
        StackTraceElement l_calledElement =  Thread.currentThread().getStackTrace()[3];
        StringBuilder sb = new StringBuilder(l_calledElement.getClassName());
    
        sb.append('.');
        sb.append(l_calledElement.getMethodName());
        
        final String l_methodFullName = sb.toString();
        return l_methodFullName;
    }
}
