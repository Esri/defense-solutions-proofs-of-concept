<p>The following web maps need to be created with the following layers:</p>     
<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Map Name</td>
						<td>Initial Service Name</td>
						<td>Layer Name in Map</td>
						<td>Properties</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td rowspan = "3">Air Plan (Next 24)</td>
						<td>2DRangeDomes</td>
						<td>Range Domes</td>
						<td></td>
					</tr>
					<tr>
						<td>ATO_Missions</td>
						<td>Future Missions (Next 24)</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>ACO_Use</td>
						<td>Airspace Control Means</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td rowspan = "4">Air Plan (Next 24) - Assault</td>
						<td>2DRangeDomes</td>
						<td>Range Domes</td>
						<td></td>
					</tr>
					<tr>
						<td>ATO_Missions</td>
						<td>Future Missions (Next 24)</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>ACO_Use</td>
						<td>Airspace Control Means></td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>Airc2WeatherImage</td>
						<td>Weather Variables</td>
						<td>as per instructions below</td>
					</tr>
					<tr>
						<td rowspan = "4">Air Plan (Next 24) - Defense</td>
						<td>2DRangeDomes</td>
						<td>Range Domes</td>
						<td></td>
					</tr>
					<tr>
						<td>ATO_Missions</td>
						<td>Future Missions (Next 24)</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>ACO_Use</td>
						<td>Airspace Control Means</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>Airc2WeatherImage</td>
						<td>Weather Variables</td>
						<td>as per instructions below</td>
					</tr>
					<tr>
						<td rowspan = "11">Recognized Air Picture - (Live View)</td>
						<td>2DRangeDomes</td>
						<td>Range Domes</td>
						<td></td>
					</tr>
					<tr>
						<td>StreamServiceOut-MilitaryFlights-Default</td>
						<td>Military Air Tracks (Stream Service)</td>
						<td>filter - alt > 0.9</td>
					</tr>
					<tr>
						<td>StreamServiceOut-CivilianFlights-Default</td>
						<td>Civilian Air Tracks (Stream Service)</td>
						<td></td>
					</tr>
					<tr>
						<td>Land_Installations</td>
						<td>Land Installations</td>
						<td></td>
					</tr>
					<tr>
						<td>Airspace_Alerts</td>
						<td>Airspace Alerts</td>
						<td></td>
					</tr>
					<tr>
						<td>Military_Air_Tracks</td>
						<td>Targets</td>
						<td></td>
					</tr>
					<tr>
						<td>AirControlOrder_status</td>
						<td>Airspace Control Order - Status</td>
						<td></td>
					</tr>
					<tr>
						<td>ACO_Use</td>
						<td>Airspace Control Order - Use</td>
						<td>Time Enabled</td>
					</tr>
					<tr>
						<td>Land_Installations</td>
						<td>Land Installations</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>Meteorological Service of Canada (WMS)</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>Live Weather Radar</td>
						<td></td>
					</tr>
					<tr>
						<td rowspan = "10">Recognized Air Picture (RAP)</td>
						<td>Land_Installations</td>
						<td>Land Installations</td>
						<td></td>
					</tr>
					<tr>
						<td>AirspaceAlerts</td>
						<td>Airspace Alerts</td>
						<td></td>
					</tr>
					<tr>
						<td>Military_Air_Tracks</td>
						<td>Military Air Tracks</td>
						<td>Altitude/Depth >1</td>
					</tr>
					<tr>
						<td>Civilian_Air_Tracks</td>
						<td>Civilian_Air_Tracks</td>
						<td></td>
					</tr>
					<tr>
						<td>2DRangeDomes</td>
						<td>Range Domes</td>
						<td></td>
					</tr>
					<tr>
						<td>Military_Air_Tracks</td>
						<td>Targets</td>
						<td>Altitude/Depth >1</td>
					</tr>
					<tr>
						<td></td>
						<td>Meteorological Service of Canada OWS Data Server - WMS</td>
						<td>Meteorological Service of Canada - Can be accessed <a href="https://ec.gc.ca/meteo-weather/default.asp?lang=En&n=C0D9B3D8-1" target="_blank">here</a></td>
					</tr>
					<tr>
						<td></td>
						<td>Live weather radar</td>
						<td>Live weather radar - Can be accessed <a href="https://nowcoast.noaa.gov/arcgis/rest/services/nowcoast/radar_meteo_imagery_nexrad_time/MapServer" target="_blank">here</a></td>
					</tr>
					<tr>
						<td>AirControlOrder_status</td>
						<td>Airspace Control Order - Status</td>
						<td></td>
					</tr>
					<tr>
						<td>ACO_Use</td>
						<td>Airspace Control Order - Use</td>
						<td></td>
					</tr>
					<tr>
						<td rowspan = "2">Air Tasking Order Briefing</td>
						<td>ATO_Missions_Briefing</td>
						<td>ATO_Missions_Briefing</td>
						<td></td>
					</tr>
					<tr>
						<td>Land_Installations</td>
						<td>Land_Installations</td>
						<td></td>
					</tr>
		</table>
<p>Weather Variables</p>
<ol class="steps">
			<li>Add in the Airc2WeatherImage service </li>
			<li>From the drop down menu for the layer select Image Display</li>
			<li>In the renderer drop down menu choose the weather variable you are interested in and click Apply and then Close, for this example we will use Temperature Degrees Fahrenheit</li> 
			<li>Now from the drop down menu for the layer select Rename and rename the layer Temperature - Degrees Fahrenheit </li> 
			<li>Next from the drop down menu choose Transparency and set it to 50%</li>
			<li>Check that time animation is enabled and set the time slider to display data at 3 hour time intervals and only display the data in the current time interval. Once this is set click ok </li> 
<h3 class="icon-notebook">Note</h3>
<p>The instructions above are to add just one weather layer if you want to add multiple then the layer can be copied, a new renderer applied and renamed as the following steps explain.</p>
<li>From the drop down layer associated with the layer select copy, layer named Temperature - Degrees Fahrenheit - Copy will appear</li>
			<li>Repeat steps 3 and 4 choosing a different weather variable and giving the layer a different name.  All the other parameters will be set from the previous layer so unless requiring adjustment will not need to be changed.</li>
			<li>Repeat steps 7 and 8 until you have all the desired layers within your web map.</li>
		</ol>
