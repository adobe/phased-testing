# Changelog

## 9.0.0 - In-Progress
* **(breaking change)** [#224 Split the project into a multi-module project](https://github.com/adobe/phased-testing/issues/224). The project is now a multi-module Maven build: `phased-testing-core` (shared engine), `phased-testing-testng` (annotation-driven authoring, same artifact coordinates as before), and `phased-testing-mutational` (new artifact, inheritance/template-method authoring). If you only use the classic `@PhasedTest` annotation model, no changes are required beyond bumping the version. If you use `Mutational`/`MutationListener`, you now need to add a dependency on `phased-testing-mutational` explicitly — see [Installation](README.md#installation).
* **(breaking change)** The internal package `com.adobe.campaign.tests.integro.phased.permutational` has been renamed to `com.adobe.campaign.tests.integro.phased.stepdependencies`. This only affects code that directly imports classes from that package (`ScenarioStepDependencies`, `ScenarioStepDependencyFactory`, `StepDependencies`), not typical library usage.
* **(breaking change)** The exception `MutationRampUpException` has been renamed to `ExecutionModeConfigurationException`, since it is thrown for a generic invalid-execution-mode configuration error, unrelated to Mutational Testing specifically.
* **(new feature)** [#204 Introduction of the Execution Mode replacing Phases](https://github.com/adobe/phased-testing/issues/204). We have revised the way we execute scenarios, as we no longer only cater to Upgrade tests. The means you should revise the way you execute Phased Tests by using Execution Modes. For more information please refer to the chapter [Execution Modes](README.md#execution-modes).
* **(new feature)** [#35 Adding the Permutation Execution Mode](https://github.com/adobe/phased-testing/issues/35). We have introduced the Permutation Execution Mode. This mode executes a scenario with all possible permutations it can have. This is done by identifying the dependencies between each step, and creating the possible orders of that scenario. For more information please refer to the chapters [Permutation Execution Mode](README.md#permutation-execution-mode).
* **(new feature)** [#197 Adding the event wrappings to a step](https://github.com/adobe/phased-testing/issues/197). We have now introduced the different wrappings an event can have around a test step. Wrappings can be set in different ways: Execution Mode, Event annotation (in progress), The Phased Test Annotation.

* **New Environment Variables**
* MUTATIONAL.EXECUTION.MODE : This property is used to set the execution mode of the Mutational Tests. The value can be one of the following: STANDARD, INTERRUPTIVE(PRODUCER), INTERRUPTIVE(CONSUMER), NON-INTERRUPTIVE, PERMUATIONAL. This will replace the PHASED.TESTS.PHASE property which will be removed in 9.X.3.
* MUTATIONAL.EVENTS.TARGET : This property allows us to run a single event on a specific step of a scenario. The notation is either the standard method reference, or that of Surefire.

## 8.11.2
* **(new feature)** [#178 Allowing the injection in any step of a scenario](https://github.com/adobe/phased-testing/issues/178). We can now inject an event into a step in an arbitrary phased test. This is done by setting the syetm property PHASED.EVENTS.TARGET. This way you can inject the event into that step.
* **(new feature)** [#198 Adding Post Step Event actions](https://github.com/adobe/phased-testing/issues/198). We allow you to define a 'tearDownEvent' tool to allow you to put the system back to a normal state after the event has finished. Please refer to the chapter [Performing Event Cleanup Actions](README.md#performing-event-cleanup-actions).

## 8.11.1
* Renaming ConfigValueHandler to ConfigValueHandlerPhased
* Migrating to Java 11
* Upgrading the TestNG library to 7.8.0

## 8.0.0
* [Non-Interrupted Events](README.md#shuffled---non-interruptive)
* [#112](https://github.com/adobe/phased-testing/issues/112) Technical : Extracted the execution properties to the class ConfigValueHandler.
* [#115](https://github.com/adobe/phased-testing/issues/115) Fixed bug where the Non-Phased execution was stored as SINGLE-RUN. You can still keep the old behaviour by passing the property "PHASED.TESTS.NONPHASED.LEGACY" to "true". However by default they are stored as "phased-default".
* [#114](https://github.com/adobe/phased-testing/issues/114) Technical : The methods PhasedTestManager.isPhasedTestShuffledMode & PhasedTestManager.isPhasedTestSingleMode no longer take into account the current phase. Instead, they simply return the information about wether or not the given method/class is Shuffled or Single Mode.
* [Implementation of execution order detection base on the code.](README.md#execution-order) We now have two options:
  * We continue as before, where we select the order alphabetically.
  * Whenever the system property, PHASED.TESTS.DETECT.ORDER is set, the steps are executed in the order the way we declared in the code. By default, we expect the coe to be in maven where the tests are in the directory src/test/java. However, this can be overriden by setting the eecution property PHASED.TESTS.CODE.ROOT (#5)
* [#116](https://github.com/adobe/phased-testing/issues/116) We are now removing the explicite "SingleRun" mode.
  * All Phased Tests without a @PhaseEvent annotation next to a step are now considered as "Shuffled".
  * Any Phased Test with a @PhaseEvent annotation is a Singe Run test.
  * We have now deprecated the @PhasedTest "canShuffle" attribute.

## 7.0.10
- Reports are now merged by default (#56)
- Refactoring : cleaning up cyclomatic complexity in the code (#49).
- Fixed issue with the listener managing data providers for non-Phased tests (#75).

## 7.0.9
- Upgraded to TestNG 7.5
- Resolved case of Skip due to config issues, such as a failure in a BeforePhase method (#41)
- We no longer throw SkipExceptions in the OnStartTest. Instead, we set the status to Skipped (#42)
- Consumer results can now contain the results of the PRODUCER phase (#34)
- Storing duration and the phase in the scenario state (#36)
- Updated Log4J to 2.17.1 to resolve security issues (#38)
- Implemented the new select tests to run based on the producer phase (#9)
- Solved case when the users really wants to use retry. In this case we do not interrupt the retry mechanism.
- Moved back to java 8. We now compile the artefacts in java 8. This is because our main users are not yet in higher java versions.
- Removed the deprecated methods `PhasedTestManager.produceWithKey` and `PhasedTestManager.consumeWithKey`.

## 7.0.8
- Upgraded java version to Java 11
- Activated sonar scans
- Solved Sonar highlighted bugs #20, #21, #23, #24
- Fixed issue #28 where the skip message when no steps have been executed previously for the current scenario phase group happens

## 7.0.7
- Migrated to the public git repository.

## 7.0.5
- You can now define Phase setup methods. `@BeforePhase` & `@AfterPhase` can be set on a normal TestNG Before and After method. The method will then be executed in before or after a phase starts. (#40)
- We now allow for user defined data providers in a phased test. For the data provider to be considered, it needs to be declared at class level and not at method level. (#26)
- We can now configure the reports to include the data providers.
- We now throw an error if the arguments of the phased steps do not correspond to expected number of parameters (phased + user defined) (#28 & #27 & #38)
- Solved issue with tests continuing in consumer mode even if their steps had not been executed in the producer phase

## 7.0.4
- Introduced the Report by Phase Group Functionality (#5)
- Allowing users to configure the Merged Reports
- Other issues corrected are : #20, #22, #23, #24, #25

## 7.0.3
- Renamed old produce/consume to produceInStep/consumeFromStep. The old produceWithKey/consumeWith key are now deprecated. Instead you should use produceWithKey/consumeWith (#15)
- We can now export the phase cache at will. This is very useful for debugging or for Data Broker testing. Added a method PhasedTestManager.fetchExportFile which help return the selected export file (#8)

## 7.0.0
- Migrated to TestNG 7.4

## 1.0.0
- First Release
