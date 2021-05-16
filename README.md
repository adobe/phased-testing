# PhasedTesting


This is the rewriting of an old implementation. It allows you to define Phases in tests using annotations. There are the following requirements:
* Each @Test method in the class will be a test phase
* Each method should be called "step_1","step_2", etc...

## Phases
We have three test phases:
* **Producer** In this state the tests are preparing data to be used in the following test phase. 
* **Consumer** In this state the tests consume the data produced in the previous phase. 
* **Non-Phased** In this state, we have not designated a state, as such, if not unwanted, we execute all tests.


### Phase Modes
We have two modes of execution Phased Testing:
* Single Mode
* Shuffled Mode

#### Single Execution Mode
In this case we execute all steps till the Phase End marker. When in Consumer mode, we execute the rest of the steps.

#### Shuffled Execution Mode
When in Shuffled mode, we execute all the possible ordered combinations of the steps. Example Given a test with three steps, in Producer State, we :
1. Execute all of the three steps
2. Execute the first two steps
3. Execute the first step only

When in Consumer state we :
1. Execute the two last steps
2. Execute the last step
3. Execute all of the steps


### Writing a Phased Test
The Phased Testing is activated using two annotations:
* **@PhasedTest** : Class level annotation. Allows for controlling of how the test should be executed
* **@PhaseEvent** : Method level annotation. By setting it you tell the system at which step does the phase event happen. The tests will stop at that point.

### Running a Phased Test

We have the following system properties:
* PHASED.TESTS.STORAGE.PATH
* PHASED.TESTS.PHASE
* PHASED.TESTS.DATABROKER
* PHASED.TESTS.RETRY.DISABLED


#### PHASED.TESTS.STORAGE.PATH
This is the path in which the Phased Data is stored, and fetched. If not set, the path /ac_test_output/phased_tests/phaseData.properties will be used.

#### PHASED.TESTS.PHASE
We have three phased states:
1. **PRODUCER** : We produce information
2. **CONSUMER** : We consume information
3. **INACTIVE** : By default we execute all the steps in a phased test, unless the @PhasedTest has set the attribute **executeInactive** to "false"

#### PHASED.TESTS.DATABROKER
This parameter allows you to tell the PhaseTestManager which DataBroker implementation you want to use. The is usually a full class path (package name + class name). More on this will be dealt with in the chapter on Phased Data Broker.

#### PHASED.TESTS.RETRY.DISABLED
By default we deactivate retry analyzer for the phased tests. However if you really want to use your retry listener, we can stop the phase test listener from deactivating it.

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


## Known Issues
Below is a list of known issues:

### Nested Test Class Definitions
There seems to be an incompatibility between External Group Manager and Nested Class Level Tests. This problem manifests itself in two ways:
* In versions 6.X the nested class may be completely ignored
* In 7.X the nested classes are mixed with normal Class level tests. We our selection will be faulty.

For now we forbid the usage of nested class level tests when you activate the external group manager. At execution time if you are using the External Group Manager, wee will generate Exception of the type NestedTestClassForbiddenException. This is for now a filed bug at TestNG : https://github.com/cbeust/testng/issues/2536


