/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.spi;

/**
 * Marker interface implemented by a test-authoring module's own built-in TestNG {@code @DataProvider}
 * classes (e.g. the annotation-driven module's provider, or the inheritance/template-method module's
 * provider), as opposed to a data provider class declared by a user of the framework. Built-in provider
 * methods are invoked directly by the framework rather than reflectively by
 * {@code PhasedTestManager.fetchDataProviderValues}, since their signatures are not a plain zero-arg user
 * data provider. Implementing this marker lets each authoring module identify its own provider class to
 * core without core needing a compile-time reference to it.
 */
public interface FrameworkDataProvider {
}
