# PhasedTesting
[![unit-tests](https://github.com/adobe/phased-testing/actions/workflows/onPushSimpleTest.yml/badge.svg)](https://github.com/adobe/phased-testing/actions/workflows/onPushSimpleTest.yml) 
[![codecov](https://codecov.io/gh/adobe/phased-testing/branch/main/graph/badge.svg?token=GSi0gUlqq5)](https://codecov.io/gh/adobe/phased-testing)
[![javadoc](https://javadoc.io/badge2/com.adobe.campaign.tests.phased/phased-testing-testng/javadoc.svg)](https://javadoc.io/doc/com.adobe.campaign.tests.phased/phased-testing-testng)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=adobe_phased-testing&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=adobe_phased-testing)


Phased testing is a concept where tests can be written in a way so that they can validate major system changes.

This library allows you to define tests in such a way, so that they can be interrupted at any point awaiting an event, and to carry on where they left off. More specifically based on your design he Phased tests will ensure that a scenario will work on an upgraded system no matter where it is interrupted.

The most common usage is for validating :
* Upgrades
* Migrations
* Time-Consuming external Data process

## Table of Contents
- [Problem Statement](#problem-statement)
- [Installation](#installation)
  - [Maven](#maven)
- [Phases](#phases)
  - [Phase Modes](#phase-modes)
- [Writing a Phased Test](#writing-a-phased-test)
  - [Setting Execution Modes](#setting-execution-modes)
  - [Local Execution](#local-execution)
  - [Before- and After-Phase Actions](#before--and-after-phase-actions)
  - [Nested Design Pattern](#nested-design-pattern)
- [Running a Phased Test](#running-a-phased-test)
  - [Run Time Properties](#run-time-properties)
  - [Executing a CONSUMER phase based on the PRODUCED Data](#executing-a-consumer-phase-based-on-the-produced-data)
- [Integrity between Steps and Scenarios](#integrity-between-steps-and-scenarios)
  - [Phase Contexts - Managing the Scenario Step Executions](#phase-contexts---managing-the-scenario-step-executions)
  - [Managing Phased Data](#managing-phased-data)
- [Reporting](#reporting)
  - [Default Reports](#default-reports)
  - [Report By Phase Group and Scenario](#report-by-phase-group-and-scenario)
- [Misc](#misc)
  - [Data Providers](#data-providers)
- [Known Issues and Limitations](#known-issues-and-limitations)
  - [Parallel Testing](#parallel-testing)
  - [Retry Mechanisms](#retry-mechanisms)
- [Release Notes](#release-notes)
 
## Problem Statement
This library was originally created to help validate system changes such as upgrades and migrations. The general migration process is the following:

![The Standard Process](diagrams/PhasedDiagrams-Normal-Migration.png)

What we discovered was that a migration will affect users depending at what stage of a workflow process they are. 

![The Real Processes](diagrams/PhasedDiagrams-HL-Change-Scenarios.png)

If we want to simulate all the use cases for a workflow of a user we will end up with too many duplicate code. This is why we came p with Phased Testing, which allows a scenario to cover all the possible steps in which a workflow can be interrupted.  

## Installation
This version runs with the TestNG runner. You can use this library by including it in your project.

### Maven
The following dependency needs to be added to your pom file:

```
 <dependency>
    <groupId>com.adobe.campaign.tests.phased</groupId>
    <artifactId>phased-testing-testng</artifactId>
    <version>7.0.9</version>
</dependency>
```

## Phases
We have three test phases:
* **Producer** In this state the tests are preparing data to be used in the following test phase. 
* **Consumer** In this state the tests consume the data produced in the previous phase. 
* **Non-Phased** In this state, we have not designated a state, as such, if not unwanted, we execute all tests.

### Phase Modes
We have two modes of execution Phased Testing:
* Default Mode
* Single Mode
* Shuffled Mode

#### Default Mode
The steps of each scenario are executed one by one without interruption.

#### Single Execution Mode
Single Execution Mode is used only when a workflow will always be interrupted at a given stage. This is particularly relevant when your scenario will expect a time concuming external process to finish. In this case we execute all steps till the Phase End marker. When in Consumer mode, we execute the rest of the steps.

![The Single Execution Mode](diagrams/PhasedDiagrams-SingleRun-H.png)

The diagram above represents what will be executed by the following code:

```java
@Test
@PhasedTest(canShuffle = true)
public class ShuffledTest {

    public void step1(String val) {
        PhasedTestManager.produce("step1Val","A");
    }

    public void step2(String val) {
        String l_fetchedValue = PhasedTestManager.consume("step1Val");
        PhasedTestManager.produce("step2Val",l_fetchedValue + "B");
        
    }
    
    @PhaseEvent

    public void step3(String val) {
        String l_fetchedValue = PhasedTestManager.consume("step2Val");
        assertEquals(l_fetchedValue, "AB");
    }
}
```

#### Shuffled Execution Mode
When in Shuffled mode, we execute all the possible ordered combinations of the steps. Example Given a test with three steps, in Producer State, we :
1. Execute all the three steps
2. Execute the first two steps
3. Execute the first step only

When in Consumer state we :
1. Execute the two last steps
2. Execute the last step
3. Execute all the steps

![The Shuffled Execution Mode](diagrams/PhasedDiagrams-Shuffle-H.png)

The diagram above represents what will be executed by the following code:

```java
@Test
@PhasedTest(canShuffle = true)
public class ShuffledTest {

    public void step1(String val) {
        PhasedTestManager.produce("A1");
    }

    public void step2(String val) {
        String l_fetchedValue = PhasedTestManager.consume("A1");
        PhasedTestManager.produce("B1",l_fetchedValue + "B");
        
    }

    public void step3(String val) {
        String l_fetchedValue = PhasedTestManager.consume("B1");
        assertEquals(l_fetchedValue, "AB");
    }
}
```

## Writing a Phased Test
The Phased Testing is activated using two annotations:
* **@PhasedTest** : Class level annotation. Allows you to control how the test should be executed
* **@PhaseEvent** : Method level annotation. By setting it you tell the system at which step does the phase event happen. The tests will stop at that point.

Moreover, you need to :
* Make your methods accept at least one argument
* The methods will be executed in alphabetical order. So prefixing the methods with their step number is a good practice. 

### Setting Execution Modes

#### Shuffled Mode
In order for a test scenario to be executed in shuffle mode you need to add the following annotation at the class level `@PhasedTest(canShuffle = true)`

#### Single Run Mode
In order for a test scenario to be executed in shuffle mode you need to add the following annotation at the class level `@PhasedTest(canShuffle = false)`. However because the interruption will happen at the same location all the time, you have to add the annotation `@PhaseEven@PhaseEvent` where you expect the interruption to occur.

Optionally if you consider that the scenario can never be run as non-phased, you need also include:  `@PhasedTest(canShuffle = false, executeInactive = false)`. When executeInactive is false, the Single Run scenario will only run when in Phases.

### Local Execution
Ideally you should set the default data provider on your tests. This allows you to execute the test locally without needing to force the Phased Test listener.

```
@Test( dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
@PhasedTest(canShuffle = true)
public class MyPhasedTest {
}
```

However, whenever the Phased Test Listener discovers a Phased Test, it will add the necessary data providers needed for running the test. But, ideally it is best to set the default providers in orrder to not lose the possibility of local execution.

### Before- and After-Phase Actions
We have introduced the possibility of defining Before and After Phases. This means that you can state if a method can be invoked before or after the phased tests are executed. These methods are only activated when we are in a Phase, and will not run when executed when we execute the scenarios in Non-Phased mode. 

However, Before/After Phase methods are like any other Before/After method as, when invoked, they will affect all underlying tests, even if they are not Phased Tests.

To activate this functionality you add the annotations `@BeforePhase` & `@AfterPhase` to a TestNG configuration method such as: **@BeforeSuite, @AfterSuite, @BeforeGroups, @AfterGroups, @BeforeTest and @AfterTest**.

To your configuration method. Example:

```java
@BeforePhase
@BeforeSuite
public void myBeforePhaseSuite() {
    //Perform actions
}
```

In the example above the method `myBeforePhaseSuite` will be invoked in the beginning of the suite. By default, the BeforePhase method is invoked when we are in a Phase I.e. Producer or Consumer.

You can configure this with the attribute `appliesToPhases`, which accepts an array of `Phases`. In the example below we are activating AfterPhase for the Consumer phase only.


 ```java
@AfterPhase(appliesToPhases = {Phases.CONSUMER})
@AfterSuite
public void myAfterPhasedSuite() {
    //Perform actions
}
```

### Nested Design Pattern
As of version 7.0.9 of Phased Testing which is based on the 7.5 of TestNG, we can now define nested Phased tests. This allows you to regroup the phased tests under the same class. Thus, you will have Phased Tests that resemble method based tests.

Example:
```java
public class PhasedTestSeries_NestedContainer {
  @Test( dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
  @PhasedTest(canShuffle = true)
  public class PhasedScenario1 {

    public void step1(String val) {
      PhasedTestManager.produce("myValX","A");
    }

    public void step2(String val) {
      String l_fetchedValue = PhasedTestManager.consume("myValX");

      assertEquals(l_fetchedValue, "A");
    }
  }

  @Test( dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
  @PhasedTest(canShuffle = true)
  public class PhasedScenario2 {

    public void step1(String val) {
      PhasedTestManager.produce("MyVal1","AB");
    }
    
    public void step2(String val) {
      String l_fetchedValue = PhasedTestManager.consume("MyVal1");

      assertEquals(l_fetchedValue, "AB");
    }

  }

}
```

## Running a Phased Test
We are able to run tests in phases since each step stores the information needed for the following steps. For now this is done at the discretion of the developer. This storage is important as it helps us keep track of the tests:

![The storage of test cache](diagrams/PhasedDiagrams-General-Process.png)

Managing this data is obviously essential to the Phased Tests. We will discuss this in more detail in the chapter on "Managing Phased Data".


### Run Time Properties
We have the following system properties:
* PHASED.TESTS.PHASE
* PHASED.TESTS.DATABROKER
* PHASED.TESTS.STORAGE.PATH
* PHASED.TESTS.OUTPUT.DIR
* PHASED.TESTS.RETRY.DISABLED
* PHASED.TESTS.REPORT.BY.PHASE_GROUP

#### PHASED.TESTS.PHASE
We have three phased states:
1. **PRODUCER** : We produce information
2. **CONSUMER** : We consume information
3. **NON_PHASED** : By default we execute all the steps in a phased test, unless the @PhasedTest has set the attribute **executeInactive** to "false"

#### PHASED.TESTS.DATABROKER
This parameter allows you to tell the PhaseTestManager which DataBroker implementation you want to use. The is usually a full class path (package name + class name). More on this will be dealt with in the chapter on Phased Data Broker.

#### PHASED.TESTS.STORAGE.PATH
This is the path in which the Phased Data is stored, and fetched. If not set, the path /phased_output/phased_tests/phaseData.properties will be used.

#### PHASED.TESTS.OUTPUT.DIR
By default, Phased Test data is stored under the directory phased_output. You can override this by setting this system property. If not set, the default directory phased_output will be used.

#### PHASED.TESTS.RETRY.DISABLED
By default, we deactivate retry analyzer for the phased tests. However if you really want to use your retry listener, we can stop the phase test listener from deactivating it.

#### PHASED.TESTS.REPORT.BY.PHASE_GROUP
By default, we do not modify reports. Each step in a scenario is reported as is. We have introduced a "Report By Phase Group" functionality, which is activated with this property.

### Executing a CONSUMER phase based on the PRODUCED Data
Usually when your test code is in the repository of the product being tested, you will be having a delta in tests between two versions **N** & **N+1**. In such cases you will want to only execute the tests that exist in both versions. 

For this, as of version 7.0.9, we have introduced the functionality that allows you to automatically select the phased tests that were executed in a previous phase. This means that when activated in a CONSUMER Phase, the selection is made based on the tests that were executed in the PRODUCER Phase. This functionality is activated when you pass or include the test group `PHASED_PRODUCED_TESTS`.

## Integrity between Steps and Scenarios 
### Phase Contexts - Managing the Scenario Step Executions
Although we try to keep the execution of a scenario like any other test scenario, we feel that it is useful to document how the state of a scenario works.

#### On Failure
Whenever a scenario step fails the following steps are marked as SKIPPED.

#### On Non-Execution of a Phase
If a phase is not executed, the steps in the next phase are also SKIPPED.

### Managing Phased Data
The way data is stored between two phases is in two ways:
* Simple properties file (Default)
* Phased Data Broker

#### Simple Properties file - Default
At the end of the producer phase we store all the phase data in a properties file. By default it is stored under:
<STD Output directory>/phased_tests/phaseData.properties

When going to the consumer state all you need to do is to make sure that the file is available.

You can override the directory by setting the system property *PHASED.TESTS.STORAGE.PATH*.

#### Phased Data Broker
In this edition we have introduced the concept of a Phased Data Broker. This allows you to define how you want your phase data to be stored. The PhasedData listener still stores a local copy, but it will in fact use a broker that you have defined. 

For this you need to define a Class that implements the interface com.adobe.campaign.tests.integro.phasedPhasedDataBroker. 

The Phased Data Broker can then be attached to the test in three ways (in descending order):
1. Setting a system property PHASED.TESTS.DATABROKER with the class full name.
2. Configuring the property PHASED.TESTS.DATABROKER as a Test Suite parameter 
3. Programmatically by calling PhasedTestManager.setDataBroker()

## Reporting
In this chapter we discuss the test reports. We currently have two types of reports:
- Default Reports
- Report By Phase Group

### Default Reports
By default we only slightly modify how TestNG generates reports. As each step is a method, you will get one result per step. This will lead to a lot of results, but you will have the fill overview of the evolution of the tests.

### Report By Phase Group and Scenario
To make the reports a bit less messy, we introduced a report where, we only keep one result per Phase Group and Scenario. Technically, we keep the most pertinent result. The following use cases exist for a phase group.
- If all steps succeed, we keep the first step as the end result.
- If in the current phase we have a failure at step X, we only keep that step result. All following steps are discarded from the result. 
- If the phase group had failed in the previous phase, we keep the first step result which is "skipped". When failing due to a failure in the PRODUCER Phase, the skip message will contain the step and the phase in which the failure occurred. 
- Whenever an exception is encountered in a step, it is enriched with the step name and the phase in which it happened.
- The duration we report will be the full duration of the scenario which includes the steps on both phases.

Whenever activated, the default behavior is we just show the phase group name. This can, however be configured. We will describe this process in more detail in the chapter [on how we can configure the Merged Reports](#configuring-merged-reports). 

To activate this report, you need to set the system property PHASED.TESTS.REPORT.BY.PHASE_GROUP to "true".

#### Configuring Merged Reports
By default, we store the Phase Groups whenever a Phased Test is run. However, we now have the possibility to override this. This is done by using the class `PhasedTestManager.MergedReportData.configureMergedReportName(Prefix Elements, Prefix Elements)`. This allows users to specify the Phased Test output.

The following configuration items can be added to the constructed name:
- **Phase** adds the phase name to the constructed method name
- **Phase Group** adds the phase group to the constructed method name
- **Scenario Name** adds the scenario name (the class) to the constructed method name
- **Data Provider** add the data providers, separated by "_" to the name 

## Misc
In this chapter we will deal with miscellaneous issues related to Phased Tests

### Data Providers
We now allow for a user to also include data providers in connection to Phased Tests. The Data Provider parameters will, when executed in a Phased Test, be added to the test result.

A configuration check is done in the beginning. The phased test steps are checked and their arguments are compared to the number of data providers + the injected data provider for phased tests. If the number of arguments does not correspond to the total number of data providers, a `PhasedTestConfigurationException` is thrown right at the beginning.

## Known Issues and Limitations
In this chapter we will share the functionalities that yet need to be implemented or fixed in the Phased Testing system. In most cases these issues are items which have not yet been tested, and we yet do not know or have not specified how they should work when we are in a phased execution. 

### Parallel Testing
For now, we do not know how parallel execution will work with phased tests. So ideally it is best to be avoided in this context. 

### Retry Mechanisms
For now, we have not come around to deciding how retry should work in the case of phased tests. By default, we deactivate them on the phased tests unless the user specifically chooses to activate them by setting the system property `PHASED.TESTS.RETRY.DISABLED` to false. 

## Release Notes
### 7.0.9
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

### 7.0.8
- Upgraded java version to Java 11
- Activated sonar scans
- Solved Sonar highlighted bugs #20, #21, #23, #24
- Fixed issue #28 where the skip message when no steps have been executed previously for the current scenario phase group happens

### 7.0.7
- Migrated to the public git repository.

### 7.0.5
- You can now define Phase setup methods. `@BeforePhase` & `@AfterPhase` can be set on a normal TestNG Before and After method. The method will then be executed in before or after a phase starts. (#40) 
- We now allow for user defined data providers in a phased test. For the data provider to be considered, it needs to be declared at class level and not at method level. (#26)
- We can now configure the reports to include the data providers.
- We now throw an error if the arguments of the phased steps do not correspond to expected number of parameters (phased + user defined) (#28 & #27 & #38)
- Solved issue with tests continuing in consumer mode even if their steps had not been executed in the producer phase

### 7.0.4
- Introduced the Report by Phase Group Functionality (#5)
- Allowing users to configure the Merged Reports
- Other issues corrected are : #20, #22, #23, #24, #25

### 7.0.3
- Renamed old produce/consume to produceInStep/consumeFromStep. The old produceWithKey/consumeWith key are now deprecated. Instead you should use produceWithKey/consumeWith (#15) 
- We can now export the phase cache at will. This is very useful for debugging or for Data Broker testing. Added a method PhasedTestManager.fetchExportFile which help return the selected export file (#8)

### 7.0.0
- Migrated to TestNG 7.4

### 1.0.0
- First Release

