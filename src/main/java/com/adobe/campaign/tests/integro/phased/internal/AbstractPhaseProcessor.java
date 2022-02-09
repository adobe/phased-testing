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

import com.adobe.campaign.tests.integro.phased.PhasedTestConfigurationException;
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
