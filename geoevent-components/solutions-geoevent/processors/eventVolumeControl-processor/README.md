# Event Volume Control Processor

The Event Volume Control Processor filters events that exceed a given number of events received over a given interval of time. The user defines the number of events and the time interval thresholds.  Any events exceeding the thresholds are dropped. 

![Image of Event Volume Control Processor](evc_390X150.png)


## Sections

* [Requirements](#requirements)
* [Building](#building)
* [Installation](#installation)
* [Testing](#testing)
* [Licensing](#licensing)

## Requirements

* See common [solutions-geoevent-java requirements](../../../../README.md#requirements).
* The ArcGIS Runtime for Java SDK is required in order to run the standalone Maven Tests included with this project.

## Building 

* See the [solutions-geoevent-java instructions](../../../../README.md#instructions) for general instructions on 
    * verifying your Maven installation
    * setting the location of GeoEvent Processor and GeoEvent Processor SDK repositories
    * and any other common required steps
* Open a command prompt and navigate to `solutions-geoevent-java/solutions-geoevent/processors/<PROCESSOR_MAVEN_DIR>`
    * Enter `mvn install` at the prompt.

## Installation

* Install the Event Volume Control Processor.
    * Browse to `solutions-geoevent-java/solutions-geoevent/processors/eventVolumeControl=processor/target` (this directory is created when you execute mvn install).
    * Copy the .jar file and paste it into the deploy folder in the GeoEvent Processor install directory ([GeoEvent Processor install location]\deploy\ -- default location is C:\Program Files\ArcGIS\Server\GeoEventProcessor\deploy).


## Testing

### Validating the Installation
 
* See the [solutions-geoevent-java validation instructions](../../../README.md#validating-install).
    * Ensure the Event Volume Control Processor exists.

### Testing with Simulated Test Data

#### Deploying the Test Configuration

If you have already deployed the test configuration you may move on to Testing the Component

In GeoEvent Extension Manager 

* Go to the Site >> Configuration Store Tab. 
* Click the 'Import Configuration' button. 
* Select 'Choose File'
* Browse to the ./solutions-geoevent-java/data/configurations/ directory 
* Select the SolutionsComponentTestConfig.xml 
* Click 'Open' in the dialog. 
* Click 'Next'. 
* When prompted choose 'Import Configuration'. 

The test service configuration will be deployed to your instance of GeoEvent.



#### Testing the Component

The Introduction to GeoEvent tutorial has a simple TCP-Console application that will be used for most of the tests. It can be found [here](http://www.arcgis.com/home/item.html?id=b6a35042effd44ceab3976941d36efcf).

You will use the EventVolumeControl-Test service from the Solutions Test Configuration to test the functionality of the Event Volume Control processor.
* Open the EventVolumeControl-Test service in GEE Manager. 
* Click on the evc-tcp-txt-in Input and expand the 'Advanced' tab. Note that the input uses TCP port 5600.
* Open the ArcGIS GeoEvent Simulator (this is installed with GeoEvent Extension and can be found at the GeoEvent Extension install location).
* Make sure the Server points to the server on which GeoEvent Extension is deployed (default is local host).
* In the upper right, change the port to 5600 and  click the button with the red X to connect (Note: if you cannot connect the server is not listening on that port. This may be because the Input in GeoEvent extension has not been started).
* Make sure 'File' is selected in the combo box on the upper left.
* Click the 'Load File' button in the upper right.
* In the new dialog, click the file folder button in the upper right.
* Browse to the ./solutions-geoevent-java/data/simulation-files/ directory.
* Select eventvolumecontrol.csv and click 'Open'
* Click the 'Load' button. In the Preview Edits window you will see 1000 records. Note all events have identical trackids (ist attribute). The trackid is used by the Event Volume Control processor to filter events.
* Modify the number of messages received per interval to 1000.
* Modify the interval to 999 (milliseconds).
* Browse to the directory of the TCP-Console application (if you downloaded it from the tutorial it will be at ./IntroductionToGeoEvent/utilities/tcp-server-app).
* Double click TCPServerApp.bat (the application will not start if no services are listening on port 5570 - if you have deployed the test configuration an output service has been configured to listen on this port. Check that the Output tcp-out-5570 has been deployed and started).
* In the GeoEvent Simulator click the 'Step' button.
* The Event volume Control Processor has been configured to receive a maximum of 1 event with a given trackid every second. In GEE Manager you will see the rate of messages coming in is 1000 and the rate of messages going out is 1.
* In the TCP-console you will see that the Received Event (Event Definition name) is evc-in with a comma separated list of values.

## Licensing

Copyright 2014 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's
[license.txt](../../../license.txt) file.

