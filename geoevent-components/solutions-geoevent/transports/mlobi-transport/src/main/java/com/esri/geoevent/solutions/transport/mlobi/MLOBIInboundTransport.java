package com.esri.geoevent.solutions.transport.mlobi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.core.http.GeoEventHttpClient;
import com.esri.ges.core.http.GeoEventHttpClientService;
import com.esri.ges.core.http.KeyValue;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.InboundTransportBase;
import com.esri.ges.transport.RestInboundTransportProvider;
import com.esri.ges.transport.TransportDefinition;
import com.esri.ges.transport.http.HttpTransportService;
import com.esri.ges.transport.http.HttpUtil;
import com.esri.ges.transport.http.LastModifiedToDateFormatter;
import com.esri.ges.util.DateUtil;

public class MLOBIInboundTransport extends InboundTransportBase implements RestInboundTransportProvider {

	private String											acceptableMimeTypes_server;
	protected String										acceptableMimeTypes_client;
	private String											eom = "";
	protected GeoEventHttpClientService						httpClientService;
	//private int 											httpTimeoutValue;
	private String 											host;
	private String 											user;
	private String 											pw;
	private String											featureService;
	private String 											token;
	private int												refreshInterval	= 10;
	private String											queryDefinition;
	private WorkerThread									runThread;
	private Charset											charset;
	private String											oidFieldName;
	//private String											newFeaturesOption;
	private String											newFeaturesTimeFieldName;
	private long											lastTimestamp;
	private String											layerDescriptionForLogs;
	private String 											layerIndex;
	private Boolean											getNewFeaturesOnly;
	private final ObjectMapper							mapper = new ObjectMapper();
	private JsonNode 									features;
	private static final BundleLogger						LOGGER= BundleLoggerFactory.getLogger(MLOBIInboundTransport.class);
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	
	private class WorkerThread extends Thread
	{
		private volatile boolean	running	= true;

		private void dismiss()
		{
			running = false;
		}

		@Override
		public void run()
		{
			while (running)
			{
				try
				{
					getIncomingData();
				}
				catch (Throwable t)
				{
					LOGGER.error("FS_RUN_ERROR", refreshInterval, t.getMessage());
					LOGGER.info(t.getMessage(), t);
				}
				try
				{
					sleep(refreshInterval * 1000);
				}
				catch (InterruptedException ex)
				{
					;
				}
			}
			// setRunningState(RunningState.STOPPED);
		}
	}
	
	public MLOBIInboundTransport(TransportDefinition definition, GeoEventHttpClientService service)
			throws ComponentException {
		super(definition);
		this.httpClientService = service;
		charset = StandardCharsets.UTF_8;
	}

	@Override
	public String getAcceptableMimeTypesServerMode() {
		return acceptableMimeTypes_server;
	}

	@Override
	public String getEOMString() {
		return eom;
	}

	@Override
	public boolean isServerMode() {
		String m = getProperty(HttpTransportService.MODE_PROPERTY).getValueAsString();
		if (m.equals("SERVER"))
			return true;
		else
			return false;
	}
	
	@Override
	public boolean isClusterable()
	{
		return false;
	}

