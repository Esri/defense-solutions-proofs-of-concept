<p>To configure your Portal a banner can be added to the top and a Group needs to be created to share your Application created with. This group can then be used to create a Gallery App so that your content can be shared with related details attached with the rest of your organization. </p>

<h2>Add Classification Banners to Portal</h2>
<p>To add classification banners to the portal, follow the <a href="http://server.arcgis.com/en/portal/latest/administer/windows/set-advanced-portal-options.htm" target="_blank">instructions</a> provided by the ArcGIS for Server team.</p>

<h2>Add Classification Banners to Web App</h2>
<p>To add classifications banner to a Web App created using ArcGIS Web AppBuilder undertake the follow the steps:</p>     
<ol class="steps">
        <li>In the root folder of the application open up index.html in a text editor.</li>
        <li>Scroll down until you find the main-page div:</li>   
          <pre>&lt;div id="main-page"&gt;</pre>
          <pre>   &lt;div id="jimu-layout-manager"&gt;&lt;/div&gt;</pre>
          <pre>&lt;/div&gt;</pre>
        <li>Above the main-page div add the following:</li>              
          <pre>&lt;div title="UNCLASSIFIED" style="height: 20px;width: 100%;background: #78D428;color: #000;text-align: center;font-weight: 700;border: 1px solid gray;padding: 0!important;overflow: hidden; text-overflow: ellipsis;position: fixed;top: 0px;"&gt;UNCLASSIFIED&lt;div&gt;</pre>
        <li>Below the main-page div add the following:</li>              
          <pre>&lt;div title="UNCLASSIFIED" style="height: 20px;width: 100%;background: #78D428;color: #000;text-align: center;font-weight: 700;border: 1px solid gray;padding: 0!important;overflow: hidden; text-overflow: ellipsis;position: fixed;bottom: 0px;"&gt;UNCLASSIFIED&lt;/div&gt;</pre>
        <li>It should now look like:</li> 
          <pre>&lt;div title="UNCLASSIFIED" style="height: 20px;width: 100%;background: #78D428;color: #000;text-align: center;font-weight: 700;border: 1px solid gray;padding: 0!important;overflow: hidden; text-overflow: ellipsis;position: fixed;top: 0px;"&gt;UNCLASSIFIED&lt;div&gt;</pre>
          <pre>&lt;div id="main-page"&gt;</pre>
          <pre>   &lt;div id="jimu-layout-manager"&gt;&lt;/div&gt;</pre>
          <pre>&lt;/div&gt;</pre>
          <pre>&lt;div title="UNCLASSIFIED" style="height: 20px;width: 100%;background: #78D428;color: #000;text-align: center;font-weight: 700;border: 1px solid gray;padding: 0!important;overflow: hidden; text-overflow: ellipsis;position: fixed;bottom: 0px;"&gt;UNCLASSIFIED&lt;/div&gt;</pre>
        <li>Save index.html</li>
        <li>In the same folder open config.json in a text editor.</li>
        <li>Change the map properties from those in the left column to those in the right:</li>
           <table>
             <tr>
              <td>
                <br />
                <pre>"map": {</pre>
                <pre>  "3D": true,</pre>   
                <pre>  "2D": false,</pre>
                <pre>  "position": {</pre>     
                <pre>    "left": 0,</pre>     
                <pre>    "top": 40,</pre>     
                <pre>    "right": 0,</pre>     
                <pre>    "bottom": 0</pre>
                <pre>},</pre>
              </td>
              <td>
                <br />
                <pre>"map": {</pre>
                <pre>  "3D": true,</pre>   
                <pre>  "2D": false,</pre>
                <pre>  "position": {</pre>     
                <pre>    "left": 0,</pre>     
                <pre>    "top": 60,</pre>     
                <pre>    "right": 0,</pre>     
                <pre>    "bottom": 20</pre>
                <pre>},</pre>
              </td>
            </tr>
          </table>
        <li>Change the HeaderController Widget properties from those in the left column to those in the right:</li>
        <table>
             <tr>
              <td>
                <br />
                <pre>"position": {</pre>     
                <pre>  "left": 0,</pre>     
                <pre>  "top": 0,</pre>     
                <pre>  "right": 0,</pre>     
                <pre>  "height": 40,</pre>
                <pre>  "paddingRight": 0,</pre>
                <pre>  "relativeTo":</pre>
                <pre>    "browser"</pre>
                <pre>},</pre>
              </td>
              <td>
                <br />
                <pre>"position": {</pre>     
                <pre>  "left": 0,</pre>     
                <pre>  "top": 20,</pre>     
                <pre>  "right": 0,</pre>     
                <pre>  "height": 40,</pre>
                <pre>  "paddingRight": 0,</pre>
                <pre>  "relativeTo":</pre>
                <pre>    "browser"</pre>
                <pre>},</pre>
              </td>
            </tr>
          </table>
        <li>Save config.json</li>
      </ol>
<h2>Creating and configuring a Portal Group</h2>
<p>To be able to share the Web Apps created a Portal Group needs to be created for them to be shared with.  The best <a href="http://server.arcgis.com/en/portal/latest/administer/windows/create-groups.htm" target="_blank">instructions</a> for this are provided by the ArcGIS for Server team. </p>     

<h2>Creating and configuring a Portal Gallery App</h2>
<p>To be able to share the Web Apps a gallery app needs to be created.  The best <a href="http://server.arcgis.com/en/portal/latest/use/create-gallery-apps.htm" target="_blank">instructions</a> for this are provided by the ArcGIS for Server team. </p>
<p>The Gallery can then be <a href="http://server.arcgis.com/en/portal/latest/administer/windows/configure-gallery.htm" target="_blank">configured</a> using the following instructions.</p>