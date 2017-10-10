<p>The AirC2-CivilianTracks service uses the Airc2-tcp-text-in-civilian-air-tracks input connector to read serials from a simulation file transmitted over a TCP socket and performs 5 distinct tasks in real-time:</p>
<ul>
               <li>The tracks are rendered to a feature service without processing to simply display the position of the aircraft being simulated.</li>
               <li>The tracks are sent to a stream service without processing to simply display the position of the aircraft being simulated.</li>
               <li>The service determines whether an aircraft is within an airspace control order (ACO) using the ACO geofences. When an aircraft is detected as within an ACO, the ACO status is changed to 'ACTIVE'. When an aircraft leaves an 'ACTIVE' ACO its status is changed back to 'INACTIVE'. Both horizontal (latitude and longitude) and vertical (altitude) coordinates are used to determine whether an aircraft is within an ACO.</li>
               <li>When an aircraft enters a restricted ACO, an alert is rendered on top of the aircraft indicating that it is inside a restricted zone. Similar to detecting whether an aircraft is inside an ACO.</li>
               <li>The service determines if an aircraft is within a weapon system's range using the Target Threat Area GeoFences. When an aircraft is within the threat area an alert is generated and rendered above the aircraft entering the threat area.</p>
             </ul>
<img src="/defense/help/air-c2-cop/img/airc2_civilianairtracksservice.png">
<ol class="steps">
			<li>Create a new GeoEvent service called AirC2-CivilianTracks.</li>
			<li>Add the AirC2-tcp-text-in-civilian-air-tracks input.</li>
               <li>Drag the AirC2-StreamServiceOut-CivilianAirTracks output service into the GeoEvent Service Designer. Connect the output to the AirC2-tcp-text-in-civilian-air-tracks input connector. This will write the simulated data to a stream service.</li>
               <li>Drag the AirC2-UpdateFeatures-CivilianAirTracks output service into the GeoEvent Service Designer. Connect the output to the AirC2-tcp-text-in-civilian-air-tracks input connector. This will write the simulated data to a feature service.</li>
			<li>Create a new GeoTag Processor. Use the table below to configure the processor. Once configured connect the processor to the AirC2-tcp-text-in-civilian-air-tracks input.</li>
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
						<td>GeoTag ACM on entry</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>GeoTagger</td>
					</tr>
					<tr>
						<td>GeoFence</td>
						<td>AirspaceControlMeasures/*</td>
					</tr>
					<tr>
						<td>Spatial Operator</td>
						<td>Inside Any</td>
					</tr>
					<tr>
						<td>Geometry Field Name</td>
						<td>GEOMETRY</td>
					</tr>
					<tr>
						<td>Geotag Field Name</td>
						<td>ACMNameInside</td>
					</tr>
					<tr>
						<td>GeoTag Format</td>
						<td>Delimited Value</td>
					</tr>
					<tr>
						<td>Include GeoFence Category in GeoTag</td>
						<td>No</td>
					</tr>
					<tr>
						<td>New GeoEvent Definition Name</td>
						<td>AirC2-CivilianAirTrack_InsideACM</td>
					</tr>
				</tbody>
			</table>
<li>Create a new filter called Filter Out Non ACM Tagged Records (Inside). Set the query to:</li>
<ul>
					<li> NOT ACMNameInside = ""</li>
				</ul>
				</br>
			 This filter removes all events that do not have values for the ACMNameInside field. This step is important because the GeoTagger Processor allows all events through regardless whether there is a valid GeoFence tagged to the event. Events that do not enter a GeoFence will have an ACMNameInside value of an empty String (""). These events will be filtered out.
			<li>Create a Field Enricher processor. This processor will enrich the incoming event with fields from any ACO in which it is inside. The fields added to the incoming event are, min_height, max_height, status, and acm. Use the following table to check Field Enricher Processor:</li>
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
						<td>Add Min and Max Heights</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Enricher (Feature Service)</td>
					</tr>
					<tr>
						<td>Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service</td>
						<td>AirC2_ACO_Status (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>Airspace Control Means - Status (0)</td>
					</tr>
					<tr>
						<td>Feature Layer Join Field</td>
						<td>name</td>
					</tr>
					<tr>
						<td>Target Fields</td>
						<td>New Fields</td>
					</tr>
					<tr>
						<td>Enrichment Fields</td>
						<td>min_height, max_height, status, acm</td>
					</tr>
					<tr>
						<td>Field Tags</td>
						<td></td>
					</tr>
					<tr>
						<td>New GeoEvent Definition Name</td>
						<td>AirC2-CivilianAirTrack_InsideACM_Enriched</td>
					</tr>
					<tr>
						<td>GeoEvent Join Field</td>
						<td>ACMNameInside</td>
					</tr>
					<tr>
						<td>Cache Refresh Time Interval</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Maximum Cache Size</td>
						<td>1000</td>
					</tr>
				</tbody>
			</table>
			Connect the Field Enricher Processor to the Filter processor.
			<li>Create a new Filter called Filter Based on Altitude. This filter tests whether an aircraft is within an ACO in the vertical coordinate system by comparing the current altitude of the aircraft to the minimum and maximum heights of an aco (enriched in the previous step) in which the aircraft has crossed in horizontal (latitude, longitude) coordinates. Set the filter query to:</li>
			<ul>
				<li>alt >= ${min_height} AND alt <= ${max_height}</li>
			</ul>
               <br />
			Connect the filter to the Add Min Max Heights Field Enricher processor.
			<li>Create a Field Calculator processor to modify the Status of the ACO. Use the following table to configure the processor.</li>
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
						<td>Calculate Status (Active)</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Calculator</td>
					</tr>
					<tr>
						<td>Expression</td>
						<td>'ACTIVE'</td>
					</tr>
					<tr>
						<td>Target Field</td>
						<td>Existing Field</td>
					</tr>
					<tr>
						<td>Existing Field Name</td>
						<td>status</td>
					</tr>
				</tbody>
			</table>
			Connect the Field Calculator processor to the Filter created in step 8.
			<li>Create a Field Mapper processor. This mapping will map the status and name of the ACO to the appropriate fields.  Use the following tables to configure the processor:</li>
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
						<td>Map Status and Name</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Mapper</td>
					</tr>
					<tr>
						<td>Source GeoEvent Definition</td>
						<td>AirC2-CivilianAirTrack_InsideACM_Enriched</td>
					</tr>
					<tr>
						<td>Target GeoEvent Definition</td>
						<td>AirC2-Update_ACO_Status</td>
					</tr>
				</tbody>
			</table>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Source Fields</td>
						<td>Target Fields</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>ACMNameInside</td>
						<td>name</td>
					</tr>
					<tr>
						<td>status</td>
						<td>status</td>
					</tr>
				</tbody>
			</table>
			<aside class="note">    
				<h5 class="icon-notebook">Note</h5>
				<div class="note-content">
					<p>The first time setting up the service the input definition created when the event passes through the Add Min and Max Heights Field Enricher will not be present. Run the simulation until an event passes through the field enricher at which point the geoevent definition will be created.</p>
				</div>
			</aside>
			Connect the Field Mapper to the Field Calculator processor created in step 9.
			<li>Drag the AirC2-UpdateFeatures-ACOStatus output onto the GeoEvent Service Designer and connect the Map Status and Name Field Mapper processor to it.</li>
			<li>Create a new GeoTag Processor. Use the table below to configure the processor. Once configured connect the processor to the AirC2-tcp-text-in-civilian-air-tracks input.</li>
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
						<td>GeoTag Air Control Means on Aircraft Exit</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>GeoTagger</td>
					</tr>
					<tr>
						<td>GeoFence</td>
						<td>AirspaceControlMeasures/*</td>
					</tr>
					<tr>
						<td>Spatial Operator</td>
						<td>Exit Any</td>
					</tr>
					<tr>
						<td>Geometry Field Name</td>
						<td>GEOMETRY</td>
					</tr>
					<tr>
						<td>Geotag Field Name</td>
						<td>GeoTags</td>
					</tr>
					<tr>
						<td>GeoTag Format</td>
						<td>Delimited Value</td>
					</tr>
					<tr>
						<td>Include GeoFence Category in GeoTag</td>
						<td>No</td>
					</tr>
					<tr>
						<td>New GeoEvent Definition Name</td>
						<td>AirC2-CivilianAirTrack_OutsideACM</td>
					</tr>
				</tbody>
			</table>
			<li>Create a new filter called Filter Out Non ACM Tagged Records. Set the filter's query to the following:</li>
			<ul>
				<li>NOT GeoTags = ""</li>
			</ul>
			<br/>
			This will filter out events that do not honor the GeoTagger's query created in step 12. Connect the filter to the GeoTagger created in the last step.
			<li>Create a Field Enricher processor. This processor will enrich the incoming event with fields from any ACO in which it has exited (see GeoTagger in step 12). The field added to the incoming event is status. Use the following table to check Field Enricher Processor</li>
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
						<td>Add Status</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Enricher (Feature Service)</td>
					</tr>
					<tr>
						<td>Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service</td>
						<td>AirC2_ACO_Status (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>Airspace Control Means - Status (0)</td>
					</tr>
					<tr>
						<td>Feature Layer Join Field</td>
						<td>name</td>
					</tr>
					<tr>
						<td>Target Fields</td>
						<td>New Fields</td>
					</tr>
					<tr>
						<td>Enrichment Fields</td>
						<td>status</td>
					</tr>
					<tr>
						<td>Field Tags</td>
						<td></td>
					</tr>
					<tr>
						<td>New GeoEvent Definition Name</td>
						<td>AirC2-CivilianAirTrack_OutsideACM_Enriched</td>
					</tr>
					<tr>
						<td>GeoEvent Join Field</td>
						<td>GeoTags</td>
					</tr>
					<tr>
						<td>Cache Refresh Time Interval</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Maximum Cache Size</td>
						<td>1000</td>
					</tr>
				</tbody>
			</table>
			Connect the Field Enricher to the Field Splitter created in step 13.
			<li>Create a Field Calculator processor to modify the Status of the ACO. Use the following table to configure the processor:</li>
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
						<td>Calculate Status (Inactive)</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Calculator</td>
					</tr>
					<tr>
						<td>Expression</td>
						<td>'INACTIVE'</td>
					</tr>
					<tr>
						<td>Target Field</td>
						<td>Existing Field</td>
					</tr>
					<tr>
						<td>Existing Field Name</td>
						<td>status</td>
					</tr>
				</tbody>
			</table>
			Connect the Field Calculator to the Field Enricher created in step 14.
			<li>Create a Field Mapper processor. This mapping will map the status and name of the ACO to the appropriate fields.  Use the following tables to configure the processor.</li>
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
						<td>Map Name and Status</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Mapper</td>
					</tr>
					<tr>
						<td>Source GeoEvent Definition</td>
						<td>AirC2-CivilianAirTrack_OutsideACM_Enriched</td>
					</tr>
					<tr>
						<td>Target GeoEvent Definition</td>
						<td>AirC2-Update_ACO_Status</td>
					</tr>
				</tbody>
			</table>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Source Field</td>
						<td>Target Field</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>GeoTags</td>
						<td>name</td>
					</tr>
					<tr>
						<td>status</td>
						<td>status</td>
					</tr>
				</tbody>
			</table>
			<aside class="note">    
				<h5 class="icon-notebook">Note</h5>
				<div class="note-content">
					<p>The first time setting up the service the input definition created when the event passes through the Add Status Field Enricher will not be present. Run the simulation until an event passes through the field enricher at which point the geoevent definition will be created.</p>
				</div>
			</aside>
			Connect the Field Mapper to the Field Calculator processor created in step 15.
			<li>Connect the Field mapper created in the previous step to the AirC2-UpdateFeatures-ACOStatus output connector.
			<li>Create GeoTagger Processor. This processor will test whether an event is inside a target threat area. Use the following table to configure the GeoTagger:</li>
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
						<td>GeoTag Range Dome</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>GeoTagger</td>
					</tr>
					<tr>
						<td>GeoFence</td>
						<td>RangeDomes/*</td>
					</tr>
					<tr>
						<td>Spatial Operator</td>
						<td>Inside Any</td>
					</tr>
					<tr>
						<td>Geometry Field Name</td>
						<td>GEOMETRY</td>
					</tr>
					<tr>
						<td>Geotag Field Name</td>
						<td>RangeDomeName</td>
					</tr>
					<tr>
						<td>GeoTag Format</td>
						<td>Delimited Value</td>
					</tr>
					<tr>
						<td>Include GeoFence Category in GeoTag</td>
						<td>No</td>
					</tr>
					<tr>
						<td>New GeoEvent Definition Name</td>
						<td>AirC2-CivilianAirTrack_InsideThreatDome</td>
					</tr>
				</tbody>
			</table>
			Connect the geoTagger to the AirC2-tcp-text-in-civilian-airtracks input connector.
			<li>Create a new Filter called 'Filter Out Non Range Tagged Records'. This filter will filter out any events that have not entered a Range Dome geoFence. Use the following query to configure the filter:</li>
			<ul>
				<li>NOT RangeDomeName = ""</li>
			</ul>
			<br/>
			<li>Create a new Field Enricher processor. This processor will add the callsign and distance fields from a range dome to the event. use the table below to configure the Field Enricher:</li>
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
						<td>Add Distance and Callsign</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Enricher (Feature Service)</td>
					</tr>
					<tr>
						<td>Server Connection</td>
						<td>AirC2 AGS Server</td>
					</tr>
					<tr>
						<td>Folder</td>
						<td>AirC2</td>
					</tr>
					<tr>
						<td>Service</td>
						<td>AirC2_TargetThreatAreas (FeatureServer)</td>
					</tr>
					<tr>
						<td>Layer</td>
						<td>Threat position Inner (m) (0)</td>
					</tr>
					<tr>
						<td>Feature Layer Join Field</td>
						<td>gtgt_name</td>
					</tr>
					<tr>
						<td>Target Fields</td>
						<td>New Fields</td>
					</tr>
					<tr>
						<td>Enrichment Fields</td>
						<td>distance, ac_callsign</td>
					</tr>
					<tr>
						<td>Field Tags</td>
						<td></td>
					</tr>
					<tr>
						<td>New GeoEvent Definition Name</td>
						<td>AirC2-CivilianAirTrack_InsideThreatDome_Enriched</td>
					</tr>
					<tr>
						<td>GeoEvent Join Field</td>
						<td>RangeDomeName</td>
					</tr>
					<tr>
						<td>Cache Refresh Time Interval</td>
						<td>1</td>
					</tr>
					<tr>
						<td>Maximum Cache Size</td>
						<td>1000</td>
					</tr>
				</tbody>
			</table>
			Connect the Field Enricher processor to the filter created in step 19.
			<li>Create a new Filter called 'Within Threat Range'. This filter will determine whether an event is within the vertical extent of the rangedome. Use the following query to configure the filter:</li>
			<ul>
				<li>alt <= ${distance}</li>
			</ul>
			<br/>
			Connect the filter to the Field Enricher created in step 20.
			<li>Create a new Field Mapper Processor. Use the following tables to configure the Field Mapper:</li>
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
						<td>Map Fields to Output</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Mapper</td>
					</tr>
					<tr>
						<td>Source GeoEvent Definition</td>
						<td>AirC2-CivilianAirTrack_InsideThreatDome_Enriched</td>
					</tr>
					<tr>
						<td>Target GeoEvent Definition</td>
						<td>AirC2-Update_AirspaceAlert</td>
					</tr>
				</tbody>
			</table>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Source Field</td>
						<td>Target Field</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>id</td>
						<td>callsign</td>
					</tr>
					<tr>
						<td>lat</td>
						<td>lat</td>
					</tr>
					<tr>
						<td>lon</td>
						<td>long</td>
					</tr>
					<tr>
						<td>alt</td>
						<td>alt</td>
					</tr>
					<tr>
						<td>$RECEIVED_TIME</td>
						<td>alerttime</td>
					</tr>
					<tr>
						<td>geometry</td>
						<td>geometry</td>
					</tr>
				</tbody>
			</table>
			<aside class="note">    
				<h5 class="icon-notebook">Note</h5>
				<div class="note-content">
					<p>The first time setting up the service the input definition created when the event passes through the Add Distance and Callsign Field Enricher will not be present. Run the simulation until an event passes through the field enricher at which point the geoevent definition will be created.</p>
				</div>
			</aside>
			Connect the Field Mapper to the Filter processor created in step 21.
			<li>Drag the AirC2-UpdateFeatures-TargetThreatAlerts output into the GeoEvent service designer. Connect the Field mapper created in step 22 to the output</li>
			<li>Create a new Field Mapper Processor. Use the following tables to configure the FieldMapper</li>
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
						<td>Map Alerts</td>
					</tr>
					<tr>
						<td>Processor</td>
						<td>Field Mapper</td>
					</tr>
					<tr>
						<td>Source GeoEvent Definition</td>
						<td>AirC2-CivilianAirTrack_InsideACM_Enriched</td>
					</tr>
					<tr>
						<td>Target GeoEvent Definition</td>
						<td>AirC2-UpdateAirspaceAlert</td>
					</tr>
				</tbody>
			</table>
			<table class="bordered stripe lined-columns lined-rows">
				<thead>
					<tr>
						<td>Source Field</td>
						<td>Target Field</td>
					</tr>
				</thead>
				<tbody>
					<tr>
						<td>id</td>
						<td>callsign</td>
					</tr>
					<tr>
						<td>lat</td>
						<td>lat</td>
					</tr>
					<tr>
						<td>lon</td>
						<td>long</td>
					</tr>
					<tr>
						<td>alt</td>
						<td>alt</td>
					</tr>
					<tr>
						<td>$RECEIVED_TIME</td>
						<td>alerttime</td>
					</tr>
					<tr>
						<td>geometry</td>
						<td>geometry</td>
					</tr>
				</tbody>
			</table>
			<aside class="note">    
				<h5 class="icon-notebook">Note</h5>
				<div class="note-content">
					<p>The first time setting up the service the input definition created when the event passes through the Add Min Max Heights Field Enricher will not be present. Run the simulation until an event passes through the field enricher at which point the geoevent definition will be created.</p>
				</div>
			</aside>
			Connect the Field Mapper to the Field Calculator processor, Calculate Status, created in step 9.
			<li>Drag the AirC2-UpdateFeatureService-AirspaceControlMeasureAlert output service into the GeoEvent Service Designer. Connect the output to the Field mapper created in step 24.</li>
		</ol>
