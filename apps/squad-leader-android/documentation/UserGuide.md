User Guide - squad-leader-android 
====================

[Setup](#setup)  
[Usage](#usage)  

## Setup

### System requirements

Squad Leader runs on Android devices version 4.0.3 and higher. Viewshed analysis works on Android 4.1 and higher.

#### Storage

These setup directions reference the SD card, as in `/mnt/sdcard`. Some Android devices have no slot for an SD card. Others have a slot for an SD card, but sometimes there may be no SD card in the slot. But Android always has a primary external storage directory, even if the "external" storage is actually internal. An SD card is not required to run Squad Leader.

#### Uncheck "Don't Keep Activities"

On the Android device, go to Settings > Developer Options or Settings > General > Developer Options. (Some devices don't have Developer Options available by default. If your device does not have Developer Options, you can skip this step.) If your device has Developer Options available, ensure that **"Don't keep activites"** or **"Do not keep activities"** is **unchecked**. If that option is checked, the map will reset whenever the user leaves it, such as when the spot report form or the settings dialog appears.

#### Running on an Android emulator

Like any app using ArcGIS Runtime for Android, Squad Leader can run on an Android emulator. But you must follow the instructions in [this blog post](http://blogs.esri.com/esri/arcgis/2012/05/02/arcgis-runtime-sdk-for-android-v1-1-supports-android-emulator/) to create an Android virtual device (AVD) that will work with ArcGIS Runtime. Please note that the Android emulator runs in a firewall-restricted sandbox that cannot communicate over UDP with outside processes, meaning you cannot send or receive Geomessages (spot reports, etc.) from or to Squad Leader running on an emulator.

### Installation and configuration

In order to install the app, your device must allow the installation of apps from unknown sources. On some devices, this setting is under **Settings > Security**. On other devices, this setting is under **Settings > Manage Applications**. Still other devices might have this setting elsewhere.

Install the app from the APK file you can download from [ArcGIS for Defense and Intelligence](http://www.arcgis.com/home/group.html?owner=Arcgisonline_defense&title=ArcGIS%20for%20Defense%20and%20Intelligence).

Optional: before running the app for the first time, if you wish to specify which layers the app initially uses, you can create a file called mapconfig.xml and put it in /mnt/sdcard/SquadLeader on the target device. Here is a simple mapconfig.xml file:

    <?xml version="1.0" encoding="UTF-8"?>
    <mapconfig name="test map">
        <layers>
            <layer name="Imagery" visible="true" type="TiledMapServiceLayer" basemap="true">
                <url>http://services.arcgisonline.com/ArcGIS/rest/services/World_Imagery/MapServer</url>
            </layer>
        </layers>
        <initialextent>
            <anchor>
                <x>7842690</x>
                <y>4086500</y>
            </anchor>
            <scale>250000</scale>
        </initialextent>
        <viewshed>
            <elevation>/mnt/sdcard/data/monterey_srtm_1arc_v3_webmercator.tif</elevation>
            <observerheight>2.0</observerheight>
        </viewshed>
    </mapconfig>

Layer type should be one of the following:

- DynamicMapServiceLayer (dynamic map service)
- FeatureServiceLayer (feature service; either an entire feature service like ".../FeatureServer" or a single layer like ".../FeatureServer/42")
- GeoPackage (OGC GeoPackage)
- ImageServiceLayer (image service)
- Mil2525CMessageLayer (GeoMessages file; see below for details on [adding a layer](#add-a-layer))
- TiledCacheLayer (file:/// URL to a local TPK or compact cache)
- TiledMapServiceLayer (cached map service)

For best results, be sure that one and only one layer with basemap="true" also has visible="true".

If you do not provide a mapconfig.xml file, a default list of ArcGIS Online basemap layers will be used when the app launches for the first time.

After the first launch, the app uses the bsaemap layers that it loaded previously. If you want to reset and re-read mapconfig.xml, you can [reset the map](#reset-the-map). Alternatively, you can manually go to the Android application settings, choose Squad Leader, and choose Clear Data. Then run the app and it will read mapconfig.xml again.

#### GPS simulation

By default, Squad Leader uses the device's location capabilities to display the user's current location on the map. This is the appropriate setting for real-world deployments, so most likely you don't need to do anything to change location settings. Read on if you are interesting in using simulated GPS.

Squad Leader can use a user-specified GPX file or a built-in GPX file of track points in Monterey, California. An IT professional who wants to deploy Squad Leader with a simulation based on a GPX file can create a file named simulation.gpx, create a directory called SquadLeader on the device's main storage (not an SD card), and put simulation.gpx in the SquadLeader directory. Then Squad Leader will use simulation.gpx to simulate the user's location until the user chooses a location setting in the Squad Leader UI. After a user has chosen a location setting, simulation.gpx will not be used again unless the Squad Leader app's data is cleared in Android settings.

#### GeoMessage files and layers

GeoMessage files contain GeoMessages that can be displayed on a map. GeoMessages are expressed in XML according to the [GeoMessage specification](https://github.com/Esri/geomessage-simulator-qt/blob/master/GeoMessageSpecification/GeoMessageSpecification.md). Squad Leader's built-in map configuration attempts to load the GeoMessage file /mnt/sdcard/data/coa.xml as a layer of type Mil2525CMessageLayer. If you would like that layer to display, do the following:

1. Make a directory called `data` in your device's `/mnt/sdcard` directory (probably your top-level directory when using a file explorer app or USB connection from Windows).
1. Place a copy of [coa.xml](../data/coa.xml) in the `data` directory.
1. Run Squad Leader. You may need to go to Squad Leader's Settings menu and choose **Reset map** to make the message layer appear.

If you don't need the coa.xml layer, do nothing. The app will load the other layers and run properly.

You can also [add a layer](#add-a-layer) by navigating to a GeoMessage file on your device.

#### Configuring viewshed analysis

Viewshed analysis is available on Android 4.1 and higher but must be configured. To configure viewshed analysis, you must put an elevation raster file on the device in a location where the app can read it, and you must edit mapconfig.xml to reference the elevation raster file. See https://developers.arcgis.com/android/guide/add-raster-data.htm for a list of supported raster file formats. See the example mapconfig.xml above for an example of configuring viewshed analysis. If no mapconfig.xml is present in /mnt/sdcard/SquadLeader, the app will use /mnt/sdcard/data/monterey_srtm_1arc_v3_webmercator.tif if it exists (you can download that raster from this repository's [data directory](../data) if desired). Otherwise, viewshed analysis will not be available.

##### Limitations of viewshed analysis:

- The elevation raster must be in the same spatial reference as the first layer added to the map or the analysis will not work.
- If you can choose which spatial reference your map uses, to maximize viewshed accuracy, choose a projection appropriate for the area of interest. The best kind of projection for viewshed analysis is a local projection that is specific to your geographic area of interest. A UTM projection is a good choice. The [sample data](../data) is in the Web Mercator projection, which is excellent for worldwide visualization but not as good for analysis as a local projection is. Unprojected data--longitude/latitude, such as WGS 1984--generally produces the least accurate results.
- ArcGIS Runtime's viewshed analysis is a type of "visual analysis:"
  - It uses the GPU, is massively multithreaded, and performs very quickly.
  - It uses an elevation raster stored on the device and thus works offline.
  - The analysis is limited to the current map extent.
  - It provides a good approximation of the viewshed that is not as rigorous as [the ArcGIS Viewshed geoprocessing tool](https://desktop.arcgis.com/en/desktop/latest/tools/spatial-analyst-toolbox/viewshed.htm), which is not available offline on Android.

### Simulating messages

You can run the GeoMessage Simulator application ([binary](http://www.esri.com/apps/products/download/index.cfm#ArcGIS_for_the_Military), [source](https://github.com/Esri/geomessage-simulator-qt)) to send messages to Squad Leader. GeoMessage Simulator is especially useful for testing and demonstration purposes. Note that these simulated messages will not make it to Squad Leader running on an emulator ([more info](#running-on-an-android-emulator)).

Squad Leader supports the "removeall" Geomessage action to remove all messages of a certain type (e.g. position_report, chemlight, spot_report). Here's an example that removes all position reports:

    <geomessages>
        <geomessage v="1.0">
            <_type>position_report</_type>
            <_action>removeall</_action>
            <_id>{b4b3eeaa-c769-11e4-8731-1681e6b88ec1}</_id>
        </geomessage>
    </geomessages>

## Usage

Launch the app on an Android device. An interactive map appears with several buttons and a data display that shows the current location in MGRS, time, speed, and heading.

### Change the basemap

To change the basemap currently displayed, tap the basemap selector button in the upper left corner. A dialog appears with a list of basemaps that have been added to the app. Choose a basemap to display it. Only one basemap is visible at a time.

### Display MGRS grid

To display or hide a military grid reference system (MGRS) grid on the map, toggle the grid button:

![Grid button](../source/SquadLeader/app/src/main/res/drawable/ic_grid_normal.png)

### Navigate the map

Drag a finger on the map to pan. To zoom in and out, either pinch open and close or use the buttons in the lower right corner.

The app displays a **Follow Me** button:

![Menu button](../source/SquadLeader/app/src/main/res/drawable/ic_follow_me_normal.png)

When Follow Me is selected, the map follows the user's current location. To exit Follow Me mode, unselect the Follow Me button or simply pan the map. You can [change the location mode](#change-settings) if desired.

To rotate the map, touch the map with two fingers and rotate. To reset the rotation so that north is up again, tap the north arrow:

![North arrow](../source/SquadLeader/app/src/main/res/drawable/ic_north_arrow.png)

To navigate to an MGRS location, go to **Menu** > **Go to MGRS Location**. Type or paste a valid MGRS string and tap **Go to MGRS Location**. 

### Reporting

Squad Leader sends and receives Geomessages to and from other instances of Squad Leader as well as other ArcGIS for the Military apps and services, including Vehicle Commander and GeoEvent Processor. The app has many [settings](#change-settings) that govern outgoing messages. You can [simulate messages](#simulating-messages) if desired, especially for testing and demonstrations.

#### Position reports

The app periodically sends out an automatic position report consisting of the user's location, ID, vehicle type, and other information. To disable outgoing reports, go to **Menu** > **Settings** and disable **Position reports**.

#### Emergency status

Toggle the 911 button to activate or deactivate emergency status:

![911 button](../source/SquadLeader/app/src/main/res/drawable/ic_911_normal.png)

As soon as emergency status is activated or deactivated, the position reports internal timer is reset, and a position report is immediately sent with the appropriate emergency status.

When Squad Leader receives a position report with emergency status activated, it displays the position report on the map and highlights it.

#### Spot reports

The user can create a spot report for observed hostile activities. Spot reports follow the SALUTE format (Size, Activity, Location, Unit, Time, Equipment).

To create a spot report, tap the spot report button:

![Spot report button](../source/SquadLeader/app/src/main/res/drawable/ic_spot_report_normal.png)

Then tap the location on the map for the spot report. A spot report form displays with the location field pre-filled with the location you tapped. Change the form's values as needed and tap the Send button in the upper left corner. The spot report is sent to listening clients, including your own device, which displays the spot report on the map.

#### Chem lights

The user can create a chem light report as a quick way to drop a dot on the map and send it to listening clients. Four colors are available; a unit should predetermine what each color means.

To create a chem light, tap one of the colored chem light buttons:

![Red report button](../source/SquadLeader/app/src/main/res/drawable/ic_chemlights_red_normal.png)

![Yellow report button](../source/SquadLeader/app/src/main/res/drawable/ic_chemlights_yellow_normal.png)

![Green report button](../source/SquadLeader/app/src/main/res/drawable/ic_chemlights_green_normal.png)

![Blue report button](../source/SquadLeader/app/src/main/res/drawable/ic_chemlights_blue_normal.png)

Then tap the location on the map for the chem light. The chem light is sent to listening clients, including your own device, which displays the chem light on the map.

### Viewshed analysis

To run viewshed analysis, tap the viewshed button:

![Viewshed button](../source/SquadLeader/app/src/main/res/drawable/ic_viewshed_normal.png)

Then tap the location on the map for the viewshed analysis. If the tapped location is within the extent of the elevation raster configured for viewshed analysis (see [configuration instructions](#configuring-viewshed-analysis)), a viewshed will display on the map. Tap another point to recalculate the viewshed. To remove the viewshed, tap the clear viewshed button:

![Viewshed button](../source/SquadLeader/app/src/main/res/drawable/ic_clear_viewshed_normal.png)

See some [limitations of Squad Leader's viewshed analysis](#limitations-of-viewshed-analysis).

### Change settings

To change application settings, in the menu, choose **Settings**. You can change various settings:

- **Angular units**: choose the units, such as degrees or mils, that the app uses for displaying headings and bearings.
- **Username**: the username displayed for outgoing reports. For semantic reasons, the username should be unique.
- **Vehicle type**: the vehicle type used in outgoing reports.
- **Unique ID**: the user's unique ID. This ID should be unique. If other users use the same unique ID, clients will treat their messages as if they had come from the same user.
- **Symbol ID Code**: the MIL-STD-2525C symbol ID code (SIC or SIDC) associated with the user in outgoing position reports.
- **Position reports**: checked to automatically send periodic position reports to listening clients.
- **Position report period**: the number of milliseconds between outgoing position reports.
- **Messaging port**: the UDP port on which messages are sent and received. The port number must be between 1024 and 65535. All apps using the same port and connected to the same router will communicate with each other.
- [**Reset map**](#reset-the-map)

To change the location mode, in the menu, choose Set Location Mode. A dialog appears with various location mode choices:

- **Hardware (GPS)**: the app uses the device's location capabilities, including GPS if available, to obtain the user's location, speed, and heading.
- **Simulation (Built-in)**: the app uses GPS points in Monterey, California, to simulate the user's location, speed, and heading.
- **Simulation (GPX File)**: the app uses points from a GPX file to simulate the user's location, speed, and heading. After choosing this option, select a GPX file on your device.

Squad Leader remembers the location mode and GPX choices and uses them on the next run of the app. If **Simulation (GPX File)** is the stored preference and the GPX file is not found, Squad Leader changes the preference to **Simulation (Built-in)**.

### Add a layer

Squad Leader allows users to add layers to the map. In the menu, choose **Add Layer**, then follow the following instructions for the type of layer you would like to add:
- **ArcGIS Server service:** . Select **From Web** if it is not already selected. Type or paste the URL of an ArcGIS Server map service, feature service, or image service. Check the Use as Basemap checkbox if you want the added layer to be one of the app's basemaps, or leave it unchecked to add the layer on top of the current basemap. Tap Add Layer, and the layer appears on the map.
- **GeoMessage file:** Select **From File** and choose **Choose a file**. Choose the GeoMessage XML file you would like to add. If it is a valid GeoMessage XML file, it appears as a layer on the map.

### Reset the map

You can clear any layers you have added and go back to the original map configuration. To reset the map, in the menu, choose **Settings**, and then choose **Reset map**. Tap **OK** if you want to reset the map. This will reload the map configuration from one of two locations:

1. If /mnt/sdcard/SquadLeader/mapconfig.xml exists on the device, it will be used for resetting the map.
2. Otherwise, Squad Leader's built-in default map configuration will be used.

