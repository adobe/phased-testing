package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.AfterPhase;
import com.adobe.campaign.tests.integro.phased.Phases;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;

/**
 * An implementation of {@link AbstractPhaseProcessor} that is capable of processing {@link Method}'s
 * that are annotated with {@link AfterPhase}
 */
class AfterPhaseProcessor extends AbstractPhaseProcessor {

  public AfterPhaseProcessor(Method method) {
    super(method);
  }

  @Override
  List<Class<? extends Annotation>> annotationsToConsider() {
    return Arrays.asList(AfterSuite.class, AfterTest.class,
        AfterGroups.class, AfterClass.class);
  }

  @Override
  Function<Method, Phases[]> filterBy() {
    return method -> method.getAnnotation(AfterPhase.class).appliesToPhases();
  }

  @Override
  String getPhaseName() {
    return AfterPhase.class.getSimpleName();
  }
}
