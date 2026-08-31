# Mutational Testing — Inheritance-Based Authoring (Recommended)

This module provides the modern, inheritance/template-method way to author a mutational test scenario.
Unlike a [Phased Test](../phased-testing-testng/README.md), your test class isn't a plain TestNG class run
directly by TestNG — it extends the abstract class `Mutational`, and a single template method on that base
class (`scenario`) resolves the right step order and invokes your step methods one by one, reflectively.

For the shared concepts (scenarios as classes, events, permutations), the execution modes, the run-time
configuration, reporting and data management — all of which apply equally to this module — see the
[root README](../README.md).

## Table of Contents
<!-- TOC -->
  * [Writing a Mutational Test](#writing-a-mutational-test)
    * [Defining a scenario](#defining-a-scenario)
    * [Permutations](#permutations)
    * [Interruptive Events](#interruptive-events)
    * [Non-Interruptive Events](#non-interruptive-events)
<!-- TOC -->

## Writing a Mutational Test
You need to register `MutationListener` for Mutational tests to be recognized, either on your suite in
`testng.xml`:

```xml
<suite name="My Suite">
    <listeners>
        <listener class-name="com.adobe.campaign.tests.integro.phased.MutationListener"/>
    </listeners>
    ...
</suite>
```

or with the `@Listeners` annotation on your test class.

### Defining a scenario
Extend `Mutational`, and write your steps as plain (non-`@Test`) public methods, each accepting a single
`String` argument — the current phase/shuffle group. You still need a class-level `@Test` annotation (for
grouping, same as any TestNG class), but you do **not** need `@PhasedTest` — `Mutational`
itself already carries the annotation wiring your steps need.

```java
@Test(groups = "checkout")
public class ShoppingCartScenario extends Mutational {

    public void loginToSite(String phaseGroup) {
        PhasedTestManager.produce("authToken", "123456");
    }

    public void searchProduct(String phaseGroup) {
        PhasedTestManager.produce("product", "product1");
    }

    public void addProductToCart(String phaseGroup) {
        String product = PhasedTestManager.consume("product");
        PhasedTestManager.produce("cart", "cart1");
    }

    public void checkout(String phaseGroup) {
        PhasedTestManager.consume("authToken");
        PhasedTestManager.consume("cart");
    }
}
```

Key differences from [Writing a Phased Test](../phased-testing-testng/README.md#writing-a-phased-test):
* Step methods are **not** annotated `@Test` individually — they're plain methods discovered via reflection
  and invoked one by one by `Mutational.scenario(String)`.
* Each step method must accept exactly one `String` argument — the current phase/shuffle group.
* The `produce`/`consume` context API (`PhasedTestManager.produce`/`consume`) is exactly the same as in
  Phased Testing, since both authoring styles share the same core engine.
* Ordering between steps is always determined from code-detected dependencies between steps (via
  `produce`/`consume`) — the same underlying mechanism Phased Tests can opt into via
  [`PHASED.TESTS.DETECT.ORDER`](../README.md#phasedtestsdetectorder), but for Mutational tests it's not
  optional, since there's no TestNG-native `@Test` method order to fall back on.

### Permutations
The [PERMUTATIONAL execution mode](../README.md#permuational-execution-mode) is the feature most specific to
Mutational Testing: instead of picking a single valid step order, it identifies every dependency between
steps (via `produce`/`consume`) and re-executes the scenario once per valid permutation of that order. See
[Permutations](../README.md#permutations) for the underlying concept.

### Interruptive Events
Interruptive events (single-run scenarios) are the one place a Mutational scenario still uses an annotation:
`@PhaseEvent`, placed on the step method where you expect the interruption to occur. Everything up to and
including that step runs in the PRODUCER phase; the remaining steps run in the CONSUMER phase after the
event. This is the same `@PhaseEvent` marker used by
[Phased Tests](../phased-testing-testng/README.md#single-run-mode) — the difference is only in how the class
is authored (`extends Mutational`, plain step methods).

```java
@Test
public class MutationalTestSingleRun extends Mutational {

    public void step1(String phaseGroup) {
    }

    public void step2(String phaseGroup) {
    }

    @PhaseEvent
    public void step3(String phaseGroup) {
    }
}
```

See [INTERRUPTIVE Execution Mode](../README.md#interruptive-execution-mode) for how the PRODUCER/CONSUMER
phases are driven at execution time.

### Non-Interruptive Events
See [Event Management and Execution](../README.md#event-management-and-execution) for how an event is
[wrapped around a step](../README.md#wrapping-an-event-around-a-step) — the same shared-core `startEvent`/
`finishEvent` calls apply here as in Phased Testing, driven from `Mutational.execute()`'s step loop rather
than a TestNG listener — and the [`23`/`33` timing behaviors](../README.md#non-interruptive-execution-mode)
that control it.

Unlike interruptive events, non-interruptive events carry **no annotation** on a Mutational scenario. They
are bound purely through execution properties, resolved by the core `ConfigValueHandler`. Where a
[Phased Test](../phased-testing-testng/README.md#binding-an-event-to-a-scenario) can attach an event through
the `@PhaseEvent`/`@PhasedTest` `eventClasses` attribute, a Mutational scenario relies exclusively on the
shared run-time properties described in the root reference:

* [`MUTATIONAL.EVENTS.NONINTERRUPTIVE`](../README.md#mutationaleventsnoninterruptive) — the fully qualified
  class name of the non-interruptive event to wrap the scenario around.
* [`MUTATIONAL.EVENTS.TARGET`](../README.md#mutationaleventstarget) — the specific step the event should be
  injected on.

```
mvn clean test \
  -DMUTATIONAL.EVENTS.NONINTERRUPTIVE=com.adobe.campaign.tests.integro.phased.data.events.MyNonInterruptiveEvent \
  -DMUTATIONAL.EVENTS.TARGET=ShoppingCartScenario#checkout
```

The event class itself is written the same way for both authoring styles — it extends
`NonInterruptiveEvent`. See
[Writing a Non-Interruptive Event](../phased-testing-testng/README.md#writing-a-non-interruptive-event) for
how to implement one, and [Event Management and Execution](../README.md#event-management-and-execution) for
the underlying concept.