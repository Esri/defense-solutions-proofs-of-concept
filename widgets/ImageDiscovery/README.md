# image-discovery-webappbuilder-widget

The Image Discovery widget allows users to search and discover imagery contained across multiple image services.

![Image of Image Discovery Widget][ss]

## Sections

* [Features](#features)
* [Requirements](#requirements)
* [Instructions](#instructions)
* [Resources](#resources)
* [New to Github?](#new-to-github)
* [Issues](#issues)
* [Contributing](#contributing)
* [Licensing](#licensing)

## Features

* Search for imagery contained across multiple image services
* Preview imagery
* Filter search results
* Perform analysis on discovered imagery
* Download imagery (if supported by image service)
* Export imagery to web map

## Requirements

* Web Appbuilder for ArcGIS Version 2.0+
    * See [ArcGIS Web Appbuilder for ArcGIS](http://developers.arcgis.com/web-appbuilder/)

## Instructions

* Deploying Widgets
    * To deploy a widget, copy the folder of the desired deployment widget to the stemapp/widgets directory. This is located in %webappbuilder_install%/client directory.
    * For more resources on developing, modifying, and deploying widgets please visit the
[Web AppBuilder for ArcGIS Documentation](https://developers.arcgis.com/web-appbuilder)

* Configuring the Widget to Use Image Services
    * In edit mode of WebApp Builder click "configure this widget" near the bottom of the Image Discovery widget icon.

Use the list below to load the image services you want to add to the Image Discovery Widget. The steps are repeated for each image service you would like to add.

* Inside of the configuration window paste a URL to an image service and click "Load"
* The image service will now be loaded into the configuration window with fields and labelling options displayed
 * *Display Name* - Label used to identify which service the search results come from
 * *Sort Configuration* - allow the user to sort the service's search results by these fields. If the service does not contain these fields please leave them unchecked.
* Display fields are all of the fields associated with each row in the image service
 * Click the checkbox next to the field name to enable the field in the Image Discovery Widget
 * *Display In* - Radio buttons either display the field value in the result itself ("Entry") or in a popup ("Info Popup") for the result
 * *Label* - The label to display to the user for the field in each result. The default is the field name in the image service.
 * *Value Append* - allows you to add to the field after the value. For instance, you can add a % or &deg; symbol to the end of a field value.
 * *Float Precision* - sets the precision of the decimal value (for decimal field types only)
 * *Bold* - Display the field as bold in the search results


## Resources

* Learn more about Esri's Solutions [Focused Maps and Apps for Your Organization](http://solutions.arcgis.com/).
* [Web AppBuilder API](https://developers.arcgis.com/web-appbuilder/api-reference/css-framework.htm)
* [ArcGIS API for JavaScript](https://developers.arcgis.com/javascript/)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)

## New to Github

* [New to Github? Get started here.](https://github.com/Esri/esri.github.com/blob/master/help/esri-getting-to-know-github.html)

## Issues

* Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## New to Github

[New to Github? Get started here.](http://htmlpreview.github.com/?https://github.com/Esri/esri.github.com/blob/master/help/esri-getting-to-know-github.html)

## Contributing

Please see our [guidelines for contributing](../../CONTRIBUTING.md).

## Licensing

Copyright 2016-2017 Esri

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

A copy of the license is available in the repository's
[license.txt](LICENSE.txt) file.

[ss]: images/screenshot.jpg
