# PhasedTesting
Phased testing is a concept where tests can be written in a way so that they can validated major system changes.

This library allows you to define tests in such a way, so that they can be interrupted at any point awaiting an event, and to carry on where they left off. More specifically based on your design he Phased tests will ensure that a scenario will work on an upgraded system no matter where it is interrupted.

The most common usage is for validating :
* Upgrades
* Migrations
* Time-Consuming external Data process

## Problem Statement
This library was originally created to help validate system changes such as upgrades and migrations. The general migration process is the following:

![The Standard Process](diagrams/PhasedDiagrams-Normal-Migration.png)

What we discovered was that a migration will affect users depending at what stage of a workflow process they are. 

![The Real Processes](diagrams/PhasedDiagrams-HL-Change-Scenarios.png)

If we want to simulate all the use cases for a workflow of a user we will end up with too many duplicate code. This is why we came p with Phased Testing, which allows a scenario to cover all the possible steps in which a workflow can be interrupted.  
  

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
@Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
@PhasedTest(canShuffle = true)
public class ShuffledTest {

    public void step1(String val) {
        PhasedTestManager.produce("A");
    }

    public void step2(String val) {
        String l_fetchedValue = PhasedTestManager.consume("step1");
        PhasedTestManager.produce(l_fetchedValue + "B");
        
    }
    
    @PhaseEvent

    public void step3(String val) {
        String l_fetchedValue = PhasedTestManager.consume("step2");
        assertEquals(l_fetchedValue, "AB");
    }
}
```

#### Shuffled Execution Mode
When in Shuffled mode, we execute all the possible ordered combinations of the steps. Example Given a test with three steps, in Producer State, we :
1. Execute all of the three steps
2. Execute the first two steps
3. Execute the first step only

When in Consumer state we :
1. Execute the two last steps
2. Execute the last step
3. Execute all of the steps

![The Shuffled Execution Mode](diagrams/PhasedDiagrams-Shuffle-H.png)

The diagram above represents what will be executed by the following code:

```java
@Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)
@PhasedTest(canShuffle = true)
public class ShuffledTest {

    public void step1(String val) {
        PhasedTestManager.produce("A");
    }

    public void step2(String val) {
        String l_fetchedValue = PhasedTestManager.consume("step1");
        PhasedTestManager.produce(l_fetchedValue + "B");
        
    }

    public void step3(String val) {
        String l_fetchedValue = PhasedTestManager.consume("step2");
        assertEquals(l_fetchedValue, "AB");
    }
}
```

## Writing a Phased Test
The Phased Testing is activated using two annotations:
* **@PhasedTest** : Class level annotation. Allows you to controlling of how the test should be executed
* **@PhaseEvent** : Method level annotation. By setting it you tell the system at which step does the phase event happen. The tests will stop at that point.

Moreover you need to :
* Add a class level Test annotation with the following characteristics : `@Test(dataProvider = PhasedDataProvider.DEFAULT, dataProviderClass = PhasedDataProvider.class)`
* Make your methods accept one argument
* The methods will be executed in alphabetical order. So prefixing the methods with their step number is a good practice. 

### Shuffled Mode
In order for a test scenario to be executed in shuffle mode you need to add the following annotation at the class level `@PhasedTest(canShuffle = true)`

### Single Run Mode
In order for a test scenario to be executed in shuffle mode you need to add the following annotation at the class level `@PhasedTest(canShuffle = false)`. However because the interruption will happen at the same location all the time, you have to add the annotation `@PhaseEven@PhaseEvent` where you expect the interruption to occur.

Optionally if you consider that the scenario can never be run as non-phased, you need also include:  `@PhasedTest(canShuffle = false, executeInactive = false)`. When executeInactive is false, the Single Run scenario will only run when in Phases.
 

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
By default Phased Test data is stored under the directory phased_output. You can override this by setting this system property. If not set, the default directory phased_output will be used.

#### PHASED.TESTS.RETRY.DISABLED
By default we deactivate retry analyzer for the phased tests. However if you really want to use your retry listener, we can stop the phase test listener from deactivating it.

#### PHASED.TESTS.REPORT.BY.PHASE_GROUP
By default we do not modify reports. Each step in a scenario is reported as is. We have introduced a "Report By Phase Group" functionality, which is activated with this property.

## Managing Phased Data
The way data is stored between two phases is in two ways:
* Simple properties file (Default)
* Phased Data Broker

### Simple Properties file - Default
At the end of the producer phase we store all the phase data in a properties file. By default it is stored under:
<STD Output directory>/phased_tests/phaseData.properties

When going to the consumer state all you need to do is to make sure that the file is available.

You can override the directory by setting the system property *PHASED.TESTS.STORAGE.PATH*.

### Phased Data Broker
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

### Report By Phase Group
To make the reports a bit less messy, we introduced a report where, we only keep one result per Phase Group. Technically, we keep the most pertinent result. The following use cases exist for a phase group.
- If all steps succeed, we keep the first step as the end result.
- If in the current phase we have a failure at step X, we only keep that step result. All following steps are discarded from the result. 
- If the phase group had failed in the previous phase, we keep the first step result which is "skipped".
- Whenever an exception is encountered in a step, it is enriched with the step name and the phase in which it happened.

Whenever activated, the default behavior is we just show the phase group name. This can, however be configured. We will describe this process in more detail in the chapter [on how we can configure the Merged Reports](#configuring-merged-reports). 

To activate this report, you need to set the system property PHASED.TESTS.REPORT.BY.PHASE_GROUP to "true".

#### Configuring Merged Reports
By default we store the Phase Groups whenever a Phased Test is run. However we now have the possibility to override this. This is done by using the class `PhasedTeestManager.configureMergedReportName(Prefix Elements, Prefix Elements)`. This allows users to specify the Phased Test output.

The following configuration items can be added to the constructed name:
- **Phase** adds the phase name to the constructed method name
- **Phase Group** adds the phase group to the constructed method name
- **Scenario Name** adds the scenario name (the class) to the constructed method name   



## Release Notes

### 7.0.4
- Introduced the Report by Phase Group Functionality
- Allowing users to configure the Merged Reports

### 7.0.3
- #15 Renamed old produce/consume to produceInStep/consumeFromStep. The old produceWithKey/consumeWith key are now deprecated. Instead you should use produceWithKey/consumeWith 
- #8  We an now export the phase cache at will. This is very useful for debugging or for Data Broker testing. Added a method PhasedTestManager.fetchExportFile which help return the selected export file 

### 7.0.0
- Migrated to TestNG 7.4

### 1.0.0
- First Release

