# CAP Adapter

The CAP Adapter provides an example of how to stream Common Alerting Protocol (CAP) XML messages as GeoEvents into ArcGIS GeoEvent Processor for Server.

![Image of geomessage-adapter](CAP_logo.JPG)

## Features

* Receives XML messages conforming to the CAP message format.
* Converts messages received using available GeoEvent Definitions.

## Sections

* [Requirements](#requirements)
* [Building](#building)
* [Installation](#installation)
* [Testing](#testing)
* [Licensing](#licensing)

## Requirements

* See common [solutions-geoevent-java requirements](../../../README.md#requirements).
* There are no additional requirements for this project.

## Building 

* See the [solutions-geoevent-java instructions](../../../README.md#instructions) for general instructions on 
    * verifying your Maven installation
    * setting the location of the GeoEvent Processor and GeoEvent Processor SDK repositories
    * and any other common required steps
* Open a command prompt and navigate to `solutions-geoevent-java/solutions-geoevent/adapters/CAP-adapter`
    * Enter `mvn install` at the prompt.

## Installation

* Install the CAP Adapter.
    * Browse to `solutions-geoevent-java/solutions-geoevent/adapters/CAP-adapter/target` (this directory is created when you execute mvn install).
    * Copy the .jar file and paste it into the deploy folder in the GeoEvent Processor install directory ([GeoEvent Processor install location]\deploy\ -- default location is C:\Program Files\ArcGIS\Server\GeoEventProcessor\deploy).

## Testing

### Validating the Installation
 
* See the [solutions-geoevent-java validation instructions](../../../README.md#validating-install).

### Testing with Simulated Test Data

* In the following steps you will configure GeoEvent Processor to receive and process simulated CAP messages.
* Open GeoEvent Processor Manager.
* Create a connector to receive file data updated to a folder.
    * Navigate to ‘Site’ > ‘GeoEvent Processor’ > 'Connectors'.
    * Select 'Create Connector' and configure as illustrated below.

![Image of create connector](doc/create-connector.png)

* Next use the GeoEvent Processor Manager to:
    * Create an Input Connector to receive file data using the connector created above. 
    * Create an Output Connector to observe received data.
    * Create a simple GeoEvent Service to direct the input data to the output.

* An example of a simple file/folder input is illustrated below: 
    * Choose the Input Connector 'Watch a folder for new .json files' (note: this connector will work for any file type, just select a wildcard or other compatible file filter).
    * An example Input Connector configuration is illustrated below.

![Image of input settings](doc/input-settings.png)

* An example of a simple GeoEvent Service is illustrated below. 

![Image of service](doc/service.png)

* In GeoEvent Processor Manager, navigate to ‘Services’ > ‘Monitor’ and observe the GeoEvent Processor components, they should be similar to the illustration below (note: your names/outputs may differ).

![Image of monitor](doc/monitor.png)

* One at a time, copy the "CAP-" XML files from solutions-geoevent-java\data\simulation_files into the input folder created during the input step and observe the values increase on the Monitor page and the selected outputs are updated. 
    * Note: each CAP message may generate multiple GeoEvents.

### Testing with Live Data Feeds

* Several government sites exist that publish CAP message data over HTTP and REST. See the resources sections for links to these sites and the GeoEvent Processor documentation and tutorials to learn how to configure appropriate connectors.

## Resources

* Learn more about the Common Alerting Protocol
    * [CAP on Wikipedia](http://en.wikipedia.org/wiki/Common_Alerting_Protocol)
* Sample CAP feeds available at the following 
    * [http://www.gdacs.org/](http://www.gdacs.org/)
    * [http://wcatwc.arh.noaa.gov/](http://wcatwc.arh.noaa.gov/)

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
