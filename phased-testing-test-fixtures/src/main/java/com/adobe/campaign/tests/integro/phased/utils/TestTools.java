/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.ITestListener;
import org.testng.TestListenerAdapter;
import org.testng.TestNG;
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
public final class TestTools {

	private TestTools() {
		//Utility class. defeat instantiation
	}


	/**
	 * This method creates a testng test instance with a result listener
	 * 
	 * @return a TestNG instance
	 */
	public static TestNG createTestNG() {
		TestNG myTestNG = new TestNG();
		TestListenerAdapter tla = new TestListenerAdapter();
		myTestNG.addListener(tla);
		return myTestNG;
	}

	public static TestNG createTestNG(Class<?> ...testClasses) {
		TestNG testng = createTestNG();
		testng.setTestClasses(testClasses);
		return testng;
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
		List<XmlSuite> mySuites = new ArrayList<>();
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
		in_testngSuite.setMethodSelectors(Collections.singletonList(mySelector));
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
		List<ITestListener> testListenerAdapters = myTestNG.getTestListeners();
	
		if (testListenerAdapters.size() != 1)
			throw new IllegalStateException("We did not expect to have more than one adapter");
	
		return (TestListenerAdapter) testListenerAdapters.get(0);
	}

}