	@Override
	public void start() throws RunningException {
		switch (getRunningState())
		{
			case STARTING:
			case STARTED:
			case ERROR:
				return;
			default:
				// fall-through
				break;
		}

		setRunningState(RunningState.STARTING);
		setup();
		try {
			connectOBI();
			runThread = new WorkerThread();
			setRunningState(RunningState.STARTED);
			runThread.setName("InboundMLOBIFeatureServiceWorkerThread-"+ featureService + "-" + layerIndex);
			runThread.start();
			setErrorMessage(null);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
	}
	
	@Override
	public synchronized void stop()
	{
		stop(true);
	}
	
	private void getIncomingData()
	{
		// String test = "";
		String whereClause = "";
		try
		{
				whereClause = queryDefinition;
				if (getNewFeaturesOnly)
				{
					
						Date cachedDate = new Date(lastTimestamp);
						String format ="yyyy-MM-dd'T'HH:mm:ss.SSS";
						TimeZone tz = TimeZone.getTimeZone("UTC");
						SimpleDateFormat dateFormat = new SimpleDateFormat(format);
						dateFormat.setTimeZone(tz);
						String dateStr = dateFormat.format(cachedDate);
						dateStr = dateStr.substring(0, dateStr.length()-3);
						dateStr += "999999";
						//String dateStr = DateUtil.format(cachedDate, format);
						if (queryDefinition.length() > 0)
						{
							// whereClause = queryDefinition + " and " + newFeaturesTimeFieldName + " > " + agscon.getDateTimeQueryOperand(dateStr);
							whereClause = queryDefinition + " and " + "object-activity-date GT " + dateStr;
						}
						else
						{
							whereClause = "object-activity-date GT " + dateStr;
						}
					
				}
				
				String clientUrl = host + "/rest/services/" + featureService + 	"/FeatureServer/" + layerIndex + "/query";	
				URL url = new URL(clientUrl);
				Collection<KeyValue> params = new ArrayList<KeyValue>();
				params.add(new KeyValue("f", "json"));
				if(!whereClause.isEmpty())
					params.add(new KeyValue("where", whereClause));
				String jsonString = executeGetAndGetReply(url, params);
				JsonNode jsonReply = mapper.readTree(jsonString);
				features = jsonReply.get("features");
				if(features.size() > 0 && getNewFeaturesOnly)
					updateLastTimestamp(jsonReply);
				for(int i = 0; i < features.size(); ++i )
				{
					JsonNode node = features.get(i);
					
					ByteBuffer buffer = charset.encode(node.toString());
					byteListener.receive(buffer, Integer.toString(i));
				}

				
				//features = mapper.readTree(jsonString);
				
			
		}
		catch (Exception e)
		{
			LOGGER.error("FS_FETCH_ERROR", featureService, e.getMessage());
			LOGGER.info(e.getMessage(), e);
		}
	}

	private void stop(boolean unregisterAsListener)
	{
		if (getRunningState() == RunningState.STARTED)
		{
			setRunningState(RunningState.STOPPING);
			runThread.dismiss();
			runThread.interrupt();
			runThread = null;
		}
		setRunningState(RunningState.STOPPED);
		setErrorMessage(null);
		if (unregisterAsListener)
		{
			try
			{
				disconnectOBI();
				token = null;
			}
			catch (Throwable t)
			{
				// Chances are, system is shutting down and agsManager instance has gone away.
				LOGGER.info("STOP_ERROR", t.getMessage());
			}
		}
	}
	
	public synchronized void setup()
	{
		lastTimestamp = -1;
		host = getProperty("host").getValueAsString();
		user = getProperty("user").getValueAsString();
		pw = getProperty("pw").getValueAsString();
		layerIndex = getProperty("layerIndex").getValueAsString();
		getNewFeaturesOnly = Boolean.parseBoolean(getProperty("newFeaturesOnly").getValueAsString());
		if (getNewFeaturesOnly) {

			newFeaturesTimeFieldName = getProperty("cleanupTimeField")
					.getValueAsString();

		}

		refreshInterval = 10;
		String stringValue = "";
		try
		{
			stringValue = getProperty("refreshInterval").getValueAsString();
			refreshInterval = Integer.parseInt(stringValue);
		}
		
		catch (NumberFormatException ex)
		{
			LOGGER.warn("REFRESH_INTERVAL_PARSE_ERROR", "refreshInterval", stringValue, refreshInterval);
		}
		featureService = getProperty("serviceName").getValueAsString();
		queryDefinition = getProperty("queryDefinition").getValueAsString();
		
		layerDescriptionForLogs = surroundBrackets("MarkLogic OBI")+ surroundBrackets(featureService) + surroundBrackets(layerIndex)+surroundBrackets("FeatureServer");
	}
	
	private void connectOBI() throws Exception
	{
		GeoEventHttpClient http = httpClientService.createNewClient();
		String clientUrl = host + "/user/login";
		String requestBody = generateTokenPayLoad();
		//HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, "POST", "", "application/json", "application/x-www-form-urlencoded", requestBody, LOGGER);
		URL url = new URL(clientUrl);
		HttpPost request = null;
		try
		{
			request = new HttpPost(url.toURI());
		}
		catch(URISyntaxException e)
		{
			
		}
		ContentType contentType = ContentType.create("application/json;charset=UTF-8");
		StringEntity entity = new StringEntity(requestBody, contentType);
		request.setEntity(entity);
		CloseableHttpResponse response = http.execute(request, GeoEventHttpClient.DEFAULT_TIMEOUT);
		if(response.getStatusLine().getStatusCode()==200)
		{
			Header[] headers = response.getAllHeaders();
			for (Header h : headers) {
				if (h.getName().equals("set-cookie")) {
					token = h.getValue();
				}
			}
		}
	}
	
	private void disconnectOBI() throws MalformedURLException
	{
		GeoEventHttpClient http = httpClientService.createNewClient();
		String logouturl = host + "/user/logout";
		URL url = new URL(logouturl);
		HttpPost request = null;
		try
		{
			request = new HttpPost(url.toURI());
		}
		catch(URISyntaxException e)
		{
			
		}
		request.setHeader("Cookie", token);
	
		CloseableHttpResponse response;
		try
		{
			response = http.execute(request, GeoEventHttpClient.DEFAULT_TIMEOUT);
			if (response == null) {
				if (getRunningState() == RunningState.ERROR) {
					LOGGER.info("RECONNECTION_MSG", logouturl);
					setErrorMessage(null);
					setRunningState(RunningState.STARTED);
				}

			} else {
				// log only if we were not in error state already
				if (getRunningState() != RunningState.ERROR) {
					;
				}
			}
		}
		catch(Exception e)
		{
			
		}
	}
	
	private String generateTokenPayLoad() throws Exception
	{
		try {
			String userKey = surroundQuotes("username");
			String userString = surroundQuotes(user);
			String pwKey = surroundQuotes("password");
			String password;
			password = cryptoService.decrypt(pw);
			String passwordString = surroundQuotes(password);
			String content = userKey + ": " + userString + "," + pwKey + ": "
					+ passwordString;
			String requestBody = surroundCurlyBrackets(content);
			return requestBody;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw (e);
		}
	}
	
	private void updateLastTimestamp(JsonNode root) throws ParseException
	{

		JsonNode features = root.get("features");
		String format ="yyyy-MM-dd'T'HH:mm:ss.SSS";
		if (features.isArray()) {
			for (int i = 0; i < features.size(); i++) {
				JsonNode feature = features.get(i);
				JsonNode attributes = feature.get("attributes");
				JsonNode time = attributes.get("LastUpdatedDateTime");
				if (feature.get("attributes").get("LastUpdatedDateTime") != null) {
					String timeString = time.toString();
					timeString = timeString.substring(1, timeString.length()-4);
					TimeZone tz = TimeZone.getTimeZone("UTC");
					SimpleDateFormat dateFormat = new SimpleDateFormat(format);
					dateFormat.setTimeZone(tz);
					Date d = dateFormat.parse(timeString);
					long ts = d.getTime();
					if (ts > lastTimestamp)
						lastTimestamp = ts;
				} else {
					LOGGER.warn("NO_TIME_FIELD_FOUND",
							newFeaturesTimeFieldName, root.toString());
				}

			}
		}
	}
	
	private String postAndGetReply(URL url, Collection<KeyValue> params) throws IOException
	{
		String responseString = null;
		try (GeoEventHttpClient http = httpClientService.createNewClient())
		{
			//HttpPost postRequest = http.createPostRequest(url, params);
			
			List<NameValuePair> formParams = new ArrayList<NameValuePair>();
		    if (params != null)
		    {
		      for (KeyValue parameter : params)
		      {
		        formParams.add(new BasicNameValuePair(parameter.getKey(), parameter.getValue()));
		        LOGGER.debug("HTTP_ADDING_PARAM", parameter.getKey(), parameter.getValue());
		      }
		    }
		    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formParams, "UTF-8");
		    HttpPost postRequest;
		    try
		    {
		    	postRequest = new HttpPost(url.toURI());
		    }
		    catch (URISyntaxException e)
		    {
		      throw new RuntimeException(e);
		    }
		    postRequest.setEntity(entity);
			postRequest.addHeader("Accept", "application/json,text/html,application/xhtml+xml,application/xml");
			postRequest.addHeader("Cookie", token);
			//postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
			postRequest.setHeader("charset","utf-8");
			responseString = http.executeAndReturnBody(postRequest, GeoEventHttpClient.DEFAULT_TIMEOUT);
		}
		catch (Exception e)
		{
			LOGGER.debug(e.getMessage());
		}
		return responseString;
	}
	
