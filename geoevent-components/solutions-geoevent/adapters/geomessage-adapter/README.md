# GeoMessage Adapter

The GeoMessage Adapter provides an example of an adapter that can receive and send XML messages in a simple GeoMessage format.  This GeoMessage format is an XML format for use with [Esri military features](http://resources.arcgis.com/en/help/main/10.2/index.html#//000n0000000p000000)
 and corresponds to the format used by the [ArcGIS Runtime Message Processor](https://developers.arcgis.com/en/java/api-reference/com/esri/core/symbol/advanced/MessageProcessor.html).

![Image of geomessage-adapter](ScreenShot.png)

## Features

* Sends and receives XML messages in a GeoMessage format.
* Converts message types received using available GeoEvent Definitions.
* The "_type" element of the message is used to look up an existing Geoevent Definition.
* Below are examples of the GeoEvent Definitions:

Definition | Purpose 
--- | ---
trackrep | Track movement reports
spotrep | To create SPOT reports
eod | To create and update EOD reports
medevac | To create Medevac reports
sitrep | To create SITREPS
slantrep | To create SLANTREPS
bedavail | To update the status of the Bedavail in an area updated based on reporting unit
chemlight | To create chemlights reports


## Sections

* [Requirements](#requirements)
* [Building](#building)
* [Installation](#installation)
* [Testing](#testing)
* [Licensing](#licensing)

## Requirements

* See common [solutions-geoevent-java requirements](../../../README.md#requirements)
* There are no additional requirements for this project

## Building 

* See the [solutions-geoevent-java instructions](../../../README.md#instructions) for general instructions on 
    * verifying your Maven installation
    * setting the location of the GeoEvent Processor and GeoEvent Processor SDK repositories
    * and any other common required steps
 * Open a command prompt and navigate to `solutions-geoevent-java/solutions-geoevent/adapters/geomessage-adapter`
* Enter `mvn install` at the prompt

## Installation

* Install the adapter
    * Browse to `solutions-geoevent-java/solutions-geoevent/adapters/geomessage-adapter/target` (this directory is created when you execute mvn install).
    * Copy the geomessage-adapter-{version}.jar file and paste it into the deploy folder in the GeoEvent Processor install directory ([GeoEvent Processor install location]\deploy\ -- default location is C:\Program Files\ArcGIS\Server\GeoEventProcessor\deploy).
* Check for existing GeoEvent Definitions.
    *  Open GeoEvent Processor Manager.
    *  Navigate to ‘Site’ > ‘GeoEvent Processor’ > ‘GeoEvent Definitions’.
    *  Confirm the following GeoEvent Definitions are available.

![Image of geoeventdefinition](doc/geoeventdefinitions.png)

* If these GeoEvent Definitions are not available, do the following to create these GeoEvent Definitions.
    *  Navigate to ‘Site’ > ‘GeoEvent Processor’ > ‘Configuration Store’ and click ‘Import Configuration’.
    *  Browse to `solutions-geoevent-java\data\configurations` and locate the `GeoEventDefinitions-GeoMessageAdapter.xml` configuration file. This file is located [here](../../../data/configurations/GeoEventDefinitions-GeoMessageAdapter.xml).
    *  On the Import Configuration dialog, click Import.

## Testing

### Validating the Installation
 
* See the [solutions-geoevent-java validation instructions](../../../README.md#validating-install).

### Testing with Live Data

* Download and build an application that can send simulated GeoMessages over UDP.
    * A sample UDP GeoMessage simulation application is available with the [GeoMessage Simulator (QT)](https://github.com/Esri/geomessage-simulator-qt).
    * A pre-built version of this project is included [here](../../../data/utilities/UDPGeoMessageSimulator).
    * A sample simulation file, SimpleGeoMessage.xml, is provided [here](../../../data/simulation_files).
* In the following steps you will configure GeoEvent Processor to receive and process simulated GeoMessage data.
* Open GeoEvent Processor Manager.
* Create a connector to receive UDP data.
    * Navigate to ‘Site’ > ‘GeoEvent Processor’ > 'Connectors'.
    * Select 'Create Connector' and configure as shown.

![Image of create connector](doc/create-connector.png)

* Next use GeoEvent Processor Manager to:
    * Create an Input Connector to receive GeoMessage data using the connector created above. 
    * Create an Output Connector to observe received data.
    * Create a simple GeoEvent Service to direct the input data to the output.
    * An example of a simple GeoEvent Service is illustrated below. 

![Image of service](doc/service.png)

* In GeoEvent Processor Manager, navigate to ‘Services’ > ‘Monitor’ and observe the GeoEvent Processor components, they should be similar to the illustration below (note: your names/outputs may differ).

![Image of monitor](doc/monitor.png)

* Start the UDP GeoMessage simulator and observe the values increase on the 'Monitor' page and the selected outputs are updated.

## Licensing

Copyright 2013 Esri

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


