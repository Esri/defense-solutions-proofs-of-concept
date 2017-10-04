<p>Within the AirC2 COP toolbox a script has been provided that aims to simplify the process for creating a daily operations brief. The script uses the AirC2_ATO_MISSION feature class to automatically generate a second feature class called AirC2_ATO_MISSION_BRIEFING that has the required fields to work with the <a href="http://storymaps.arcgis.com/en/app-list/map-tour/" target="_blank">Story Map Tour</a> template.</p>
<p>The script provides the option of adding a weather Image Service URL (see<a href="#" target="_blank"> Publish Weather Services</a>) that will be used to determine the effects of weather on each target. Currently, this uses two weather variables, Impact of Cloud Ceiling on target acquisition for fixed wing aircraft and Impact of temperature on Air Defense. The script is intended as a starting point and could be extended to include other variables as required.</p>     
<ol class="steps">
		<li>Open a Client Application such as ArcMap or ArcGIS Pro</li>
        <li>Run the <a href="/defense/help/air-c2-cop/workflows/update-briefs/" target="_blank">Update Mission Briefing Feature Class</a> script to populate the AirC2_ATO_MISSION_BRIEFING feature class.</li>
        <li>Add the AirC2_ATO_MISSION_BRIEFING feature class to an empty ArcMap document.</li>
        <li>Share the map as a web feature service (do this by ensuring you have Feature Access enabled under the service capabilities) called AirC2_ATO_MISSION_BRIEFING ensuring it is placed into the AirC2 folder.</li>
        <li>Within your portal create a web map and add in the AirC2_ATO_MISSION_BRIEFING feature service, do not worry about symbology as icons will automatically be created for each of the target locations within the application.</li>
        <li>Share the web map and choose the option to create a web app.</li>
        <li>Choose the Story Map Tour from the gallery of applications.</li>
        <li>Click Create App.</li>
        <li>Specify a title, tags, and a summary for the new web app and click Done.</li>
        <li>The story map will open in edit mode, modify the title, color scheme and logo as appropriate and click save.</li>       
        <li>The creation of the web app is a one time process when you need to update the content of the application with new missions just re-run the <a href="/defense/help/air-c2-cop/workflows/update-briefs/" target="_blank">Update Mission Briefing Feature Class</a> script.</li>
     </ol>	
