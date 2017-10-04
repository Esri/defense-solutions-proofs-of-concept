<p>To run the AirC2 GeoEvent simulations, some output connectors are required. First, several Update Feature outputs are needed to generate real-time modifications to feature services. These outputs are used to show aircraft tracking behavior, to render ACOs as active vs. inactive, and to generate alerts. Additionally, two Stream Service outputs will be created to demonstrate streaming behavior in web maps.</p>
<ol class="steps">
  <li>Create a new Update a Feature output connector named AirC2-UpdateFeatureService-ACOStatus. Use the following table to configure the connector:</li>
		<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-UpdateFeatureService-ACOStatus</td>
					</tr>
					<tr>
						<td>ArcGIS Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service Name</td>
						<td>AirC2_ACO_Status (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>Airspace Control Means - Status (0)</td>
					</tr>
					<tr>
						<td>Unique Feature Identifier Field</td>
						<td>name</td>
					</tr>
					<tr>
						<td>Update Interval (seconds)</td>
						<td>0.1</td>
					</tr>
					<tr>
						<td>Generate Flat JSON</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>Formatted JSON</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Delete Old Features</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Maximum Features per Transaction</td>
						<td>500</td>
					</tr>
					<tr>
						<td>Update Only</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>
               <li>Create a new Update a Feature output connector named AirC2-UpdateFeatureService-TargetThreatAlerts. Use the following table to configure the connector:</li>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-UpdateFeatureService-TargetThreatAlerts</td>
					</tr>
					<tr>
						<td>ArcGIS Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service Name</td>
						<td>AirC2_AirspaceAlerts (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>Military Range Dome Alert (0)</td>
					</tr>
					<tr>
						<td>Unique Feature Identifier Field</td>
						<td>callsign</td>
					</tr>
					<tr>
						<td>Update Interval (seconds)</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Generate Flat JSON</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>Formatted JSON</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Delete Old Features</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>Maximum Feature Age (minutes)</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Frequency of Deleting Old Features (seconds)</td>
						<td>10</td>
					</tr>
					<tr>
						<td>Time Field in Feature Class</td>
						<td>alerttime</td>
					</tr>
					<tr>
						<td>Maximum Features per Transaction</td>
						<td>500</td>
					</tr>
					<tr>
						<td>Update Only</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>	
			<li>Create a new Update a Feature output connector named AirC2-UpdateFeatureService-AirspaceControlMeasureAlert. Use the following table to configure the connector:</li>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-UpdateFeatureService-AirspaceControlMeasureAlert</td>
					</tr>
					<tr>
						<td>ArcGIS Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service Name</td>
						<td>AirC2_AirspaceAlerts (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>Airspace Control Measure Alert (1)</td>
					</tr>
					<tr>
						<td>Unique Feature Identifier Field</td>
						<td>callsign</td>
					</tr>
					<tr>
						<td>Update Interval (seconds)</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Generate Flat JSON</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>Formatted JSON</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Delete Old Features</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>Maximum Feature Age (minutes)</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Frequency of Deleting Old Features (seconds)</td>
						<td>10</td>
					</tr>
					<tr>
						<td>Time Field in Feature Class</td>
						<td>alerttime</td>
					</tr>
					<tr>
						<td>Maximum Features per Transaction</td>
						<td>500</td>
					</tr>
					<tr>
						<td>Update Only</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>
			<li>Create a new Update a Feature output connector named AirC2-UpdateFeatureService-MilitaryAirTracks. Use the following table to configure the connector:</li>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-UpdateFeatureService-MilitaryAirTracks</td>
					</tr>
					<tr>
						<td>ArcGIS Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service Name</td>
						<td>AirC2_MilitaryAirTracks (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>AirTracks (0)</td>
					</tr>
					<tr>
						<td>Unique Feature Identifier Field</td>
						<td>uniquedesignation</td>
					</tr>
					<tr>
						<td>Update Interval (seconds)</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Generate Flat JSON</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>Formatted JSON</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Delete Old Features</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Maximum Features per Transaction</td>
						<td>500</td>
					</tr>
					<tr>
						<td>Update Only</td>
						<td>Yes</td>
					</tr>
				</tbody>
			</table>
               <li>Create a new Update a Feature output connector named AirC2-UpdateFeatureService-CivilianAirTracks. Use the following table to configure the connector:</li>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-UpdateFeatureService-CivilianAirTracks</td>
					</tr>
					<tr>
						<td>ArcGIS Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service Name</td>
						<td>AirC2_CivilianAirTracks (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>Civilian Air Tracks (0)</td>
					</tr>
					<tr>
						<td>Unique Feature Identifier Field</td>
						<td>id</td>
					</tr>
					<tr>
						<td>Update Interval (seconds)</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Generate Flat JSON</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>Formatted JSON</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Delete Old Features</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Maximum Features per Transaction</td>
						<td>500</td>
					</tr>
					<tr>
						<td>Update Only</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>
			<li>Create a new Send Features to a Stream Service output connector named AirC2-StreamServiceOut-CivilianAirTracks. Use the following tables to configure the connector:</li>							
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-StreamServiceOut-CivilianAirTracks</td>
					</tr>
					<tr>
						<td>GeoEvent Definition Name</td>
						<td>AirC2-CivilianAirTracksIn</td>
					</tr>
					<tr>
						<td>ArcGIS Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Stream Service Name</td>
						<td>AirC2-StreamServiceOut-CivilianAirTracks</td>
					</tr>
					<tr>
						<td>Update Interval (seconds)</td>
						<td>0.01</td>
					</tr>
					<tr>
						<td>Formatted JSON</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Enforce Unique TrackID</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>
			
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-StreamServiceOut-CivilianAirTracks</td>
					</tr>
					<tr>
						<td>Geometry Type</td>
						<td>Point</td>
					</tr>
					<tr>
						<td>Display Field Name</td>
						<td>id</td>
					</tr>
					<tr>
						<td>Override</td>
						<td>Unchecked</td>
					</tr>
					<tr>
						<td>Store latest</td>
						<td>Unchecked</td>
					</tr>
					<tr>
						<td>Related Features</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>
			<li>Create a new Send Features to a Stream Service output connector named AirC2-StreamServiceOut-MilitaryAirTracks. Use the following tables to configure the connector:</li>							
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-StreamServiceOut-MilitaryAirTracks</td>
					</tr>
					<tr>
						<td>GeoEvent Definition Name</td>
						<td>AirC2-MilitaryAirTracksIn</td>
					</tr>
					<tr>
						<td>ArcGIS Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Stream Service Name</td>
						<td>AirC2-StreamServiceOut-MilitaryAirTracks</td>
					</tr>
					<tr>
						<td>Update Interval (seconds)</td>
						<td>0.01</td>
					</tr>
					<tr>
						<td>Formatted JSON</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Enforce Unique TrackID</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>
			
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>Name</td>
						<td>AirC2-StreamServiceOut-MilitaryAirTracks</td>
					</tr>
					<tr>
						<td>Geometry Type</td>
						<td>Point</td>
					</tr>
					<tr>
						<td>Display Field Name</td>
						<td>id</td>
					</tr>
					<tr>
						<td>Override</td>
						<td>Unchecked</td>
					</tr>
					<tr>
						<td>Store latest</td>
						<td>Unchecked</td>
					</tr>
					<tr>
						<td>Related Features</td>
						<td>No</td>
					</tr>
				</tbody>
			</table>
		</ol>
