<p>ArcGIS Server Data Store connections must be created pointing to ArcGIS Server and or Portal instances hosting the services referenced in the connectors and processors used in the GeoEvent services. </p>
<ol class="steps">
  <li>Create a new ArcGIS server connection named AirC2 AGS Server. In the URL field point to the URL of the ArcGIS Server hosting the AirC2 services (e.g. https://airc2.domain.com/ags/)</li>	
  <li>Create a new Portal Connection named AirC2BDS. Click the check next to 'Use Web Tier Authentication' and add credentials for an ArcGIS Portal user with admin permissions. In the URL field put the rest endpoint of the portal hosting your services.</li>
</ol>