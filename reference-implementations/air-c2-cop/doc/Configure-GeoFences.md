<p>The Air C2 COP GeoEvent services make use of a number of GeoFences to determine if aircraft are flying in certain areas.</p>
<ol class="steps">
  <li>Import GeoFences from the AirC2_ACO_Use feature service. Use the following table to configure the GeoFences:</li>
  <table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
				<tr>
					<td>Registered ArcGIS Server</td>
					<td>AirC2 AGS Server</td>
				</tr>
				<tr>
					<td>Folder</td>
					<td>AirC2</td>
				</tr>
				<tr>
					<td>Service</td>
					<td>AirC2_ACO_Use</td>
				</tr>
				<tr>
					<td>Layer</td>
					<td>Airspace Control Means - Use</td>
				</tr>
				<tr>
					<td>Category Field</td>
					<td>AirspaceControlMeasures</td>
				</tr>
				<tr>
					<td>Replace All GeoFences in Category</td>
					<td>Checked</td>
				</tr>
				<tr>
					<td>Name Field</td>
					<td>name</td>
				</tr>
				<tr>
					<td>Active Field</td>
					<td>(Always Active)</td>
				</tr>
				<tr>
					<td>WKID</td>
					<td>4326</td>
				</tr>
				<tr>
					<td>Max Allowable Offset</td>
					<td> </td>
				</tr>
				<tr>
					<td>Time Extent Start</td>
					<td> </td>
				</tr>
				<tr>
					<td>Time Extent End</td>
					<td> </td>
				</tr>
				</tbody>
			</table>
  <li>Import GeoFences from the 2DRangeDomes feature service. Use the following table to configure the GeoFences:</li>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Field</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
				<tr>
					<td>Registered ArcGIS Server</td>
					<td>AirC2 AGS Server</td>
				</tr>
				<tr>
					<td>Folder</td>
					<td>AirC2</td>
				</tr>
				<tr>
					<td>Service</td>
					<td>AirC2_TargetThreatAreas</td>
				</tr>
				<tr>
					<td>Layer</td>
					<td>Threat Positions Outer</td>
				</tr>
				<tr>
					<td>Category Field</td>
					<td>RangeDomes</td>
				</tr>
				<tr>
					<td>Replace All GeoFences in Category</td>
					<td>Checked</td>
				</tr>
				<tr>
					<td>Name Field</td>
					<td>gtgt_name</td>
				</tr>
				<tr>
					<td>Active Field</td>
					<td>(Always Active)</td>
				</tr>
				<tr>
					<td>WKID</td>
					<td>4326</td>
				</tr>
				<tr>
					<td>Max Allowable Offset</td>
					<td></td>
				</tr>
				<tr>
					<td>Time Extent Start</td>
					<td></td>
				</tr>
				<tr>
					<td>Time Extent End</td>
					<td></td>
				</tr>
				</tbody>
			</table>
			
  <li>Set up range Dome Synchronization Rules. Use the following table to configure synchronization rules for threat domes.</li>

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
					<td>RangeDomes</td>
				</tr>
				<tr>
					<td>Registered ArcGIS Server</td>
					<td>AirC2 AGS Server</td>
				</tr>
				<tr>
					<td>Folder</td>
					<td>AirC2</td>
				</tr>
				<tr>
					<td>Service</td>
					<td>AirC2_TargetThreatAreas</td>
				</tr>
				<tr>
					<td>Layer</td>
					<td>Threat Positions Outer</td>
				</tr>
				<tr>
					<td>Category Field</td>
					<td>RangeDomes</td>
				</tr>
				<tr>
					<td>Name Field</td>
					<td>gtgt_name</td>
				</tr>
				<tr>
					<td>Active Field</td>
					<td>(Always Active)</td>
				</tr>
				<tr>
					<td>GeoSynch Filter Field</td>
					<td></td>
				</tr>
				<tr>
					<td>Refresh Interval</td>
					<td>1 Minutes</td>
				</tr>
				<tr>
					<td>WKID</td>
					<td>4326</td>
				</tr>
				<tr>
					<td>Max Allowable Offset</td>
					<td></td>
				</tr>
				<tr>
					<td>Time Extent Start</td>
					<td></td>
				</tr>
				<tr>
					<td>Time Extent End</td>
					<td></td>
				</tr>
				</tbody>
			</table>
		</ol>
