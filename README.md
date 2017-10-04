# defense-solutions-proofs-of-concept

A repository to showcase demonstrations, prototypes and proofs of concept from the Defense Solutions Team.

## IMPORTANT NOTE

The samples provided in this repository are not officially released Esri solutions and have not gone through the standard software development lifecycle and/or testing used in officially released Esri solutions. They may not be fully tested or documented, and are not supported by Esri Technical Support or the Defense Solutions Team.  

![Screenshot][ss]

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

* Web Appbuilder (WAB) for ArcGIS Widgets
    * Prototypes used to address specialized workflows
    * See the individual widget READMEs for more information
    * Current widgets included and last/latest version tested with:

| Widget Name | Last WAB<br>Version Tested With | 
|---------------------------------|:-------------------------------:|
| [Bomb Threat Tool](./widgets/BombThreat/README.md) | 2.3 |
| [Critical Infrastructure <br>and Key Resources](./widgets/CI_KR_Chart/README.md) | 1.2 | 
| [Elevation Profile Widget](./widgets/ElevationProfileTable/README.md) | 1.2 | 
| [ERG](./widgets/ERG/README.md) | 2.3 |
| [Filter Editor](./widgets/FilterEditor/README.md) | 2.0 | 
| [Image Discovery](./widgets/ImageDiscovery/README.md) | 2.0 | 

* Reference Implementations
    * [Air Operations Command and Control Common Operating Picture (Air C2 COP)](.reference-implementations/air-c2-cop/README.md)

## Requirements

* Web Appbuilder for ArcGIS
    * See [ArcGIS Web Appbuilder for ArcGIS](http://developers.arcgis.com/web-appbuilder/)

## Instructions

* Deploying Widgets
    * To deploy a widget, copy the folder of the desired deployment widget to the stemapp/widgets directory. This is located in %webappbuilder_install%/client directory.
    * For more resources on developing, modifying, and deploying widgets please visit the
[Web AppBuilder for ArcGIS Documentation](https://developers.arcgis.com/web-appbuilder)

## Resources

* [ArcGIS for Defense Solutions Website](http://solutions.arcgis.com/defense)
* [ArcGIS for Defense Downloads](http://appsforms.esri.com/products/download/#ArcGIS_for_Defense)
* Learn more about Esri's Solutions [Focused Maps and Apps for Your Organization](http://solutions.arcgis.com/).
* [Web AppBuilder API](https://developers.arcgis.com/web-appbuilder/api-reference/css-framework.htm)
* [ArcGIS API for JavaScript](https://developers.arcgis.com/javascript/)
* [ArcGIS Blog](http://blogs.esri.com/esri/arcgis/)

## New to Github

* [New to Github? Get started here.](https://github.com/Esri/esri.github.com/blob/master/help/esri-getting-to-know-github.html)

## Issues

* Find a bug or want to request a new feature?  Please let us know by submitting an issue. Please see the [Important Note above](#important-note) concerning the level of support.

## New to Github

[New to Github? Get started here.](http://htmlpreview.github.com/?https://github.com/Esri/esri.github.com/blob/master/help/esri-getting-to-know-github.html)

## Contributing

Please see our [guidelines for contributing](./CONTRIBUTING.md).

## Licensing

Copyright 2017 Esri

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

[ss]: widgets/ERG/images/screenshot.jpg
