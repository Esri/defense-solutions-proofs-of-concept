<p>In order to be used within 3D web applications a couple of layers must be published that are created using the <a href="https://www.esri.com/library/whitepapers/pdfs/multipatch-geometry-type.pdf" target="_blank">Multipatch Geometry Type</a>.</p>
<h2>Airspace Control Order</h2>
<ol class="steps">
  <li>Ensuring that you have successfully completed Step 7 of the <a href="#" target="_blank">Copy Sample Data to</a> section, your AirC2_ACO_POLYGON feature class should contain a number of features representing the current Airspace Control Means derived from the Airspace Control Order.</li>    
  <li>Open ArcCatalog.</li>
  <li>Open the Airspace Management Tools toolbox in tools folder.</li>
  <li>Double-click the Convert Airspace Control Order to 3D script.</li>
  <li>Specify the following parameters:</li>
    <ul>
      <li>Source: The AirC2_ACO_Polygon feature class generated using the Process Airspace Control Order Script within your Enterprise Geodatabase.</li>
      <li>Elevation Model: The DEM source raster, which provides the elevation values for the calculation. If you want to generate features that are above sea level (flat top and bottom) then a raster with a constant value of 0 is needed, detailed instructions on how to create this are contained within the tool help.</li>
<h3 class="icon-notebook">Note</h3>
<p>The projection of the DEM must be WGS 1984 Web Mercator Auxiliary Sphere coordinate system (ESPG: 102100) to match the projection of the base mapping within portal.</p>
      <li>Target Workspace: The target workspace to write the output file to, it will automatically be named AirC2_ACO_Polygon3D.</li>
    </ul>
  <li>Click OK to run the tool.</li>
<h3 class="icon-alert">Warning</h3>
<p>This script can take some time to process depending on the number of features in the ACO and the resolution of the DEM used.</p>
  <li>Once the script has completed open the ArcGIS Pro project created in the [[Publish 2D|https://github.com/Esri/air-c2-cop-python/wiki/Publish-2D-Services]] section.</li>
  <li>Create a new scene.</li>
  <li>Add the AirC2_ACO_POLYGON_3D multipatch feature class to the scene.</li>
  <li>Apply desired symbology.</li>
  <li><a href="http://pro.arcgis.com/en/pro-app/help/sharing/overview/introduction-to-sharing-web-layers.htm" target="_blank">Share the Layer as a Web Layer </a> with the following properties:</li>
  <ul>
    <li><b>Name:</b> AirC2_ACO_POLYGON_3D (scene layers are published as a hosted feature service so no need to prefix service name with the AirC2 folder).</li>
    <li><b>Layer Type:</b> Scene.</li>
    <li><b>Summary:</b> Scene displaying the Airspace control means derived from the Airspace Control Order symbolized by the control means type. Used with the AirC2 3D App.</li>
    <li><b>Tags:</b> AirC2, ACO, Airspace Control Order, Air Plan, Air Operations, Military</li>
  </ul>
</ol>
<h2>Target Threat Areas / Range Domes (Optional)</h2>
<p>Optionally you can create 3D threat areas around each of your target locations, to achieve this use the <a href="/defense/help/range-dome-analysis/" target="_blank">Range Dome Analysis</a> template.</p>