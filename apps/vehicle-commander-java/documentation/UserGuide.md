User Guide - vehicle-commander
====================

* [Running](#running) 
* [Usage](#usage)

## Running

This section assumes that you have deployed the Vehicle Commander application [using the developer instructions](../README.md), or you are running it from the application/VehicleCommander directory.

1. Review the [software requirements](../README.md#software-requirements).
2. Verify your Java Runtime Environment (JRE) installation by typing `java -version` at a command prompt. NOTE: you must use 32-bit Java with 32-bit Vehicle Commander and 64-bit Java with 64-bit Vehicle Commander. When you run `java -version` at the command line, if the output includes the text "64-Bit," then it is 64-bit Java; otherwise, it is 32-bit Java. If your Java version and Vehicle Commander version don't match, you can download and unzip the other Vehicle Commander or you can download the other JRE from [http://www.java.com](http://www.java.com) and install it. If you install a different Java version, you must either add the desired Java to your PATH or use an absolute or relative path to the desired java executable.
2. In your application directory, open mapconfig.xml in a text editor. If mapconfig.xml is absent, the application will open with a blank map. You can define the following:
   * The initial extent, using a point in the map’s spatial reference and a scale.
   * The spatial reference of the map, using a well-known ID (WKID). Web Mercator (WKID 3857) is used by ArcGIS Online services and is normally recommended.
   * The map layers. You can add the following types of layers:
      * TiledCacheLayer: a compact map cache, or a tile package (.tpk) enabled for ArcGIS Runtime. Set the value of `<datasetpath>` to the .tpk filename or cache directory, using a relative or absolute path. The cache directory is the one that contains conf.xml and conf.cdi.
      * TiledMapServiceLayer: an ArcGIS Server cached map service or image service. Set the value of `<url>` to the service REST URL.
      * LocalDynamicMapLayer: an ArcGIS map package (.mpk) enabled for ArcGIS Runtime. Set the value of `<datasetpath>` to the .mpk filename, using a relative or absolute path.
      * DynamicMapServiceLayer: an ArcGIS Server dynamic map service or image service. Set the value of `<url>` to the service REST URL.
      * Mil2525CMessageLayer: an XML file of military messages with MIL-STD-2525C symbol codes. Use one `<geomessages>` tag containing one or more `<geomessage>` tags formatted as follows. Lines and polygons can be used (with appropriate symbol codes) by creating semicolon-separated lists of points in the `<_control_points>` element.

     &lt;geomessages v="1.1"> <br>
     &lt;geomessage>  <br>
     &lt;_type&gt;position_report&lt;/_type&gt; <br>
     &lt;_action&gt;UPDATE&lt;/_action&gt; <br>
     &lt;_id&gt;{3bf3e432-94c5-4db8-b9a1-42318708af74}&lt;/esri_id&gt; <br>
     &lt;_control_points&gt;7843104.64,4087771.88&lt;/_control_points&gt; <br>
     &lt;_wkid&gt;3857&lt;/_wkid&gt; <br>
     &lt;sic&gt;SFGPUCII---F---&lt;/sic&gt; <br>
     &lt;/geomessage&gt; <br>
     &lt;/geomessages&gt; <br>
          
3. (Optional) If you want to run viewshed analysis in the application, add a `<viewshed>` element to mapconfig.xml. It should be a child of the `<mapconfig>` element and should contain the following child elements:

   * `<servicepath>`: the path to a GPK, or the URL to a geoprocessing service.
   * `<taskname>`: the name of the viewshed task in the GPK or service.
   * `<observerheight>`: the observer height (in the units of the elevation dataset being used by the GPK or service) to use for viewshed analysis.
   * `<observerparamname>`: the name of the observer feature set parameter.
   * `<observerheightparamname>`: the name of the observer height parameter.
   * `<radiusparamname>`: the name of the radius parameter.

4. Run the Vehicle Commander application:
   * Open a command prompt and navigate to application/VehicleCommander folder
   * Enter:  `java -jar VehicleCommander.jar`
   * At the end of the command line, you can pass the following parameters:
   * -mapconfig `<XML file>`: use a map configuration file other than the mapconfig.xml file located in the application directory.
   * -license `<license string or file>`: use a license other than the one compiled into the application. This can be either the license string itself or the name of a file containing only the license string. 
   * -exts <extension license strings or file>: use a set of extension license strings other than the ones compiled into the application. This can be either a semicolon-separated list of extension license strings or the name of a file containing only a semicolon-separated list of extension license strings. 

The application opens as shown in [Usage](#usage).

* If the application crashes, it is possible the machine does not have the proper OpenGL capabilities. Refer to [Hardware Requirements](../README.md#hardware-requirements) to learn how to verify OpenGL.
*  If the map opens blank, verify the paths in mapconfig.xml.
5.If you want to change user settings, click the Main Menu button and go to Options > Settings. A dialog box lets you change the user settings described in [Deploying the Application.](../README.md#deploying-the-application)

## Usage

![Image of Vehicle Commander](../ScreenShotLabels.png "vehicle-commander")

Vehicle Commander provides high performance mapping, situational awareness, and reporting for mounted units. It is intended for touchscreen use, though it also works properly with a mouse; in that sense, the words "click" and "tap" are interchangeable in this section.

### Mapping

To pan the map, press the mouse and drag, or use the navigation buttons. To zoom in or out, use the mouse wheel or the navigation buttons. To navigate to an MGRS coordinate, go to Main Menu > Navigation and type a valid MGRS string, then type Enter or tap the Go button.

To change the basemap, tap the Basemaps button to open the basemap gallery and tap one of the basemap icons.

To add or remove an MGRS grid, tap the Grid button.

To display the vehicle’s simulated GPS location and broadcast it to other applications, click the Main Menu button and go to Options > Show Me. A green icon indicates the current GPS location and heading, and the map enters Follow Me mode, following the current GPS location. To exit Follow Me mode, zoom or pan the map. To reenter Follow Me mode at any time, click the Follow Me button, located in the lower right corner with the navigation buttons.

While in Follow Me mode, three navigation modes are available and are selected with navigation buttons:

* North Up: the map stays rotated so that north is up, until the user manually rotates the map with the V and B keys.
* Track Up: the map rotates so that the current GPS direction is up.
* Waypoint Up: the map rotates so that the direction from the current GPS location to the selected waypoint is up. To select a waypoint, go to Main Menu > Waypoints. You can read about creating waypoints elsewhere in this document.

To rotate the map clockwise, press and hold the V key. To rotate the map counterclockwise, press and hold the B key. To clear the rotation and orient the map with north up, press the N key.

To add a map overlay, go to Main Menu > Overlays. You can add an ArcGIS map package (.mpk) enabled for ArcGIS Runtime. Clicking Map File opens a file chooser dialog. Navigate to the .mpk of your choice.

You can click the map to identify features from map packages, as well as MIL-STD-2525C symbols on the map. Identified items are shown in a panel. Click the previous and back buttons to view the attributes of identified items. Click the X button to close the identify panel.

A panel at the bottom of the application displays the current position, heading, and time. To change the units used to display the heading, go to Main Menu > Options > Settings. For Heading Units, choose Degrees or Mils and click OK.

To close the Main Menu, click the back arrow button at the top of the menu.

### Situational awareness and reporting

The map displays moving locations of friendly forces if you run the Vehicle Commander on multiple machines that are connected to the same network router. These machines must have the messaging port (default 45678; see [Deploying the Application](../README.md#deploying-the-application) ) open for UDP sending and receiving in the machine’s firewall settings. They must also have unique IDs set under Main Menu > Options > Settings > Unique ID.

You can use [GeoMessage Simulator](https://github.com/Esri/geomessage-simulator-qt) to send messages to Vehicle Commander if desired. Vehicle Commander and GeoMessage Simulator must be set to the same UDP port and must be connected to a router that allows UDP traffic. See [GeoMessage Simulator](https://github.com/Esri/geomessage-simulator-qt) for details.

Toggle the 911 button to indicate to friendly forces that you need immediate assistance. Your position marker will flash on the display of other vehicles that receive your position reports. Toggle the 911 button off to clear your emergency status.

Use the Chem Light buttons to create digital chem lights. Click a color, and then click the map to place a chem light. The chem light appears on your map, as well as the maps in vehicles that receive your position reports. In the field, different colors of chem lights would have different predetermined meanings to all friendly forces.

To create a spot report:

* Go to Main Menu > Reports > Spot Report.
* Click the top button (Size) to get started. The spot report wizard helps you enter a value for each spot report field in the SALUTE format (Size, Affiliation, Location, Unit, Time, Equipment). Rather than entering text, most fields offer preset fat buttons ideal for quick touchscreen use.
* Go through the fields and choose values.

      1. For Location, choose From Map and click the location for the spot report.
      2. For Equipment, you can choose one of the preset symbols, or you can search for any MIL-STD-2525C symbol by name, tags, or symbol code.
		
* After completing the final field, Equipment, you can click any field to change its value before sending.
* When you are satisfied with your spot report values, click Send. The spot report displays on your map, as well as the maps in vehicles that receive your position reports.

### Analysis

When properly configured, the application provides advanced geospatial analysis. Tap the Tools button to open the toolbar.
To calculate a viewshed, tap the Viewshed button and follow the dialog’s instructions:

1. Tap a point on the map.
2. Select a viewshed radius in one of various ways:
   * Tap a second point.
   * Type a radius.
   * Choose a preset viewshed radius.
3. Tap the Go button. The viewshed is calculated and displayed on the map.

Calculating a new viewshed removes the previous viewshed from the map. To hide the viewshed, go to Main Menu > Overlays and turn off the Viewshed overlay.

You can use the Route panel to create a route with waypoints. To create a route, tap the Route button and follow the Route panel’s instructions:

* Tap the map to add a waypoint.
* Drag the map to draw a route segment.
* Toggle the Draw Route button:
* Select the button to add waypoints and route segments.
* Deselect the button to activate panning and identifying. This is useful if the route you want to draw will go off the edge of the map. Turn off drawing, pan the map, and turn on drawing to continue placing the route.
* Tap the Undo button to remove the last route segment or waypoint created.
* Tap the Clear button to delete all routes and waypoints. The Clear operation cannot be undone.

To hide the route, go to Main Menu > Overlays and turn off the Route overlay.