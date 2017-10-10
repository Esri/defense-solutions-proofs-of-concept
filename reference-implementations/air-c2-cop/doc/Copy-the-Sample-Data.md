<p>For the template to function correctly the data <b>must</b> be held within an Enterprise Geodatabase which is registered with your ArcGIS Server (click <a href="http://server.arcgis.com/en/server/latest/publish-services/windows/overview-register-data-with-arcgis-server.htm" target="_blank">here</a> to learn more):</p>
  <ol class="steps">
    <li>Open ArcCatalog.</li>    
    <li>Navigate to the location where you extracted the AirC2COP1.0.0.zip file and expand the tools folder.</li>
    <li>Expand the Airspace Management Tools toolbox and double click the tool named Copy Source Data And Update MXD Paths.</li>
    <li>Specify the following parameters:</li>    
    <ul>
      <li>Source Database: AirC2.gdb found under the data folder in the AirC2COP download.</li>
      <li>Target Database: Specify the database connection file that connects to the enterprise geodatabase that is registered with your server.</li>
      <li>Target Maps Folder: The maps folder in the AirC2COP download.</li>
    </ul>
    <li>Click OK to run the tool.</li>
    <li>On script completion check the data sources have been updated to your Enterprise GeoDatabase by performing a right click and selecting <a href="http://desktop.arcgis.com/en/arcmap/latest/manage-data/using-arccatalog/setting-data-sources.htm" target="_blank">Set Data Source(s)</a> on one of the map documents found under the maps folder.</li>
    <li>Process the Airspace Control Order and the Airspace Tasking Order found under files\SampleACOandATO.</li>
    <li>During the update data source process the symbology of one of the layers in the AirC2_ACO_Use.mxd will be dropped, repair this by doing the following:</li>
    <ul>
      <li>Using ArcMap open the AirC2_ACO_Use.mxd.</li>
      <li>Open the properties of the Airspace Control Means - Use layer.</li>
      <li>Select the Symbology tab.</li>
      <li>Click Import.</li>
      <li>Click the folder icon and navigate to the AirC2_Airspace Control Means - Use.lyr found under the maps folder in the AirC2COP download.</li>
      <li>Click Add.</li>
      <li>Click OK.</li>
      <li>For the Value field select Use.</li>
      <li>Click OK.</li>
      <li>Click Apply.</li>
      <li>Click OK to close the properties dialog.</li>
      <li>Save the map.</li>
    </ul>
  </ol>