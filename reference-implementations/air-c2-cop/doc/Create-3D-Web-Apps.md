<p>This tutorial demonstrates how you can use HTML and the <a href="https://developers.arcgis.com/javascript/" target=_blank">ArcGIS API for JavaScript</a> to create a simple 3D application.</p>
<ol class="steps">
     <li>First, set up a basic HTML document similar to the following:</li>   
          <pre>&lt;!DOCTYPE html&gt;</pre>
          <pre>  &lt;html&gt;</pre>
          <pre>    &lt;head&gt;</pre>
          <pre>    &lt;meta charset="utf-8"&gt;</pre>
          <pre>    &lt;meta name="viewport" content="initial-scale=1, maximum-scale=1, user-scalable=no"&gt;</pre>
          <pre>    &lt;title&gt;Air Plan - Current Operations&lt;title&gt;</pre>
          <pre>  &lt;head&gt;</pre>
          <pre>&lt;html&gt;</pre>
     <li>Inside the &lt;head&gt; tags, reference the ArcGIS API for JavaScript using &lt;script&gt; and &lt;link&gt; tags (this can be a locally hosted version of the API or from Esri's CDN):</li>   
          <pre>&lt;link rel="stylesheet" href="https://js.arcgis.com/4.0/esri/css/main.css"&gt;</pre>
          <pre>&lt;script src="https://js.arcgis.com/4.0/"&gt;&lt;/script&gt;</pre>
     <li>Use a second  &lt;script&gt; tag to load specific modules from the API. Load the following modules using the syntax in the snippet below:</li>   
          <pre>require([</pre>
          <pre>  "esri/Map",</pre>
          <pre>  "esri/views/SceneView",</pre>
          <pre>  "esri/layers/SceneLayer",</pre>
          <pre>  "esri/layers/StreamLayer",</pre>
          <pre>  "esri/layers/TileLayer",</pre>
          <pre>  "esri/Basemap",</pre>
          <pre>  "esri/renderers/SimpleRenderer",</pre>
          <pre>  "esri/symbols/PointSymbol3D",</pre>
          <pre>  "esri/symbols/IconSymbol3DLayer",</pre>
          <pre>  "esri/symbols/ObjectSymbol3DLayer",</pre>
          <pre>  "esri/layers/support/LabelClass",</pre>
          <pre>  "esri/symbols/TextSymbol",</pre>
          <pre>  "dojo/dom",</pre>
          <pre>  "dojo/on",</pre>
          <pre>  "dojo/_base/Color",</pre>
          <pre>  "dojo/domReady!"</pre>
          <pre>  ], function(Map, SceneView, SceneLayer, StreamLayer, TileLayer, Basemap, SimpleRenderer, PointSymbol3D, IconSymbol3DLayer, ObjectSymbol3DLayer, LabelClass, TextSymbol, dom, on, Color)</pre>
          <pre>  {</pre>
          <pre>     // Code to create the map and view will go here</pre>
          <pre>  });</pre>
     <li>Replace the line // Code to create the map and view will go here, with the code snippet to create a Map:</li>   
          <pre>var map = new Map({</pre>
          <pre>  basemap: "streets",</pre>
          <pre>  ground: "world-elevation"</pre>
          <pre>});</pre>
     <li>Under the code to create the map add the ACO scene layer created in the <a href="/defense/help/air-c2-cop/get-started/publish-3d/" target="_blank">Publish 3D Services</a> section:</li>   
          <pre>    var ACOLayer = new SceneLayer({</pre>
          <pre>      url: 'https://airc2.esri.com/ags/rest/services/Hosted/ACO_3D/SceneServer/layers/0',</pre>
          <pre>      opacity: 0.5    </pre>
          <pre>    });</pre>
          <pre>    map.add(ACOLayer);</pre>
     <li>Create a label class that will be used to label the stream services:</li>   
          <pre>    var labelClass = new LabelClass({</pre>
          <pre>      labelExpressionInfo: { value: "{id}" },</pre>
          <pre>      symbol: new TextSymbol({</pre>
          <pre>      color: "black",</pre>
          <pre>      haloSize: 1,</pre>
          <pre>      haloColor: "white",</pre>
          <pre>      font: {  // autocast as esri/symbols/Font</pre>
          <pre>        size: 8,</pre>
          <pre>        family: "sans-serif",</pre>
          <pre>        weight: "bolder"</pre>
          <pre>        }</pre>
          <pre>      })</pre>
          <pre>    });</pre>          
     <li>Add your military and civilian stream services that were created in the configure GeoEvent Section. You will notice the code references a couple of icons (one for civilian aircraft and one for military aircraft) that you will need to host on your own server, these were created using the <a href="http://explorer.milsymb.net/#/home" target="_blank">Joint military symbology explorer</a>:</li>   
          <pre>    var milStreamLayer = new StreamLayer('https://your.server.com/ags/rest/services/StreamServiceOut-MilitaryFlights-Default/StreamServer', {</pre>
          <pre>      purgeOptions: {</pre>
          <pre>        displayCount: 1000,</pre>
          <pre>        age: 0.1</pre>
          <pre>      }</pre>
          <pre>    });</pre>
          <pre>    var milSymbol = new PointSymbol3D({</pre>
          <pre>      symbolLayers: [new IconSymbol3DLayer({</pre>
          <pre>      size: 32,  // points</pre>
          <pre>      resource: { href: "./10030100001101000000.png" },</pre>
          <pre>      })]</pre>
          <pre>    });</pre>
          <pre>    milSymbolRenderer = new SimpleRenderer({</pre>
          <pre>      symbol: milSymbol</pre>
          <pre>      });</pre>
          <pre>    milStreamLayer.renderer = milSymbolRenderer;</pre>
          <pre>    milStreamLayer.labelsVisible = true;</pre>
          <pre>    milStreamLayer.labelingInfo = [ labelClass ];</pre>
          <pre>    map.add(milStreamLayer);</pre>
          <pre>    var civStreamLayer = new StreamLayer('https://your.server.com/ags/rest/services/StreamServiceOut-CivilianFlights-Default/StreamServer', {</pre>
          <pre>      purgeOptions: {</pre>
          <pre>        displayCount: 1000,</pre>
          <pre>        age: 0.1</pre>
          <pre>      }    </pre>
          <pre>    });</pre>
          <pre>    var civSymbol = new PointSymbol3D({</pre>
          <pre>      symbolLayers: [new IconSymbol3DLayer({</pre>
          <pre>      size: 32,  // points</pre>
          <pre>      resource: { href: "./10040100001201000000.png" },</pre>
          <pre>      })]</pre>
          <pre>    });</pre>
          <pre>      civSymbolRenderer = new SimpleRenderer({</pre>
          <pre>      symbol: civSymbol</pre>
          <pre>      });</pre>
          <pre>    civStreamLayer.labelsVisible = true;</pre>
          <pre>    civStreamLayer.labelingInfo = [ labelClass ];</pre>
          <pre>    civStreamLayer.renderer = civSymbolRenderer;</pre>
          <pre>    map.add(civStreamLayer);</pre>
     <li>Create a Scene view:</li>
          <pre>    var view = new SceneView({</pre>
          <pre>      map: map,</pre>
          <pre>      container: "viewDiv",</pre>
          <pre>      camera: {</pre>
          <pre>        position: {</pre>
          <pre>          x: -124.521419, // lon</pre>
          <pre>          y: 35.034312,   // lat</pre>
          <pre>          z: 125000 // elevation in meters</pre>
          <pre>        },</pre>
          <pre>          tilt: 75,</pre>
          <pre>          heading:60</pre>
          <pre>      }</pre>
          <pre>    })  </pre>
          <pre>  });    </pre>
     <li>Now the JavaScript needed to create a map and a view is complete! The next step is to add the associated HTML for viewing the map. For this example, the HTML is very simple: add a &lt;body&gt; tag, which defines what is visible in the browser, and a single &lt;div&gt; element inside the body where the view will be created.:</li>
          <pre>&lt;body&gt;</pre>
          <pre>  &lt;div id="viewDiv" /&gt; </pre>
          <pre>&lt;/body&gt;</pre>
     <li>Style the content of the page using &lt;style&gt; tags inside the &lt;head&gt;. To make the map fill the browser window, add the following CSS inside the page's &lt;style&gt;</li>
          <pre>&lt;style&gt;</pre>
          <pre>  html,</pre>
          <pre>  body,</pre>
          <pre>  #viewDiv {</pre>
          <pre>    padding: 0;</pre>
          <pre>    margin: 0px;</pre>
          <pre>    height: 100%;</pre>
          <pre>    width: 100%;</pre>
          <pre>  } </pre>
          <pre>&lt;/style&gt;</pre>
     <li>The final HTML code should look like the following:</li>
          <pre>&lt;!DOCTYPE html&gt;</pre>
          <pre>&lt;html&gt;</pre>
          <pre>&lt;head&gt;</pre>
          <pre>&lt;meta charset="utf-8"&gt;</pre>
          <pre>&lt;meta name="viewport" content="initial-scale=1,maximum-scale=1,user-scalable=no"&gt;</pre>
          <pre>&lt;title&gt;Air Plan - Current Operations&lt;/title&gt;</pre>
          <pre>&lt;style&gt;</pre>
          <pre>  html,</pre>
          <pre>  body,</pre>
          <pre>  #viewDiv {</pre>
          <pre>    padding: 0;</pre>
          <pre>    margin: 0px;</pre>
          <pre>    height: 100%;</pre>
          <pre>    width: 100%;</pre>
          <pre>  } </pre>
          <pre>&lt;/style&gt;</pre>
          <pre>&lt;link rel="stylesheet" href="https://js.arcgis.com/4.0/esri/css/main.css"&gt;</pre>
          <pre>&lt;script src="https://js.arcgis.com/4.0/"&gt;&lt;/script&gt;</pre>
          <pre>&lt;script&gt;</pre>
          <pre>  require([</pre>
          <pre>    "esri/Map",</pre>
          <pre>    "esri/views/SceneView",</pre>
          <pre>    "esri/layers/SceneLayer",</pre>
          <pre>    "esri/layers/StreamLayer",</pre>
          <pre>    "esri/Basemap",</pre>
          <pre>    "esri/renderers/SimpleRenderer",</pre>
          <pre>    "esri/symbols/PointSymbol3D",</pre>
          <pre>    "esri/symbols/IconSymbol3DLayer",</pre>
          <pre>    "esri/symbols/ObjectSymbol3DLayer",</pre>
          <pre>    "esri/layers/support/LabelClass",</pre>
          <pre>    "esri/symbols/TextSymbol",</pre>
          <pre>    "dojo/dom",</pre>
          <pre>    "dojo/on",</pre>
          <pre>    "dojo/_base/Color",</pre>
          <pre>    "dojo/domReady!"</pre>
          <pre>  ], function(</pre>
          <pre>    Map, SceneView, SceneLayer, StreamLayer, Basemap, SimpleRenderer, PointSymbol3D, IconSymbol3DLayer, ObjectSymbol3DLayer, LabelClass, TextSymbol, dom, on, Color</pre>
          <pre>  ) {   </pre>
          <pre></pre>
          <pre>    var map = new Map({</pre>
          <pre>       basemap: "streets",</pre>
          <pre>       ground: "world-elevation"</pre>
          <pre>    });</pre>
          <pre>    var ACOLayer = new SceneLayer({</pre>
          <pre>      url: 'https://airc2.esri.com/ags/rest/services/Hosted/ACO_3D/SceneServer/layers/0',</pre>
          <pre>      opacity: 0.5    </pre>
          <pre>    });</pre>
          <pre>    map.add(ACOLayer);</pre>
          <pre>    var labelClass = new LabelClass({</pre>
          <pre>      labelExpressionInfo: { value: "{id}" },</pre>
          <pre>      symbol: new TextSymbol({</pre>
          <pre>      color: "black",</pre>
          <pre>      haloSize: 1,</pre>
          <pre>      haloColor: "white",</pre>
          <pre>      font: {  // autocast as esri/symbols/Font</pre>
          <pre>        size: 8,</pre>
          <pre>        family: "sans-serif",</pre>
          <pre>        weight: "bolder"</pre>
          <pre>        }</pre>
          <pre>      })</pre>
          <pre>    });</pre>
          <pre>    var milStreamLayer = new StreamLayer('https://airc2.esri.com/ags/rest/services/StreamServiceOut-MilitaryFlights-Default/StreamServer', {</pre>
          <pre>      purgeOptions: {</pre>
          <pre>        displayCount: 1000,</pre>
          <pre>        age: 0.1</pre>
          <pre>      }</pre>
          <pre>    });</pre>
          <pre>    var milSymbol = new PointSymbol3D({</pre>
          <pre>      symbolLayers: [new IconSymbol3DLayer({</pre>
          <pre>      size: 32,  // points</pre>
          <pre>      resource: { href: "./10030100001101000000.png" },</pre>
          <pre>      })]</pre>
          <pre>    });</pre>
          <pre>    milSymbolRenderer = new SimpleRenderer({</pre>
          <pre>      symbol: milSymbol</pre>
          <pre>      });</pre>
          <pre>    milStreamLayer.renderer = milSymbolRenderer;</pre>
          <pre>    milStreamLayer.labelsVisible = true;</pre>
          <pre>    milStreamLayer.labelingInfo = [ labelClass ];</pre>
          <pre>    map.add(milStreamLayer);</pre>
          <pre>    var civStreamLayer = new StreamLayer('https://airc2.esri.com/ags/rest/services/StreamServiceOut-CivilianFlights-Default/StreamServer', {</pre>
          <pre>      purgeOptions: {</pre>
          <pre>        displayCount: 1000,</pre>
          <pre>        age: 0.1</pre>
          <pre>      }    </pre>
          <pre>    });</pre>
          <pre>    var civSymbol = new PointSymbol3D({</pre>
          <pre>      symbolLayers: [new IconSymbol3DLayer({</pre>
          <pre>      size: 32,  // points</pre>
          <pre>      resource: { href: "./10040100001201000000.png" },</pre>
          <pre>      })]</pre>
          <pre>    });</pre>
          <pre>      civSymbolRenderer = new SimpleRenderer({</pre>
          <pre>      symbol: civSymbol</pre>
          <pre>      });</pre>
          <pre>    civStreamLayer.labelsVisible = true;</pre>
          <pre>    civStreamLayer.labelingInfo = [ labelClass ];</pre>
          <pre>    civStreamLayer.renderer = civSymbolRenderer;</pre>
          <pre>    map.add(civStreamLayer);</pre>
          <pre>    var view = new SceneView({</pre>
          <pre>      map: map,</pre>
          <pre>      container: "viewDiv",</pre>
          <pre>      camera: {</pre>
          <pre>        position: {</pre>
          <pre>          x: -124.521419, // lon</pre>
          <pre>          y: 35.034312,   // lat</pre>
          <pre>          z: 125000 // elevation in meters</pre>
          <pre>        },</pre>
          <pre>          tilt: 75,</pre>
          <pre>          heading:60</pre>
          <pre>      }</pre>
          <pre>    })  </pre>
          <pre>  });    </pre>
          <pre>&lt;/script&gt;</pre>
          <pre>&lt;/head&gt;</pre>
          <pre>&lt;body&gt;</pre>
          <pre>  &lt;div id="viewDiv" /&gt; </pre>
          <pre>&lt;/body&gt;</pre>
          <pre>&lt;/html&gt;</pre>
     <li>Host the html page and associated images (icons) on your web server.</li>
     </ol>           
