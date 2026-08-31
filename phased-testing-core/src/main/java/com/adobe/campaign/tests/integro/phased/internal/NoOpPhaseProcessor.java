/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.Phases;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * An implementation of {@link AbstractPhaseProcessor} that is basically a no operation processor.
 */
class NoOpPhaseProcessor extends AbstractPhaseProcessor {

  public NoOpPhaseProcessor(Method method) {
    super(method);
  }

  @Override
  List<Class<? extends Annotation>> annotationsToConsider() {
    return Collections.emptyList();
  }

  @Override
  Function<Method, Phases[]> filterBy() {
    return method -> new Phases[]{};
  }

  @Override
  String getPhaseName() {
    return "Dummy Phase";
  }
}
