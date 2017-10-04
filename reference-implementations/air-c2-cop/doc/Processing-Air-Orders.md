<h1>Process Air Orders</h1>
<p>To process an Airspace Control Order (ACO) or an Airspace Tasking Order (ATO), follow the steps below:</p>
<ol class="steps">
			<li>Open ArcCatalog.</li>
			<li>Open the Airspace Management Tools toolbox in tools folder.</li>
			<li>Double-click the Process Airspace Control Order or Process Airspace Tasking Order script.</li>
			<li>Specify the following parameters:</li>
			<ul>
				<li>Source File: The source ACO/ATO file to be processed (found in files/SampleACOandATO folder).</li>
				<li>Target Workspace: The target workspace containing the feature classes intended to receive the individual features.</li>
				<li>Log Level: (Optional) - select DEBUG for extended diagnostics.</li>
			</ul>
		</ol>		
<h1>Delete Air Orders</h1>
<p>To delete an Airspace Control Order (ACO) or an Airspace Tasking Order (ATO), follow the steps below:</p>
<ol class="steps">
			<li>Open ArcCatalog.</li>
			<li>Open the Airspace Management Tools toolbox in tools folder.</li>
			<li>Double-click the Delete Entries from ACO Record or Delete Entries from ATO Record.</li>
			<li>Specify the following parameters:</li>
			<ul>
				<li>Target Workspace: The target workspace containing the feature classes intended to receive the individual features.</li>
				<li>AMS Id: Enter a specific AMS ID to delete. If default value is left (%%) ALL ACOs/ATOs will be deleted.</li>
			</ul>
		</ol>