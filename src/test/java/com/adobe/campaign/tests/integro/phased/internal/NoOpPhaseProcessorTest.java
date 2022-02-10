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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.adobe.campaign.tests.integro.phased.internal.samples.PhasesSample;
import java.lang.reflect.Method;
import org.testng.annotations.Test;

public class NoOpPhaseProcessorTest extends BaseProcessorTest {

  @Test
  public void testNoPhaseHappyFlow() throws NoSuchMethodException {
    Method method = PhasesSample.class.getMethod("regularMethod");
    AbstractPhaseProcessor processor = PhaseProcessorFactory.getProcessor(method);
    assertTrue(processor instanceof NoOpPhaseProcessor);
    assertTrue(processor.canProcessPhase());
    assertEquals(processor.filterBy().apply(method).length, 0);
    assertEquals(processor.getPhaseName(), "Dummy Phase");
  }
}
