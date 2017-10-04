<p>Once you have completed the <a href="/defense/help/air-c2-cop/get-started/copy-sample-data/" target="_blank">Copy Sample Data</a> the next step is to publish the map documents in the maps folder as <a href="http://links.esri.com/Server/Publishingfeatureservice/" target="_blank">feature services using ArcGIS for Server</a>.</p>
<h2>Publish .mxd documents using ArcMap</h2>
<ol class="steps">
  <li>First of all create a <a href="http://server.arcgis.com/en/server/latest/publish-services/linux/adding-a-gis-server-folder-in-manager.htm" target="_blank">folder</a> called AirC2 on your GIS Server to act as a container for your services.</li>    
  <li>Individually publish each of the map documents in the maps folder as feature services (do this by ensuring you have Feature Access enabled under the service capabilities) ensuring they are placed into the AirC2 folder. Leave the service names as the default value i.e. the same as the Map document name</li>
  <ul>
    <li>Before publishing the AirC2_ACO_Use .mxd recalculate the time extent on the 'Airspace Control Means - Use' layer by going to the layer properties, selecting the time tab and click the calculate button. Set the time interval to one hour.</li>
  </ul>
 <h3 class="icon-notebook">Note</h3>
 <p>If your Enterprise GeoDatabase is registered with your server and the data paths are correct you should not receive any warning messages about data being copied to the server when you analyze the service. If you do get an error message correct it before continuing as this will cause problems when updating the Air Orders at a later date.</p>
</ol>
<h2>Publish .mapx files using ArcGIS Pro</h2>
<p>The military air tracks must be published through ArcGIS Pro 1.3 to an ArcGIS Server 10.4 to utilize the military symbology dictionary renderer:</p>
  <ol class="steps">
    <li>Open ArcGIS Pro.</li>
    <li>From the Project templates sidebar select blank project.</li>
    <li>The Create a New Project dialog box appears. Type AirC2 as the name for the new project and change where the project is created to the maps folder in the AirC2COP download.</li>
    <li><a href="http://pro.arcgis.com/en/pro-app/help/data/databases/set-up-a-database-connection.htm" target="_blank">Set up a database connection</a> to your Enterprise Geodatabase that you copied the AirC2 data to.</li> 
    <li>On the Insert Ribbon select Import Map.</li>
    <li>Navigate to the maps folder in the AirC2COP download and select MilitaryAirTracks.mapx</li>
    <li><a href="http://pro.arcgis.com/en/pro-app/help/mapping/map-authoring/repair-broken-data-links.htm" target="_blank">Repair the layers data source</a> to the AirC2_MilitaryAirTracks layer in your Enterprise GeoDatabase. If correct the symbology of the features should look similar to:</li>
    <img src="/defense/help/air-c2-cop/img/symbology.png">
    <li><a href="http://pro.arcgis.com/en/pro-app/help/sharing/overview/introduction-to-sharing-web-layers.htm" target="_blank">Share the Layer as a Web Layer </a> with the following properties:</li>
      <ul>
        <li><b>Name:</b> AirC2\AirC2_MilitaryAirTracks (prefixing the service name with AirC2 ensures the service is published under the AirC2 folder on your server).</li>
        <li><b>Data:</b> Reference registered data.</li>
        <li><b>Layer Type:</b> Feature.</li>
        <li><b>Summary:</b> Military Features that support air units and equipment.</li>
        <li><b>Tags:</b> Military, MIL-STD-2525D, AirC2, Tracks, Air Operations.</li>
      </ul>
    <li>Once completed you should have the following services running within you AirC2 folder:</li>
    <img src="/defense/help/air-c2-cop/img/services.png">
  </ol>    