	private String executeGetAndGetReply(URL url, Collection<KeyValue> params) {
		String responseString = null;
		try (GeoEventHttpClient http = httpClientService.createNewClient()) {
			// HttpGet getRequest = http.createGetRequest(url, params);
			String paramString = "?";
			Boolean isFirst = true;
			for (KeyValue k : params) {
				if (!isFirst) {
					paramString += "&";
				} else {
					isFirst = false;
				}
				String key = k.getKey();
				String value = k.getValue();
				paramString += key + "=" +  URLEncoder.encode(value, "UTF-8");
				

			}
			//String encodedParams = URLEncoder.encode(paramString, "UTF-8");
			String uri = url + paramString;
			//String uri = "http://obi-esri.demo.marklogic.com/rest/services/obi-arcgis/FeatureServer/4/query?f=json&outfields=track-id,objectId&returnGeometry=false&where=12345";
			//token="sid=s%3AeP9e9pkN47h7H31sxa3L8pBPVPP-xI9I.%2BfetBeKb%2F%2BuBBJjBNd2V65x7z6QFruZynsRK2%2Fb0peM";
			HttpGet getRequest = new HttpGet(uri);
			String contentType = "text/html,application/xhtml+xml,application/xml,application/json";
			getRequest.addHeader("Cookie", token);
			getRequest.addHeader("Accept", contentType);
			HttpClient httpclient = HttpClientBuilder.create().build();
			HttpResponse response = httpclient.execute(getRequest);
			HttpEntity entity = response.getEntity();
			InputStream instream = entity.getContent();
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(
						(instream)));
				responseString = "";
				String ln;
				while ((ln = br.readLine()) != null) {
					responseString += ln;
				}
			} catch (IOException ex) {
				// In case of an IOException the connection will be
				// released
				// back to the connection manager automatically
				LOGGER.error(ex.getMessage());
				throw ex;

			} catch (RuntimeException ex) {
				// In case of an unexpected exception you may want to
				// abort
				// the HTTP request in order to shut down the underlying
				// connection immediately.
				LOGGER.error(ex.getMessage());
				getRequest.abort();
				throw ex;
			} catch (Exception ex) {

				LOGGER.error(ex.getMessage());
				getRequest.abort();
				throw ex;
			} finally {
				// Closing the input stream will trigger connection
				// release
				try {
					instream.close();
				} catch (Exception ignore) {
				}
			}
			
		} catch (Exception e) {
			LOGGER.debug(e.getMessage());
		}
		return responseString;
	}
	
	//helper methods
		private String surroundQuotes(String in)
		{
			String out = "\"" + in + "\"";
			return out;
		}
		
		
		
		private String surroundCurlyBrackets(String in)
		{
			return "{" + in + "}";
		}
		
		private String surroundBrackets(String in)
		{
			return "[" + in + "]";
		}
		
		

}
