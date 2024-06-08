/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
/**
 *
 */
package com.adobe.campaign.tests.integro.phased;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
/**
 * A PhasedTest Step in a class means that the class itself is a PhasedTest
 *
 * Author : gandomi
 *
 */
public @interface PhasedTest {

    boolean executeInactive() default true;

    /**
     * Lets us know if the phased test can shuffle
     *
     * @deprecated From now on by default a phased test will shuffle, unless you set a @PhaseEvent annotation on one of
     * the steps. In that case the Phased test will be considered as a Single Run Phased test.
     */
    @Deprecated
    boolean canShuffle() default true;

    String[] eventClasses() default {};
}
