/*
 * Copyright 2022 Adobe
 * All Rights Reserved.
 *
 * NOTICE: Adobe permits you to use, modify, and distribute this file in
 * accordance with the terms of the Adobe license agreement accompanying
 * it.
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
