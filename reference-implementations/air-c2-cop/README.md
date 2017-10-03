# air-c2-cop-python

### This repository is no longer maintained.

### Additional information is available in the repository's [Wiki](https://github.com/Esri/air-c2-cop-python/wiki).

A collection of scripts and files necessary for the creation of the Defense Solutions Air C2 COP project.

The Air Operations Command and Control Common Operating Picture is a gallery of maps and apps for an Air Operations Officer to maintain a common operating picture for command and control. These include a 3D web application built in Web AppBuilder for situational awareness and analysis, an operations dashboard for real-time awareness and decision making, and story map journals to brief pre- and post-action. These applications all reference a central web map and web scene integrated with other real-time data inputs analyzed using GeoEvent Extension.

### Repository Owner: [Joe](https://github.com/joebayles)
#### Secondary: [Gigzy](https://github.com/adgiles)
* Merge Pull Requests
* Creates Releases and Tags
* Manages Milestones
* Manages and Assigns Issues

## Sections

* [Features](#features)
* [Requirements](#requirements)
* [Instructions To Get Started](#instructions-to-get-started)
* [Resources](#resources)
* [Issues](#issues)
* [Contributing](#contributing)
* [Credits](#credits)
* [Licensing](#licensing)

## Features

## Requirements

* ArcGIS 10.4.1 Enterprise

## Instructions To Get Started

### Fork and Clone the Repo
Start contributing to the solutions-geoprocessing-toolbox repo by making a fork and cloning it to your local machine.

* Fork the **dev** branch from the repo on github.com.
* Clone your remote onto your local system.

### Set Your Upstream
Setting the parent repo to get changes from.

* `> git remote -v`

if no *upstream* is listed continue with:

* `> git remote add upstream https://github.com/Esri/air-c2-cop-python`
* `> git remote set-url upstream --push no_push`

check that an *upstream* is registered:

* `> git remote -v`

### Getting Changes from Upstream
The solutions-geoprocessing-toolbox repo changes often, so make sure you are getting the latest updates often.

* `> git fetch upstream`
* `> git merge upstream/dev`

## Resources

* Learn more about [ArcGIS Solutions](http://solutions.arcgis.com/).
* Learn more about [ArcGIS for Defense](http://solutions.arcgis.com/defense/).
* Learn more about [ArcGIS Pro](http://pro.arcgis.com/en/pro-app/).

## Issues

* Find a bug or want to request a new feature?  Please let us know by submitting an issue.

## Contributing

Esri welcomes contributions from anyone and everyone through GitHub. Please see Esri's [guidelines for contributing](https://github.com/esri/contributing).

When you contribute to this repository we ask that you follow the guidelines below. If you've got questions, or you get stuck, please ask the [Repository Owner](#repository-owner). We are here to help! Thanks.

### Share Your Mods
If you've made changes to the repo that you want to share with the community.

* Commit your changes to your local
* Sync local with your remote fork
* Make a **Pull Request** from your remote fork on github.com.


### Notes On Contributing
* Always work in the **dev** branch, never in *master*. This helps us keep our releases clean.
* Never merge Pull Requests. The [Repository Owner](#repository-owner) needs to test any updates to make sure the repo is stable.
* Always log an [Issue](https://github.com/Esri/air-c2-cop-python/issues) for problems you find, though you should check through the existing issues to make sure it wasn't already logged. 

## Credits

## Licensing

Copyright 2016 Esri

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
[license.txt](license.txt) file.

[](Esri Tags: ArcGIS Defense and Intelligence Military Emergency Management National Security)
[](Esri Language: Python)
