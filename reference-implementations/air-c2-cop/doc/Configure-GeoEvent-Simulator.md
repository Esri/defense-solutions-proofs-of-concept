<p>After the GeoEvent Inputs have been configured the Geo Event Server is in a position to receive events simulated through the <a href="https://server.arcgis.com/en/geoevent-extension/10.3/administer/simulating-geoevents-from-a-file.htm" target="_blank">GeoEvent Simulator</a>. Two simulations will be run, one for military aircraft and one for civilian aircraft. The simulations are run from text files in CSV format and can be found in the folder files/FlightSimulation in the AirC2COP download. Set up the simulators as per the <a href="https://server.arcgis.com/en/geoevent-extension/10.3/administer/simulating-geoevents-from-a-file.htm" target="_blank">GeoEvent Simulator</a> instructions amending the following properties for each simulator:</p>
<h2>Military Air Tracks</h4>
<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Property</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>File</td>
						<td>MilitaryFlightSimulation-WithTimeGaps.csv or MilitaryFlightSimulation-NoTimeGaps.csv</td>
					</tr>
                         <tr>
						<td>Time Field #</td>
						<td>7</td>
					</tr>
                         <tr>
						<td>Skip the first</td>
						<td>Checked - 1 lines</td>
					</tr>
                         <tr>
						<td>Server Name</td>
						<td>The name of <i>your</i> GEE server</td>
					</tr>
					<tr>
						<td>Server Port</td>
						<td>5600</td>
					</tr>
					<tr>
						<td>Playback rate</td>
						<td>20 events per 1000 m/s</td>
					</tr>
					<tr>
						<td>Continuous Loop</td>
						<td>Checked</td>
					</tr>					
					<tr>
						<td>Set value to Current Time</td>
						<td>Checked</td>
					</tr>
				</tbody>
			</table>
<h2>Civilian Air Tracks</h2>
<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Property</td>
						<td>Value</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>File</td>
						<td>CivilianFlightSimulation.csv</td>
					</tr>
                         <tr>
						<td>Time Field #</td>
						<td>7</td>
					</tr>
                         <tr>
						<td>Skip the first</td>
						<td>Checked - 1 lines</td>
					</tr>
                         <tr>
						<td>Server Name</td>
						<td>The name of <i>your</i> GEE server</td>
					</tr>
					<tr>
						<td>Server Port</td>
						<td>5605</td>
					</tr>
					<tr>
						<td>Playback rate</td>
						<td>Real rate x20 (Enter 2000 in the text box)</td>
					</tr>
					<tr>
						<td>Continuous Loop</td>
						<td>Checked</td>
					</tr>					
					<tr>
						<td>Set value to Current Time</td>
						<td>Checked</td>
					</tr>
				</tbody>
			</table>
