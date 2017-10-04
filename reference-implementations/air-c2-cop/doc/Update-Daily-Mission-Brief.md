<p>To update the daily mission brief follow the steps below:</p>
<ol class="steps">
			<li>Open ArcCatalog.</li>
			<li>Open the Airspace Management Tools toolbox in tools folder.</li>
			<li>Double-click the Update Mission Briefing Feature Class script.</li>
			<li>Specify the following parameters:</li>
			<ul>
				<li>Target Workspace: The target workspace containing the AirC2_MISSION and AirC2_MISSION_BRIEFING feature classes.</li>
				<li>Image Folder URL: The URL to the folder storing your images. Images must be stored as .jpg and be named using the Ground Target ID for them to automatically be visible in the Story Map Tour Web Application i.e. https://airc2.domain.com/solutionsweb/resources/graphics</li>
				<li>Weather Service URL (Optional): REST Endpoint URL for your weather image service created during the <a href="/defense/help/air-c2-cop/get-started/publish-weather/" target="_blank">Publish Weather Services</a> section . i.e. https://airc2.domain.com/ags/rest/services/AirC2/AirC2WeatherImage/ImageServer</li>
			</ul>
               <li>Click OK to run the script.</li>
		</ol>	