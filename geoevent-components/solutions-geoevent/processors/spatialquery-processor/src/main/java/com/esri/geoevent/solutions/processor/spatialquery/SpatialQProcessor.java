package com.esri.geoevent.solutions.processor.spatialquery;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.LinearUnit;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.Polyline;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.geometry.Unit;
import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldCardinality;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldGroup;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.core.http.GeoEventHttpClient;
import com.esri.ges.core.http.GeoEventHttpClientService;
import com.esri.ges.core.http.KeyValue;
import com.esri.ges.core.property.Property;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnection;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnection.ConnectionType;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerType;
//import com.esri.ges.datastore.agsconnection.DefaultAGOLConnection;
import com.esri.ges.manager.datastore.agsconnection.Layer;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.EventDestination;
import com.esri.ges.messaging.EventUpdatable;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.GeoEventProducer;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.ges.util.Validator;

public class SpatialQProcessor extends GeoEventProcessorBase implements
		GeoEventProducer, EventUpdatable {
	private static final Log LOG = LogFactory.getLog(SpatialQProcessor.class);
	private Tokenizer tokenizer = new Tokenizer();
	private Map<String, String> eventTokenMap = new HashMap<String, String>();
	public GeoEventDefinitionManager manager;
	public ArcGISServerConnectionManager connectionManager;

	private EventDestination destination;

	private SpatialReference srIn;
	private SpatialReference srBuffer;
	private SpatialReference srOut;
	private double radius;
	private String units;
	private int inwkid;
	private int outwkid;
	private int bufferwkid;
	private String geoSrc;
	private String eventfld;
	private String file;
	private String outDefName;
	private String connName;
	private ArcGISServerConnection conn;
	private String folder;
	private String service;
	private String lyrName;
	private Layer layer;
	private String layerId;
	private String field;
	private String endpoint=null;
	private Boolean calcDist;
	private String wc;
	private com.esri.core.geometry.Geometry inGeometry;
	private String ownerId;
	private static final BundleLogger LOGGER = BundleLoggerFactory
			.getLogger(SpatialQProcessor.class);

	private Messaging messaging;
	private GeoEventCreator geoEventCreator;
	private GeoEventProducer geoEventProducer;
	private String token = null;
	private ConnectionType connectionType;
	//private DefaultAGOLConnection agolconn;
	public GeoEventHttpClientService httpClientService;
	private boolean useReferer = true;
	private String webTierUserName;
	private String webTierEncryptedPassword;
	private String userName;
	private String password;
	private String agolpassword;
	private String agoluser;
	private final int defaultTimeout = 30000;
	private ObjectMapper mapper = new ObjectMapper();
	protected SpatialQProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);
		LOGGER.info(toString());
	}

	@Override
	public void setId(String id) {
		super.setId(id);
		geoEventProducer = messaging
				.createGeoEventProducer(new EventDestination(id + ":event"));
	}

	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {
		try {
			if(!ge.getGeoEventDefinition().getTagNames().contains("GEOMETRY"))
			{
				return null;
			}
			srIn = ge.getGeometry().getSpatialReference();
			inwkid=srIn.getID();
			ownerId = ge.getGeoEventDefinition().getOwner();
			List<FieldDefinition> fldDefs = ge.getGeoEventDefinition()
					.getFieldDefinitions();
			for (FieldDefinition fd : fldDefs) {
				if (fd.getType() != FieldType.Geometry
						&& fd.getType() != FieldType.Group) {
					String n = fd.getName();
					String tk = tokenizer.tokenize("geoevent." + n);
					eventTokenMap.put(tk, n);
				}

			}
			ArrayList<Object> queries = CreateQueries(ge);

			MapGeometry geo = ge.getGeometry();
			MapGeometry inGeo = null;
			if (geoSrc.equals("Buffer")) {
				inGeometry = constructGeometry(geo);
				Unit u = queryUnit(units);
				inGeo = constructBuffer(geo.getGeometry(), radius, u);
			} else if (geoSrc.equals("Event_Definition")) {
				String geostr = (String) ge.getField(eventfld);
				MapGeometry g = constructGeometryFromString(geostr);
				Geometry polyGeo = constructGeometry(g);
				inGeometry = polyGeo;
				com.esri.core.geometry.Geometry projGeo = GeometryEngine
						.project(polyGeo, srBuffer, srOut);
				inGeo = new MapGeometry(projGeo, srOut);
			} else {

				Geometry polyGeo = constructGeometry(geo);
				inGeometry = polyGeo;
				com.esri.core.geometry.Geometry projGeo = GeometryEngine
						.project(polyGeo, srBuffer, srOut);
				// String json = GeometryEngine.geometryToJson(srOut, projGeo);
				inGeo = new MapGeometry(projGeo, srOut);
			}
			Geometry newGeo = inGeo.getGeometry();
			String jsonGeo = GeometryEngine.geometryToJson(srOut.getID(),
					newGeo);
			String geotype = GeometryUtility
					.parseGeometryType(newGeo.getType());
			HashMap<String, Object> responseMap = ExecuteRestQueries(jsonGeo,
					geotype, queries);
			Set<String> keys = responseMap.keySet();
			Iterator<String> it = keys.iterator();

			String k = it.next();
			@SuppressWarnings("unchecked")
			HashMap<String, Object> response = (HashMap<String, Object>) responseMap
					.get(k);
			@SuppressWarnings("unchecked")
			Map<String, Object> fset = (HashMap<String, Object>) response
					.get("fset");
			@SuppressWarnings("unchecked")
			HashMap<String, Object> tokenmap = (HashMap<String, Object>) response
					.get("tokenmap");
			@SuppressWarnings("unchecked")
			List<Object> fieldlist = (List<Object>) fset.get("fields");

			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> features = (ArrayList<HashMap<String, Object>>) fset
					.get("features");
			List<FieldDefinition> qflddefs = GenerateNewFieldDefs(fieldlist,
					tokenmap);
			GeoEventDefinition geoDef = ge.getGeoEventDefinition();

			GeoEventDefinition edOut;
			// if ((edOut = manager.searchGeoEventDefinition(outDefName,
			// getId())) == null) {
			edOut = geoDef.augment(qflddefs);
			if (edOut.getFieldDefinition("TRACK_ID") == null) {
				List<FieldDefinition> flddefs = new ArrayList<FieldDefinition>();
				FieldDefinition trackdef = new DefaultFieldDefinition(
						"trackid", FieldType.String, "TRACK_ID");
				flddefs.add(trackdef);
				edOut = edOut.augment(flddefs);
			}
			edOut.setOwner(definition.getUri().toString());
			edOut.setName(outDefName);
			Collection<GeoEventDefinition>eventDefs = manager.searchGeoEventDefinitionByName(outDefName);
			Iterator<GeoEventDefinition>eventDefIt = eventDefs.iterator();
			while(eventDefIt.hasNext())
			{
				GeoEventDefinition currentDef = eventDefIt.next();
				manager.deleteGeoEventDefinition(currentDef.getGuid());
			}
			manager.addGeoEventDefinition(edOut);
			// }
			// GeoEvent geOut = null;
			try {
				GeoEventDefinition ged = getEventDefinition(edOut);
				// geOut = geoEventCreator.create(ged.getName(), ownerId);

				List<FieldGroup> featureFieldGroups = new ArrayList<FieldGroup>();

				for (int i = 0; i < features.size(); ++i) {
					HashMap<String, Object> f = features.get(i);

					if (f != null) {
						// FieldGroup fieldGroup =
						// geOut.createFieldGroup("Features");
						GeoEvent featureGE = createFeatureGeoEvent(f, tokenmap,
								edOut, ge, i + 1);

						// for (int j = 0; j < featureGE.getAllFields().length;
						// j++) {
						// fieldGroup.setField(j, featureGE.getField(j));
						// }
						// featureFieldGroups.add(fieldGroup);
						send(featureGE);
						// notifyObservers(featureGE);
					}
				}
				// geOut.setField("Features", featureFieldGroups);
			} catch (ConfigurationException e) {
				LOG.error(e);
				return null;
			} catch (GeoEventDefinitionManagerException e) {
				LOG.error(e);
				return null;
			} catch (MessagingException e) {
				LOG.error(e);
				return null;
			} catch (FieldException e) {
				LOG.error(e);
				return null;
			}
			// return geOut;
			// send(createFeatureGeoEvent(ge));
		} catch (MessagingException e) {
			LOGGER.error("EVENT_SENT_FAILURE", e);
		}
		return null;
	}

	private GeoEvent createFeatureGeoEvent(HashMap<String, Object> feature,
			HashMap<String, Object> tokenmap, GeoEventDefinition ged,
			GeoEvent inEvent, Integer id) throws Exception {
		GeoEvent geoEvent = null;
		if (geoEventCreator != null) {
			try {
				try {
					geoEvent = geoEventCreator.create(outDefName, definition
							.getUri().toString());

					@SuppressWarnings("unchecked")
					Map<String, Object> att = (Map<String, Object>) feature
							.get("attributes");
					@SuppressWarnings("unchecked")
					Map<String, Object> objGeo = (Map<String, Object>) feature
							.get("geometry");

					Set<String> fields = tokenmap.keySet();
					
					for (FieldDefinition fd : geoEvent.getGeoEventDefinition()
							.getFieldDefinitions()) {
						String name = fd.getName();
						String value = null;
						String src = "event";
						List<String> tags = fd.getTags();
						if (tags.contains("TRACK_ID"))
						{
							geoEvent.setField("TRACK_ID", inEvent.getField(name));
						}
						if(tags.contains("QUERY_ID"))
						{
							geoEvent.setField("QUERY_ID", id.toString());
						}
						if(tags.contains("QUERY_GEOMETRY"))
						{
							
							MapGeometry mapGeo = null;
							Geometry geo = generateGeoFromMap(objGeo);
							SpatialReference sr = SpatialReference
									.create(outwkid);
							mapGeo = new MapGeometry(geo, sr);
							geoEvent.setField("QUERY_GEOMETRY", mapGeo);
						}
						else if (fields.contains(name)) {
							Object tmpVal = att.get(name);
							if (tmpVal != null) {
								value = att.get(name).toString();
							} else {
								value = null;
							}
							src = "feature";
						} else {
							Object val = inEvent.getField(name);
							if (val == null) {
								continue;
							}
							geoEvent.setField(name, val);
						
						}
						
						if (src.equals("feature")) {
							if (value == null) {
								geoEvent.setField(name, null);
							} else {
								switch (fd.getType()) {
								case Boolean:
									geoEvent.setField(name,
											value.equals("true") ? true : false);
									break;
								case Date:
									if (src.equals("feature"))
										geoEvent.setField(name,
												new Date(Long.parseLong(value)));
									break;
								case Double:
									Double doubleValue = null;
									// if (!Validator.isEmpty(value)) {
									doubleValue = Double.parseDouble(value);
									// }
									geoEvent.setField(name, doubleValue);
									break;
								case Float:
									Float floatValue = null;
									if (!Validator.isEmpty(value)) {
										floatValue = Float.parseFloat(value);
									}
									geoEvent.setField(name, floatValue);
									break;
								case Integer:
									Integer intValue = null;
									// if (!Validator.isEmpty(value)) {
									intValue = Integer.parseInt(value);
									// }
									geoEvent.setField(name, intValue);
									break;
								case Long:
									Long longValue = null;
									// if (!Validator.isEmpty(value)) {
									longValue = Long.parseLong(value);
									// }
									geoEvent.setField(name, longValue);
									break;
								case Geometry:
									MapGeometry mapGeo = null;
									Geometry geo = generateGeoFromMap(objGeo);
									SpatialReference sr = SpatialReference
											.create(outwkid);
									mapGeo = new MapGeometry(geo, sr);
									geoEvent.setGeometry(mapGeo);
									break;
								default:
									geoEvent.setField(name, value);
									break;
								}
							}
						}
					}
					geoEvent.setProperty(GeoEventPropertyName.TYPE, "event");
					geoEvent.setProperty(GeoEventPropertyName.OWNER_ID, getId());
					geoEvent.setProperty(GeoEventPropertyName.OWNER_URI,
							definition.getUri());
					return geoEvent;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			} catch (Exception e) {
				geoEvent = null;
				LOG.error(e.getMessage());
			}
		}
		return geoEvent;
	}

	@Override
	public void send(GeoEvent geoEvent) throws MessagingException {
		if (geoEventProducer != null && geoEvent != null)
			geoEventProducer.send(geoEvent);
	}

	@Override
	public void afterPropertiesSet() {
		radius = (Double) properties.get("radius").getValue();
		units = properties.get("units").getValue().toString();
		outwkid = (Integer) properties.get("wkidout").getValue();
		bufferwkid = (Integer) properties.get("wkidbuffer").getValue();
		geoSrc = properties.get("geosrc").getValueAsString();
		eventfld = properties.get("geoeventdef").getValue().toString();
		outDefName = properties.get("gedname").getValueAsString();
		connName = properties.get("connection").getValueAsString();
		folder = properties.get("folder").getValueAsString();
		service = properties.get("service").getValueAsString();
		lyrName = properties.get("layer").getValueAsString();
		
		try {
			conn = connectionManager.getArcGISServerConnection(connName);
			//agolconn = (DefaultAGOLConnection)conn;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			ValidationException ve = new ValidationException(
					"Unable to make connection to ArcGIS Server");
			LOG.error(ve.getMessage());
			try {
				throw ve;
			} catch (ValidationException e1) {

				e1.printStackTrace();
			}
		}
		layer = conn.getLayer(folder, service, lyrName,
				ArcGISServerType.FeatureServer);
		layerId = ((Integer) layer.getId()).toString();
		if(!properties.get("endpoint").getValueAsString().isEmpty())
		{
			endpoint=properties.get("endpoint").getValueAsString();
		}
		if(!properties.get("token").getValueAsString().isEmpty())
		{
			token=properties.get("token").getValueAsString();
		}
		//connectionType = conn.getConnectionType();
		//try {
			//token = conn.getDecryptedToken();
		//} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
		field = properties.get("field").getValueAsString();
		wc = properties.get("wc").getValueAsString();
	}

	public void validate() throws ValidationException {
		if (radius <= 0) {
			ValidationException ve = new ValidationException(
					"Radius cannot be less than or equal to 0");
			LOG.error(ve.getMessage());
			throw ve;
		}
		try {
			srBuffer = SpatialReference.create(bufferwkid);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			ValidationException ve = new ValidationException("Invalid wkid");
			LOG.error(ve.getMessage());
			throw ve;
		}
		try {
			srOut = SpatialReference.create(outwkid);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			ValidationException ve = new ValidationException("Invalid wkid");
			LOG.error(ve.getMessage());
			throw ve;
		}
	}

	@Override
	public EventDestination getEventDestination() {
		return (geoEventProducer != null) ? geoEventProducer
				.getEventDestination() : null;
	}

	@Override
	public List<EventDestination> getEventDestinations() {
		return (geoEventProducer != null) ? Arrays.asList(geoEventProducer
				.getEventDestination()) : new ArrayList<EventDestination>();
	}

	@Override
	public void disconnect() {
		if (geoEventProducer != null)
			geoEventProducer.disconnect();
	}

	@Override
	public boolean isConnected() {
		return (geoEventProducer != null) ? geoEventProducer.isConnected()
				: false;
	}

	@Override
	public String getStatusDetails() {
		return (geoEventProducer != null) ? geoEventProducer.getStatusDetails()
				: "";
	}

	@Override
	public void setup() throws MessagingException {
		;
	}

	@Override
	public void init() throws MessagingException {
		;
	}

	@Override
	public void update(Observable o, Object arg) {
		;
	}

	public void setMessaging(Messaging messaging) {
		this.messaging = messaging;
		this.geoEventCreator = messaging.createGeoEventCreator();
	}

	public void setManager(GeoEventDefinitionManager m) {
		manager = m;
	}

	public void setConnectionManager(ArcGISServerConnectionManager cm) {
		connectionManager = cm;
	}
	
	public void setHttpClentService(GeoEventHttpClientService service)
	{
		httpClientService = service;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(definition.getName());
		sb.append("/");
		sb.append(definition.getVersion());
		sb.append("[");
		for (Property p : getProperties()) {
			sb.append(p.getDefinition().getPropertyName());
			sb.append(":");
			sb.append(p.getValue());
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}

	public ArrayList<Object> CreateQueries(GeoEvent ge) throws Exception {
		Set<String> eventTokens = eventTokenMap.keySet();
		Iterator<String> eventIt = eventTokens.iterator();
		while (eventIt.hasNext()) {
			String et = eventIt.next();
			String fn = eventTokenMap.get(et);
			String val = null;
			if (ge.getField(fn) != null) {
				val = ge.getField(fn).toString();
				wc = wc.replace(et, val);
			}
		}
		ArrayList<Object> queries = new ArrayList<Object>();
		URL url = conn.getUrl();
		String protocol = url.getProtocol();
		String host = url.getHost();
		Integer port = url.getPort();
		String path = url.getPath();
		String baseUrl = null;
		String curPath=null;
		if (endpoint != null)
		{
			curPath = endpoint;
		}
		else
		{
			baseUrl = protocol + "://" + host +":" + port.toString() + path + "rest/services";
			curPath = baseUrl + "/" + folder + "/" + service+ "/FeatureServer/" + layerId;
		}
		//String baseUrl = url.getProtocol() + "://" + url.getHost() + ":"
				//+ url.getPort() + url.getPath() + "rest/services/";
		if(connectionType == ConnectionType.AGOL)
		{
			
			//String agolUrl = DefaultAGOLConnection.ARCGIS_Dot_Com_URL;
			//token = agolconn.getToken();
		}
				
		String restpath = curPath + "/query?";
		HashMap<String, Object> query = new HashMap<String, Object>();
		HashMap<String, String> fieldMap = new HashMap<String, String>();

		String fldsString = field;
		String[] fieldArray = fldsString.split(",");
		for (String f : fieldArray) {
			String tk = tokenizer.tokenize(f);
			fieldMap.put(f, tk);
		}

		query.put("restpath", restpath);
		query.put("path", curPath);
		query.put("whereclause", wc);
		query.put("fields", fldsString);
		query.put("tokenMap", fieldMap);
		query.put("usingdist", calcDist);
		query.put("layer", layer.getName());
		UUID uid = UUID.randomUUID();
		query.put("id", uid);
		queries.add(query);
		return queries;
	}

	private Geometry constructGeometry(MapGeometry geo) throws Exception {
		try {

			Geometry geoIn = geo.getGeometry();
			return GeometryEngine.project(geoIn, srIn, srBuffer);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			throw (e);
		}
	}

	private Unit queryUnit(String units) {
		UnitConverter uc = new UnitConverter();
		String cn = uc.findConnonicalName(units);
		int unitout = uc.findWkid(cn);
		Unit u = LinearUnit.create(unitout);
		return u;
	}

	private MapGeometry constructBuffer(Geometry geo, double radius, Unit u)
			throws JsonParseException, IOException {
		Polygon buffer = GeometryEngine.buffer(inGeometry, srBuffer, radius, u);
		Geometry bufferout = GeometryEngine.project(buffer, srBuffer, srOut);
		MapGeometry mapGeo = new MapGeometry(bufferout, srOut);
		return mapGeo;
	}

	private MapGeometry constructGeometryFromString(String geoString) {
		String[] pairs = geoString.split(" ");

		Polygon polygon = new Polygon();
		Boolean firstit = true;
		for (String coords : pairs) {

			String[] tuple = coords.split(",");
			Double x = Double.parseDouble(tuple[0]);
			Double y = Double.parseDouble(tuple[1]);
			Point p = new Point(x, y);
			Double z = Double.NaN;
			if (tuple.length > 2) {
				z = Double.parseDouble(tuple[2]);
				p.setZ(z);
			}
			if (firstit) {
				polygon.startPath(p);
				firstit = false;
			} else {
				polygon.lineTo(p);
			}
		}
		polygon.closeAllPaths();
		MapGeometry mapgeo = new MapGeometry(polygon, srOut);
		return mapgeo;
	}

	private HashMap<String, Object> ExecuteRestQueries(String jsonGeometry,
			String geoType, ArrayList<Object> queries)
			throws UnsupportedEncodingException {
		String contentType = "application/json";
		//HttpClient httpclient = HttpClientBuilder.create().build();
		GeoEventHttpClient http = httpClientService.createNewClient();
		HashMap<String, Object> responseMap = new HashMap<String, Object>();
		for (int i = 0; i < queries.size(); ++i) {
			@SuppressWarnings("unchecked")
			HashMap<String, Object> query = (HashMap<String, Object>) queries
					.get(i);
			String path = (String) query.get("restpath");
			String wc = URLEncoder.encode((String) query.get("whereclause"),
					"UTF-8");
			String geo = URLEncoder.encode(jsonGeometry, "UTF-8");

			@SuppressWarnings("unchecked")
			HashMap<String, String> tokenMap = (HashMap<String, String>) query
					.get("tokenMap");
			String itemConfig = (String) query.get("itemconfig");
			
			String wc2 = (String) query.get("whereclause");
			
			ArrayList<NameValuePair> postparameters = new ArrayList<NameValuePair>();
			postparameters.add(new BasicNameValuePair("where",wc2));
			postparameters.add(new BasicNameValuePair("geometry", jsonGeometry));
			postparameters.add(new BasicNameValuePair("geometryType", geoType));
			postparameters.add(new BasicNameValuePair("outFields", "*"));
			postparameters.add(new BasicNameValuePair("returnGeometry", "true"));
			postparameters.add(new BasicNameValuePair("returnDistinctValues", "false"));
			postparameters.add(new BasicNameValuePair("returnIdsOnly", "false"));
			postparameters.add(new BasicNameValuePair("returnZ", "false"));
			postparameters.add(new BasicNameValuePair("returnM", "false"));
			postparameters.add(new BasicNameValuePair("f", "json"));
			
			
			/*String args = "where="
					+ wc
					+ "&objectIds=&time=&geometry="
					+ geo
					+ "&geometryType="
					+ geoType
					+ "&inSR=&spatialRel=esriSpatialRelIntersects&relationParam=&outFields=*"
					// + fields
					+ "&returnGeometry=true&maxAllowableOffset=&geometryPrecision=&outSR=&gdbVersion=&returnDistinctValues=false&returnIdsOnly=false&returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&f=json";*/
			if(token != null)
			{
				path += "token=" + token;
			}
			//String uri = path + args;
			String uri = path;
			try {
				HttpPost httppost = new HttpPost(uri);
				
				httppost.setEntity(new UrlEncodedFormEntity(postparameters, "UTF-8"));
				
				httppost.setHeader("Accept", contentType);
				CloseableHttpResponse response = http.execute(httppost, GeoEventHttpClient.DEFAULT_TIMEOUT);
				//HttpResponse response = httpclient.execute(httppost);

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					try {
						BufferedReader br = new BufferedReader(
								new InputStreamReader((instream)));
						String output = "";
						String ln;
						while ((ln = br.readLine()) != null) {
							output += ln;
						}
						Map<String, Object> map = new HashMap<String, Object>();
						ObjectMapper mapper = new ObjectMapper();
						map = mapper.readValue(output,
								new TypeReference<HashMap<String, Object>>() {
								});
						HashMap<String, Object> tuple = new HashMap<String, Object>();
						String lyr = (String) query.get("layer");
						String lyrheadercfg = (String) query
								.get("headerconfig");
						Boolean calcdist = (Boolean) query.get("usingdist");
						Boolean sortByDist = (Boolean) query.get("sortbydist");
						String distToken = (String) query.get("disttoken");
						String distUnits = (String) query.get("distunits");
						String id = query.get("id").toString();
						tuple.put("fset", map);
						tuple.put("tokenmap", tokenMap);
						tuple.put("config", itemConfig);
						tuple.put("layer", lyr);
						tuple.put("lyrheader", lyrheadercfg);
						tuple.put("sortbydist", sortByDist);
						tuple.put("calcdist", calcdist);
						tuple.put("distunits", distUnits);
						tuple.put("disttoken", distToken);
						responseMap.put(id, tuple);
					} catch (IOException ex) {
						// In case of an IOException the connection will be
						// released
						// back to the connection manager automatically
						LOG.error(ex);
						throw ex;
					} catch (RuntimeException ex) {
						// In case of an unexpected exception you may want to
						// abort
						// the HTTP request in order to shut down the underlying
						// connection immediately.
						LOG.error(ex);
						httppost.abort();
						throw ex;
					} catch (Exception ex) {

						LOG.error(ex);
						httppost.abort();
						throw ex;
					} finally {
						// Closing the input stream will trigger connection
						// release
						try {
							instream.close();
						} catch (Exception ignore) {
						}
					}
				}

			} catch (Exception ex) {
				LOG.error(ex);
				ex.printStackTrace();
			}

		}
		return responseMap;
	}

	private List<FieldDefinition> GenerateNewFieldDefs(List<Object> fieldlist,
			HashMap<String, Object> tokenmap) throws ConfigurationException {
		FieldType ft = null;
		String type = null;
		List<FieldDefinition> qflddefs = new ArrayList<FieldDefinition>();

		for (Object fldobj : fieldlist) {

			@SuppressWarnings("unchecked")
			Map<String, Object> fldmap = (Map<String, Object>) fldobj;
			String name = fldmap.get("name").toString();
			String tag=null;
			if (tokenmap.containsKey(name)) {
				type = fldmap.get("type").toString();
				if (type.equals("esriFieldOID")) {
					continue;
				} else if (type.equals("esriFieldTypeString")) {
					ft = FieldType.String;
				} else if (type.equals("esriFieldTypeInteger")) {
					ft = FieldType.Integer;
				} else if (type.equals("esriFieldTypeDouble")) {
					ft = FieldType.Double;
				} else if (type.equals("esriFieldTypeDate")) {
					ft = FieldType.Date;
				} else if (type.equals("esriFieldTypeBoolean")) {
					ft = FieldType.Boolean;
				} 
				FieldDefinition fd = new DefaultFieldDefinition(name, ft);
				qflddefs.add(fd);
			}
			
		}
		if(tokenmap.containsKey("GEOMETRY"))
		{
			ft = FieldType.Geometry;
			String name="QueriedGeometry";
			String tag="QUERY_GEOMETRY";
			FieldDefinition fd = new DefaultFieldDefinition(name, ft, tag);
			qflddefs.add(fd);
		}
		ft = FieldType.String;
		String name = "QueryId";
		String tag = "QUERY_ID";
		FieldDefinition fd = new DefaultFieldDefinition(name, ft, tag);
		qflddefs.add(fd);
		return qflddefs;
	}

	private GeoEventDefinition getEventDefinition(GeoEventDefinition subdef)
			throws ConfigurationException, GeoEventDefinitionManagerException {
		GeoEventDefinition ged = geoEventCreator.getGeoEventDefinitionManager()
				.searchGeoEventDefinition("spatial-query-features", ownerId);
		if (ged != null) {
			return ged;
		}
		ged = new DefaultGeoEventDefinition();
		ged.setName("spatial-query-features");
		ged.setOwner(ownerId);

		List<FieldDefinition> fieldDefinitions = new ArrayList<FieldDefinition>();
		FieldDefinition featureFD = new DefaultFieldDefinition("Features",
				FieldType.Group);
		List<FieldDefinition> featureFieldDefinitions = subdef
				.getFieldDefinitions();
		for (FieldDefinition child : featureFieldDefinitions) {
			featureFD.addChild(child);
		}
		featureFD.setCardinality(FieldCardinality.Many);
		fieldDefinitions.add(featureFD);

		ged.setFieldDefinitions(fieldDefinitions);
		geoEventCreator.getGeoEventDefinitionManager().addGeoEventDefinition(
				ged);
		return ged;
	}

	private Geometry generateGeoFromMap(Map<String, Object> objGeo) {
		Geometry geo = null;
		if (objGeo.containsKey("rings")) {
			@SuppressWarnings("unchecked")
			ArrayList<ArrayList<ArrayList<String>>> rings = (ArrayList<ArrayList<ArrayList<String>>>) objGeo
					.get("rings");
			geo = generatePolygon(rings);
		} else if (objGeo.containsKey("paths")) {
			@SuppressWarnings("unchecked")
			ArrayList<ArrayList<ArrayList<String>>> paths = (ArrayList<ArrayList<ArrayList<String>>>) objGeo
					.get("paths");
			geo = generatePolyLine(paths);
		} else if (objGeo.containsKey("points")) {

		} else {
			Double x = Double.valueOf(objGeo.get("x").toString());
			Double y = Double.valueOf(objGeo.get("y").toString());
			if (objGeo.size() > 2) {
				Double z = Double.valueOf(objGeo.get("z").toString());
				geo = generate3DPoint(x, y, z);
			} else {
				geo = generatePoint(x, y);
			}
		}
		return geo;
	}

	private Point generatePoint(Double x, Double y) {
		Point p = new Point(x, y);
		return p;
	}

	private Point generate3DPoint(Double x, Double y, Double z) {
		Point p = new Point(x, y, z);
		return p;
	}

	private Polyline generatePolyLine(
			ArrayList<ArrayList<ArrayList<String>>> paths) {
		Polyline polyln = new Polyline();
		for (ArrayList<ArrayList<String>> path : paths) {
			Boolean firstPt = true;
			for (ArrayList<String> strPt : path) {
				Point p = null;
				if (strPt.size() > 2) {
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
					Double z = Double.valueOf(strPt.get(2));
					p = generate3DPoint(x, y, z);
				} else {
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
					p = generatePoint(x, y);
				}
				if (firstPt) {
					polyln.startPath(p);
					firstPt = false;
				} else {
					polyln.lineTo(p);
				}
			}
		}
		return polyln;
	}

	private Polygon generatePolygon(
			ArrayList<ArrayList<ArrayList<String>>> paths) {
		Polygon polygon = new Polygon();
		for (ArrayList<ArrayList<String>> path : paths) {
			Boolean firstPt = true;
			for (ArrayList<String> strPt : path) {
				Point p = null;
				if (strPt.size() > 2) {
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
					Double z = Double.valueOf(strPt.get(2));
					p = generate3DPoint(x, y, z);
				} else {
					Double x = Double.valueOf(strPt.get(0));
					Double y = Double.valueOf(strPt.get(1));
					p = generatePoint(x, y);
				}
				if (firstPt) {
					polygon.startPath(p);
					firstPt = false;
				} else {
					polygon.lineTo(p);
				}
			}
		}
		polygon.closeAllPaths();
		return polygon;
	}
	
	/*private String getTokenFromService() throws MalformedURLException
	{
		try(GeoEventHttpClient http = getHttpClient())
		{
			String urlString = DefaultAGOLConnection.ARCGIS_Dot_Com_URL + "sharing/generateToken";
			URL url = new URL(urlString.replace("http://", "https://"));
			Collection<KeyValue> params = new ArrayList<KeyValue>();
			params.add(new KeyValue("f", "json"));
		     params.add(new KeyValue("username", userName));
		     params.add(new KeyValue("password", password));
		     params.add(new KeyValue("client", "referer"));
		     params.add(new KeyValue("referer", conn.getReferer()));
		     String jsonResponse = http.post(url,  params, defaultTimeout);
		     
		     JsonNode response = (jsonResponse != null) ? mapper.readTree(jsonResponse) : mapper.createObjectNode();
		     token = response.get("token").asText();
			return token;
			
		}
		catch(Throwable t)
		{
			throw new RuntimeException(t.getMessage(), t);
		}
		
	}
	
	private GeoEventHttpClient getHttpClient()
	{
		
		GeoEventHttpClient http = httpClientService.createNewClient();
		if(useReferer)
		{
			//http.setReferer(agolconn.getReferer());
		}
		Collection<Property> implProps = agolconn.getImplementationProperties();
		
		Iterator iterator = implProps.iterator();
		while (iterator.hasNext()) {
			Property property = (Property) iterator.next();
			if (property.getName().equals("webTierUserName")) {
				webTierUserName = property.getValue().toString();

			} else if (property.getName().equals("webTierEncryptedPassword")) {
				webTierEncryptedPassword = property.getValue().toString();
			}
			else if (property.getName().equals("agol.password"))
			{
				agolpassword = property.getValue().toString();
			}
			else if(property.getName().equals("agol.username"))
			{
				agoluser = property.getValue().toString();
			}
			if(webTierUserName != null && webTierEncryptedPassword != null)
			{
				int port = conn.getUrl().getPort();
				if(port == -1)
				{
					port = (conn.getUrl().getProtocol().equals("https")) ? 443 : 80;
				}
				try
				{
					password = cryptoService.decrypt(webTierEncryptedPassword);
					http.setUsernamePassword(webTierUserName, password, conn.getUrl().getHost(), port);
				}
				catch(Exception e)
				{
					
				}
			}
			if(agoluser!= null && agolpassword != null)
			{
				//int port = conn.getUrl().getPort();
				//if(port == -1)
				//{
					//port = (conn.getUrl().getProtocol().equals("https")) ? 443 : 80;
				//}
				try
				{
					password = cryptoService.decrypt(agolpassword);
					userName = agoluser;
					//http.setUsernamePassword(agoluser, password, conn.getUrl().getHost(), port);
				}
				catch(Exception e)
				{
					
				}
			}
		}
		// webtierusername = implProps.

		return http;
	}*/
	
}