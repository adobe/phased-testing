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

import com.adobe.campaign.tests.integro.phased.AfterPhase;
import com.adobe.campaign.tests.integro.phased.BeforePhase;
import java.lang.reflect.Method;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PhaseProcessorFactory {

  private PhaseProcessorFactory() {
    //Utility class with a factory method. defeat instantiation.
  }

  private static final Logger log = LogManager.getLogger();

  /**
   * A Simple factory method that is capable of producing {@link AbstractPhaseProcessor}
   * implementations.
   * @param method - The {@link Method} in question.
   * @return - A sub-class variant of {@link AbstractPhaseProcessor}
   */
  public static AbstractPhaseProcessor getProcessor(Method method) {
    if (method.isAnnotationPresent(BeforePhase.class)) {
      log.debug("Instantiating a Before Phase Processor");
      return new BeforePhaseProcessor(method);
    }
    if (method.isAnnotationPresent(AfterPhase.class)) {
      log.debug("Instantiating an After Phase Processor");
      return new AfterPhaseProcessor(method);
    }
    log.debug("Instantiating no operation dummy Phase Processor");
    return new NoOpPhaseProcessor(method);
  }
}
