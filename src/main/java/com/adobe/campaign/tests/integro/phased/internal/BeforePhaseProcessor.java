/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.BeforePhase;
import com.adobe.campaign.tests.integro.phased.Phases;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeTest;

/**
 * An implementation of {@link AbstractPhaseProcessor} that is capable of processing {@link Method}'s
 * that are annotated with {@link BeforePhase}
 */
class BeforePhaseProcessor extends AbstractPhaseProcessor {

  public BeforePhaseProcessor(Method method) {
    super(method);
  }

  @Override
  List<Class<? extends Annotation>> annotationsToConsider() {
    return Arrays.asList(BeforeSuite.class, BeforeTest.class, BeforeGroups.class,
        BeforeClass.class);
  }

  @Override
  Function<Method, Phases[]> filterBy() {
    return method -> method.getAnnotation(BeforePhase.class).appliesToPhases();
  }

  @Override
  String getPhaseName() {
    return BeforePhase.class.getSimpleName();
  }
}
