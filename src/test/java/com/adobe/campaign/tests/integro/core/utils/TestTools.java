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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.testng.ITestListener;
import org.testng.ITestNGListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
import org.testng.collections.Lists;
import org.testng.xml.XmlMethodSelector;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

/**
 * This class helps create and run testNG files from the command line.
 * 
 * You can configure and run tests in two ways:
 * <ol>
 * <li>Load a testng file in memory, and run it</li>
 * <li>Load and configure a test in your test itself</li>
 * </ol>
 * 
 * @author gandomi
 *
 */
public class TestTools {


	/**
	 * This method creates a testng test instance with a result listener
	 * 
	 * @return a TestNG instance
	 */
	public static TestNG createTestNG() {
		TestNG myTestNG = new TestNG();
		TestListenerAdapter tla = new TestListenerAdapter();
		myTestNG.addListener((ITestNGListener) tla);
		return myTestNG;
	}

	/**
	 * Attached a newly created empty suite attached to the given testng instance
	 * 
	 * @param in_testNGTestInstance
	 * @param in_suiteName
	 * @return
	 */
	public static XmlSuite addSuitToTestNGTest(TestNG in_testNGTestInstance, String in_suiteName) {
		XmlSuite mySuite = new XmlSuite();
		mySuite.setName(in_suiteName);
		List<XmlSuite> mySuites = new ArrayList<XmlSuite>();
		mySuites.add(mySuite);
		// Set the list of Suites to the testNG object you created earlier.
		in_testNGTestInstance.setXmlSuites(mySuites);
		return mySuite;
	}

	/**
	 * Attaches a the filter our plugin to the given testng suite
	 * 
	 * @param in_testngSuite
	 */
	public static void attachFilterOutPluginToSuite(XmlSuite in_testngSuite) {
		XmlMethodSelector mySelector = new XmlMethodSelector();
		mySelector.setClassName("com.adobe.campaign.tests.integro.core.FilterOut");
		in_testngSuite.setMethodSelectors(Arrays.asList(mySelector));
	}

	/**
	 * Attaches a test to the TestNG suite
	 * 
	 * @param in_testNGSuite
	 * @param in_testName
	 * @return
	 */
	public static XmlTest attachTestToSuite(XmlSuite in_testNGSuite, String in_testName) {
		XmlTest lr_Test = new XmlTest(in_testNGSuite);
	
		lr_Test.setName(in_testName);
		return lr_Test;
	}

	/**
	 * This method returns the test result listener attached to the testng instance
	 * 
	 * @param myTestNG
	 * 
	 * @return a TestListenerAdapter that listens on the test results
	 */
	public static TestListenerAdapter fetchTestResultsHandler(TestNG myTestNG) {
		List<ITestListener> testLisenerAdapaters = myTestNG.getTestListeners();
	
		if (testLisenerAdapaters.size() != 1)
			throw new IllegalStateException("We did not expect to have more than one adapter");
	
		return (TestListenerAdapter) testLisenerAdapaters.get(0);
	}

}
