<p>The AirC2 GeoEvent Services use 2 input connectors. The first is configured to receive simulation feeds for civilian aircraft. The second is configured to receive simulations for military aircraft.</p>
<ol class="steps">
        	<li>Create a new Receive Text Over TCP Input Connector named Airc2-tcp-text-in-civilian-airtracks. Use the following table to configure the connector:</li>
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
						<td>Airc2-tcp-text-in-civilian-air-tracks</td>
					</tr>
					<tr>
						<td>Server Port</td>
						<td>5605</td>
					</tr>
					<tr>
						<td>Message Separator</td>
						<td>\n</td>
					</tr>
					<tr>
						<td>Attribute Separator</td>
						<td>,</td>
					</tr>
					<tr>
						<td>Expected Date Format</td>
						<td></td>
					</tr>
					<tr>
						<td>Incoming data contains GeoEvent Definition</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Create Fixed GeoEvent Definitions</td>
						<td>No</td>
					</tr>
					<tr>
						<td>GeoEvent Definition Name (existing)</td>
						<td>Airc2-CivilianAirTracksIn</td>
					</tr>
					<tr>
						<td>Construct Geometry Fields</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>X Geometry Field</td>
						<td>lon</td>
					</tr>
					<tr>
						<td>Y Geometry Field</td>
						<td>lat</td>
					</tr>
					<tr>
						<td>Z Geometry Field</td>
						<td>alt</td>
					</tr>
					<tr>
						<td>Wkid Geometry Field</td>
						<td></td>
					</tr>
					<tr>
						<td>Well Known Text Geometry Field</td>
						<td></td>
					</tr>
					<tr>
						<td>Language for Number Formatting</td>
						<td></td>
					</tr>
				</tbody>
			</table>
<li>Create a new Receive Text Over TCP Input Connector named Airc2-tcp-text-in-military-airtracks. Use the following table to configure the connector:</li>
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
						<td>Airc2-tcp-text-in-military-air-tracks</td>
					</tr>
					<tr>
						<td>Server Port</td>
						<td>5600</td>
					</tr>
					<tr>
						<td>Message Separator</td>
						<td>\n</td>
					</tr>
					<tr>
						<td>Attribute Separator</td>
						<td>,</td>
					</tr>
					<tr>
						<td>Expected Date Format</td>
						<td></td>
					</tr>
					<tr>
						<td>Incoming data contains GeoEvent Definition</td>
						<td>No</td>
					</tr>
					<tr>
						<td>Create Fixed GeoEvent Definitions</td>
						<td>No</td>
					</tr>
					<tr>
						<td>GeoEvent Definition Name (existing)</td>
						<td>Airc2-MilitaryAirTracksIn</td>
					</tr>
					<tr>
						<td>Construct Geometry Fields</td>
						<td>Yes</td>
					</tr>
					<tr>
						<td>X Geometry Field</td>
						<td>lon</td>
					</tr>
					<tr>
						<td>Y Geometry Field</td>
						<td>lat</td>
					</tr>
					<tr>
						<td>Z Geometry Field</td>
						<td>alt</td>
					</tr>
					<tr>
						<td>Wkid Geometry Field</td>
						<td></td>
					</tr>
					<tr>
						<td>Well Known Text Geometry Field</td>
						<td></td>
					</tr>
					<tr>
						<td>Language for Number Formatting</td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</ol>
