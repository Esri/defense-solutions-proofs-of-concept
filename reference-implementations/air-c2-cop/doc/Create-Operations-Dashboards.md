<p>The above dashboard has 4 count widgets, 5 list widgets, 2 gauges and 2 feature detail widgets. Below is an explanation of how to create these styles of widgets and then the parameters for the specific settings used in the dashboard shown above.</p>
## General Settings
<ul>
          <li> Refresh set as type of <b>'Use a single interval to update data sources'</b> and at an interval of 6 seconds</li>
          <li> Web map used is <b>Recognized Air Picture (RAP)</b></li>
       </ul>
<p>Setting up a Summary widget</p>
<ol class="steps">
        <li>From Widgets - Add Widget choose the <b>Summary</b> Widget</li>
        <li>Set the Display Type as <b>Count</b> or <b>Statistics</b></li>
		<li>Set the Data Source, Title and Description.</li>
		<li>Set the Appearance tab settings</li>
      </ol>	
	  
## Setting up a List widget
<ol class="steps">
        <li>From Widgets - Add Widget choose the <b>List</b> Widget</li>
		<li>Set the Data Source, Title and Description.</li>
		<li>Configure the Data, Feature Display and Feature Actions Tabs for what you want to show.</li>
      </ol>	
	  
	  <p>Setting up a Gauge widget</p>
	  <ol class="steps">
        <li>From Widgets - Add Widget choose the <b>Gauge</b> Widget</li>
		<li>Set the Data Source, Title and Description parameters.</li>
		<li>Configure the Data, Target Range and Appearance Tabs for what you want to show.</li>
      </ol>	
	  
## Setting up a Feature Detail widget
<ol class="steps">
        <li>From Widgets - Add Widget choose the <b>Feature Details</b> Widget</li>
		<li>Set the Data Source, Title.</li>
		<li>Under Title and Description set the Description type and choose the fields that you want to display. </li>
		<li>Configure the Filed Format, Media and Capabilities tabs if appropriate.</li>
      </ol>	
<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td width = "5%">Gauge Number</td>
						<td width = "10%">Type</td>
						<td width = "30%">Data Source</td>
						<td width = "55%">Properties</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>1</td>
						<td>Summary</td>
						<td>Filter - Alt > 1</td>
						<td>Display Type: Count</td>
					</tr>
					<tr>
						<td>2</td>
						<td>List</td>
						<td>Filter - Alt > 1</td>
						<td>Sort Field: Unique Designation <p>Selection Mode: Single</p><p>Title: {uniquedesignation}</p></td>
					</tr>
					<tr>
						<td>3</td>
						<td>List</td>
						<td>Civilian_Air_Tracks</td>
						<td>Sort Field: ident <p>Selection Mode: Single</p><p>Title: {id}</p></td>
					</tr>
					<tr>
						<td>4</td>
						<td>Feature Details</td>
						<td><i>enterprise database</i>.ATO.GENTEXT</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>5</td>
						<td>Gauge</td>
						<td>Airspace Control Means</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>6</td>
						<td>Summary</td>
						<td>Civilian Air Tracks Selection</td>
						<td>Display Type: Statistic<p>Value Field: heading</p><p>Operation: Average</p></td>
					</tr>
					<tr>
						<td>7</td>
						<td>Gauge</td>
						<td>Civilian Air Tracks Selection</td>
						<td>Display Type: Statistic<p>Value Field: alt</p><p>Operation: Average</p></td>
					</tr>
					<tr>
						<td>8</td>
						<td>List</td>
						<td>AirspaceAlerts - Military Range Dome Alerts</td>
						<td>Sort Field: Callsign <p>Selection Mode: Single</p><p>Title: {callsign}</p></td>
					</tr>
					<tr>
						<td>9</td>
						<td>Summary</td>
						<td>AirspaceAlerts - Military Range Dome Alerts</td>
						<td>Display Type: Count</td>
					</tr>
					<tr>
						<td>10</td>
						<td>List</td>
						<td>AirspaceAlerts - Airspace Control Measure Alert</td>
						<td>Sort Field: Callsign <p>Selection Mode: Single</p><p>Title: {callsign}</p></td>
					</tr>
					<tr>
						<td>11</td>
						<td>Summary</td>
						<td>AirspaceAlerts - Airspace Control Measure Alert</td>
						<td>Display Type: Count</td>
					</tr>
					<tr>
						<td>12</td>
						<td>List</td>
						<td>Filter - Status = Active</td>
						<td>Sort Field: Name <p>Selection Mode: Single</p><p>Title: {name}</p></td>
					</tr>
					<tr>
						<td>13</td>
						<td>Count</td>
						<td>Filter - Status = Active</td>
						<td>Display Type: Count</td>
					</tr>
				</tbody>
	</table>			
