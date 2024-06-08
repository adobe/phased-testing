/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
 */
package com.adobe.campaign.tests.integro.phased.internal;

import com.adobe.campaign.tests.integro.phased.exceptions.PhasedTestConfigurationException;
import com.adobe.campaign.tests.integro.phased.Phases;
import com.adobe.campaign.tests.integro.phased.utils.ClassPathParser;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An generic processor that defines the base capabilities of any implementation that is capable of
 * processing phases.
 */
public abstract class AbstractPhaseProcessor {

  private static final Logger log = LogManager.getLogger();
  private final Method method;

  public AbstractPhaseProcessor(Method method) {
    this.method = method;
  }

  /**
   *
   * @return - <code>true</code> if for a given Method it's phase is recognised and can be processed.
   */
  public final boolean canProcessPhase() {
    log.debug("{} : in process() as part of annotation transformation",
        getClass().getSimpleName());
    if (annotationsToConsider().isEmpty()) {
      // We have nothing to consider. So perhaps we stumbled into a dummy phase processor.
      // Bail out.
      return true;
    }

    checkAnnotationCompatibility(method);
    boolean matchFound = Arrays.stream(filterBy().apply(method))
        .noneMatch(t -> t.equals(Phases.getCurrentPhase()));

    if (matchFound) {
      log.info("Omitting {}} method {}", getPhaseName(),
          ClassPathParser.fetchFullName(method));
      return false;
    }
    return true;
  }

  /**
   * @return - The list of annotations that should be considered
   */
  abstract List<Class<? extends Annotation>> annotationsToConsider();

  /**
   * @return - A {@link Function} that can be used to filter out annotations in a given method.
   */
  abstract Function<Method, Phases[]> filterBy();

  /**
   * @return - A string representation of the phase mainly for logging purposes.
   */
  abstract String getPhaseName();

  /**
   * Given a list of expected annotations, this method sees if the given method contains any of
   * these them
   * <p>
   * Author : gandomi
   *
   * @param in_testMethod          a Test Method
   */
  private void checkAnnotationCompatibility(Method in_testMethod) {
    List<Class<? extends Annotation>> in_expectedAnnotations = annotationsToConsider();
    boolean noMatchesFound = Arrays.stream(in_testMethod.getDeclaredAnnotations())
        .noneMatch(t -> in_expectedAnnotations.contains(t.annotationType()));
    if (noMatchesFound) {
      String errorMsg = Arrays.stream(in_testMethod.getDeclaredAnnotations())
          .map(each -> each.annotationType().getName()).collect(Collectors.joining(","));

      throw new PhasedTestConfigurationException(
          "You have declared a " + getPhaseName() + " annotation with an incompatible TestNG Configuration Annotation. The method "
              + ClassPathParser.fetchFullName(in_testMethod)
              + " has the following annotations: " + errorMsg);
    }
  }

}
