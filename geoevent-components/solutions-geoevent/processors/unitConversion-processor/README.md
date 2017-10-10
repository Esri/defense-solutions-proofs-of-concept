# Unit Conversion Processor

<DESCRIPTION>  

![Image of Unit Conversion Processor]<PROCESSOR_PICTURE_NAME>.PNG

## Features 


* Unit Conversion Processor - <DESCRIPTION>


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
* Open a command prompt and navigate to `solutions-geoevent-java/solutions-geoevent/10.3.0/processors/unitConversion-processor`
    * Enter `mvn install` at the prompt.

## Installation

* Install the Unit Conversion Processor.
    * Browse to `solutions-geoevent-java/solutions-processors/unitConversion-processor/target` (this directory is created when you execute mvn install).
    * Copy the .jar file and paste it into the deploy folder in the GeoEvent Processor install directory ([GeoEvent Processor install location]\deploy\ -- default location is C:\Program Files\ArcGIS\Server\GeoEventProcessor\deploy).


## Testing

### Validating the Installation
 
* See the [solutions-geoevent-java validation instructions](../../../../README.md#validating-install).
    * Ensure the Unit Conversion Processor exists.

### Testing with Simulated Test Data

* In the following steps you will configure GeoEvent Processor to receive and process simulated data.
* The following example configures the Unit Conversion Processor, the other processors can be configured in a similar manner.

* Open GeoEvent Processor Manager.
* Create a new GeoEvent definition 
   * Go to Site > GeoEvent > GeoEvent Definitions
   * Click 'New GeoEvent Definition'
   * In the the New Geoevent Definition dialog configure similar to the follwing illustration

![Image of Add New Input](doc/add-new-def.png)

in the GeoEvent Definition Name textbox and click 'Create'
   * Configure the fields as in the image below (this will ensure that the sample simulation data can be consumed in the test service).
   
![Image of geoevent definition](doc/geoeventdefinition.png)

* Create an Input Service to accept text over tcp messages
   * In GeoEvent Manager go to Services->Input and click Add Input
   * In the Input Connectors Window choose 'Receive text from a TCP socket'
   * Configure the service similar to the picture below.

![Image of Input Service](doc/input-service.png)

* Next, create an Output Connector to observe the received data.
    * Navigate ‘Services’ > 'Outputs'.
    * Select Add Output and select 'Write to a json file' and configure the properties using the image below as a guide.
    
![Image of Output Service](doc/tests-json-output.png)

   * If you do not have a registered folder already to write json files to, click the 'Register Folder' button and give the folder a name and point it to an existing folder on your file system. In the example the name is test_components_out and the path is C:\gep\registered\tests.  
   * Click 'Save' when you are finished.
* Create a simple GeoEvent Service to direct the input data to the output using the selected processor.
   * Go to Services > GeoEvent Services and click 'Add Service'
   * In the Add New Service dialog enter a name and description similar to the following

![Image Add New Service](doc/add-geoevent-service.png)

   * On the left panel click and drag the input service you created previously and tests-json-output services into the service constructor window.
   * Next click and drag a Processor into the service constructor window.
   * Configure the processor similar to the illustration below.
   
![Image of processor](doc/configure.png)

   * Connect the components of the service as illustrated below.

![Image of service](doc/test-service.png)

* When finished click the 'Publish' button to save the service.



* In GeoEvent Processor Manager, navigate to ‘Services’ > ‘Monitor’ and observe the GeoEvent Processor components. You should see the newly created service and it should have a status of 'Started'.

* Using the GeoEvent Simulator, load the simulation file located at  solutions-geoevent-java\data\simulation_files\<SIMULATION>.csv
* Set the listening server to your geoevent server instance (local host if the simulator is on the same machine as geoevent server)
* Set the port to the same value as you set the input services port and click the connect button
* Click the 'Play' button to run the simulation
* In GeoEvent Processor Manager, navigate to 'Services' > 'Monitor' to observe that the values are increasing and the selected outputs are updated. 
* Next go to Services > Output and find the tests-json-out service you created previosly
* Click the Stop button to stop the service
* In a file browser go to the folder where your tests-json-output is located.
* Open the tests_<timestamp>.json file with the most recent timestamp.
* You can now test the processors with additional outputs such as published feature services.

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

