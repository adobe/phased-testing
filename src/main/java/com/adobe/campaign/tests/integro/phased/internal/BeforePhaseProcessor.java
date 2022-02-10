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
