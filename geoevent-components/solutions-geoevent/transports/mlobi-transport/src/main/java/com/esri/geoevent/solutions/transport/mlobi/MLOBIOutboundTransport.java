package com.esri.geoevent.solutions.transport.mlobi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.component.RunningState;
import com.esri.ges.core.http.GeoEventHttpClient;
import com.esri.ges.core.http.GeoEventHttpClientService;
import com.esri.ges.core.http.KeyValue;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.transport.OutboundTransportBase;
import com.esri.ges.transport.RestOutboundTransportProvider;
import com.esri.ges.transport.TransportContext;
import com.esri.ges.transport.TransportDefinition;
import com.esri.ges.transport.http.HttpTransportContext;
import com.esri.ges.transport.http.HttpUtil;
import com.esri.ges.util.DateUtil;
import com.esri.ges.util.Validator;


public class MLOBIOutboundTransport extends OutboundTransportBase implements RestOutboundTransportProvider {
	private enum RequestType {GENERATE_TOKEN, QUERY, UPDATE}
	private String 										host;
	private String 										user;
	private String 										pw;
	private GeoEventHttpClientService					httpClientService;
	private HttpTransportContext						context;
	private String 										token;
	private static final BundleLogger					LOGGER= BundleLoggerFactory.getLogger(MLOBIOutboundTransport.class);
	private String 										clientUrl;
	protected String									loginUrl;
	protected String									httpMethod;
	private String										acceptableMimeTypes_client;
	protected String									postBodyType;
	protected String									postBody = "";
	private String										headerParams;
	//private String										mode;
	private int 										httpTimeoutValue;
	private String 										featureService;
	private String 										layerIndex;
	private boolean										append;
	private volatile String								trackIDField;
	private final HashMap<String, String>				oidCache = new HashMap<String, String>();
	private final ObjectMapper							mapper = new ObjectMapper();
	//private String 										oidQueryParams;
	private final List<String>							insertFeatureList = new ArrayList<String>();
	private final List<String>							updateFeatureList = new ArrayList<String>();
	private final StringBuilder							featureBuffer				= new StringBuilder(1024);
	private boolean										cleanupOldFeatures	= false;
	private int											featureLifeSpan;
	private int											cleanupFrequency;
	private volatile String							    cleanupTimeField;
	private CleanupThread								cleanupThread;
	private volatile int								maxTransactionSize	= 500;
	private JsonNode									features;

	private String										layerDescriptionForLogs;
	public static final String							AGS_DATE_FORMAT	= "yyyy-MM-dd HH:mm:ss";
	@SuppressWarnings("rawtypes")
	private static ThreadLocal format = new ThreadLocal() {
		protected synchronized Object initialValue() {
			return new SimpleDateFormat(AGS_DATE_FORMAT);
		}
	};
	public MLOBIOutboundTransport(TransportDefinition definition, GeoEventHttpClientService httpClientService)
			throws ComponentException {
		super(definition);
		this.httpClientService = httpClientService;
	}
	
	//Overridden methods
	
	@Override
	public void afterPropertiesSet()
	{
		setup();
	}
	/*@Override
	public void beforeConnect(TransportContext context)
	{
		try {
			HttpRequest request = ((HttpTransportContext)context).getHttpRequest();
			if(token == null)
			{
				String password = cryptoService.decrypt(pw);
				generateToken(user, password, (HttpTransportContext)context);
			}
			request.setHeader("Cookie", token);
			
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
		
	}*/
	
	
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public synchronized void start()
	{
		switch (getRunningState())
		{
			case STARTING:
			case STARTED:
			case ERROR:
				return;
		}
		setRunningState(RunningState.STARTING);
		setup();
		context = new HttpTransportContext();
		context.setHttpClientService(httpClientService);
		setRunningState(RunningState.STARTED);
		setErrorMessage(null);
		startCleanUpThread();
	}

	@Override
	public synchronized void stop()
	{
		super.stop();
		LOGGER.debug("OUTBOUND_STOP");
		stop(true);
		if (token != null)
		{
			token = null;
		}
	}

	
	
	@Override
	public void onReceive(TransportContext context) {
		super.onReceive(context);
		if (token == null) {
			if (!(context instanceof HttpTransportContext))
				return;

			HttpResponse response = ((HttpTransportContext) context)
					.getHttpResponse();

			Header[] headers = response.getAllHeaders();
			for (Header h : headers) {
				if (h.getName().equals("set-cookie")) {
					token = h.getValue();
				}
			}
		}
	}

	@Override
	public void receive(ByteBuffer bb, String channelid) {
		if (host != null || !host.isEmpty())
		{
			//byte[] data = new byte[bb.remaining()];
			//bb.get(data);
			if(this.getRunningState()==RunningState.STARTED){
				if(token == null)
				{
					doHttp("", RequestType.GENERATE_TOKEN);
				}
				try{
					CharsetDecoder decoder = getCharsetDecoder();
					CharBuffer charBuffer = decoder.decode(bb);
					String jsonString = charBuffer.toString();
					doHttp(jsonString, RequestType.UPDATE);
				}
				catch(Exception e)
				{
					String errorMsg = LOGGER.translate("BUFFER_PARSING_ERROR", e.getMessage());
					LOGGER.error(errorMsg);
					LOGGER.info(e.getMessage(), e);
					//errorMessage = errorMsg;
				}
			}
		}
		
	}

	@Override
	public byte[] processCache(HashMap<String, String[]> arg0) {
		
		return null;
	}
	
	public synchronized void setup()
	{
		host = getProperty("server").getValueAsString();
		user = getProperty("username").getValueAsString();
		pw = getProperty("password").getValueAsString();
		featureService = getProperty("featureservice").getValueAsString();
		layerIndex = getProperty("layerindex").getValueAsString();
		trackIDField = getProperty("trackid").getValueAsString();
		cleanupOldFeatures = ((Boolean) getProperty("cleanupFeatures").getValue()).booleanValue();
		featureLifeSpan = Integer.parseInt(getProperty("featureLifeSpan").getValueAsString());
		cleanupFrequency = Integer.parseInt(getProperty("cleanupFrequency").getValueAsString());
		cleanupTimeField = getProperty("cleanupTimeField").getValueAsString();
		loginUrl = host + "/user/login";
		clientUrl = host + "/rest/services/" + featureService + "/" + layerIndex + "/updateFeatures";
		httpMethod = "POST";
		acceptableMimeTypes_client = "application/json";
		postBodyType = "application/x-www-form-urlencoded";
		headerParams = "";
		//mode = "CLIENT";
		httpTimeoutValue = GeoEventHttpClient.DEFAULT_TIMEOUT;
		layerDescriptionForLogs = surroundBrackets("MarkLogic OBI")+ surroundBrackets(featureService) + surroundBrackets(layerIndex)+surroundBrackets("FeatureServer");
		
		
	}
	
	
		
	/*private void doHttp(byte[] data)
	{
		
		try (GeoEventHttpClient http = httpClientService.createNewClient())
		{
			HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, httpMethod, "", acceptableMimeTypes_client, postBodyType, data, headerParams, LOGGER);
			if (request != null && request instanceof HttpUriRequest)
			{
				context.setHttpRequest(request);
				this.beforeConnect(context);
				CloseableHttpResponse response;
				try
				{
					response = http.execute(request, httpTimeoutValue);
					if (response != null)
					{
					// check if we were in error state - if so then set state to running
						// - we have reconnected
						if (getRunningState() == RunningState.ERROR)
						{
							LOGGER.info("RECONNECTION_MSG", clientUrl);
							setErrorMessage(null);
							setRunningState(RunningState.STARTED);
						}
						
						context.setHttpResponse(response);
						this.onReceive(context);
					}
					else
					{
						// log only if we were not in error state already
						if (getRunningState() != RunningState.ERROR)
						{
							String errorMsg = LOGGER.translate("FAILED_HTTP_METHOD", clientUrl, httpMethod);
							LOGGER.info(errorMsg);

							// set the error state
							setErrorMessage(errorMsg);
							setRunningState(RunningState.ERROR);
						}
					}
				}
				catch (IOException e)
				{
					// log only if we were not in error state already
					if( getRunningState() != RunningState.ERROR )
					{
						
						String errorMsg = LOGGER.translate("ERROR_ACCESSING_URL", clientUrl, e.getMessage());
						LOGGER.error(errorMsg);
						LOGGER.info(e.getMessage(), e);
						
						// set the error state
						setErrorMessage(errorMsg);
						setRunningState(RunningState.ERROR);
					}
				}
			}
		}
		catch (Exception exp)
		{
			LOGGER.error(exp.getMessage(), exp);
		}
	}

	protected void doHttp()
	{
		try (GeoEventHttpClient http = httpClientService.createNewClient())
		{
			HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, httpMethod, "", acceptableMimeTypes_client, postBodyType, postBody, headerParams, LOGGER);
			if (request != null && request instanceof HttpUriRequest)
			{
				context.setHttpRequest(request);
				this.beforeConnect(context);
				CloseableHttpResponse response = null;
				try
				{
					response = http.execute(request, httpTimeoutValue);
					if (response != null)
					{
						// check if we were in error state - if so then set state to running
						// - we have reconnected
						if (getRunningState() == RunningState.ERROR)
						{
							LOGGER.info("RECONNECTION_MSG", clientUrl);
							setErrorMessage(null);
							setRunningState(RunningState.STARTED);
						}

						context.setHttpResponse(response);
						this.onReceive(context);
					}
					else
					{
						// log only if we were not in error state already
						if (getRunningState() != RunningState.ERROR)
						{
							String errorMsg = LOGGER.translate("RESPONSE_FAILURE", clientUrl);
							LOGGER.info(errorMsg);

							// set the error state
							setErrorMessage(errorMsg);
							setRunningState(RunningState.ERROR);
						}
					}
				}
				catch (IOException e)
				{
					// log only if we were not in error state already
					if( getRunningState() != RunningState.ERROR )
					{
						
						String errorMsg = LOGGER.translate("ERROR_ACCESSING_URL", clientUrl, e.getMessage());
						LOGGER.error(errorMsg);
						LOGGER.info(e.getMessage(), e);
						
						// set the error state
						setErrorMessage(errorMsg);
						setRunningState(RunningState.ERROR);
					}
				}
				finally
				{
					IOUtils.closeQuietly(response);
				}
			}
		}
		catch (Exception exp)
		{
			LOGGER.error(exp.getMessage(), exp);
		}
	}*/
	
	/*private void doHttp(byte[] data)
	{
		
		try (GeoEventHttpClient http = httpClientService.createNewClient())
		{
			HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, httpMethod, "", acceptableMimeTypes_client, postBodyType, data, headerParams, LOGGER);
			if (request != null && request instanceof HttpUriRequest)
			{
				context.setHttpRequest(request);
				this.beforeConnect(context);
				CloseableHttpResponse response;
				try
				{
					response = http.execute(request, httpTimeoutValue);
					if (response != null)
					{
					// check if we were in error state - if so then set state to running
						// - we have reconnected
						if (getRunningState() == RunningState.ERROR)
						{
							LOGGER.info("RECONNECTION_MSG", clientUrl);
							setErrorMessage(null);
							setRunningState(RunningState.STARTED);
						}
						
						context.setHttpResponse(response);
						this.onReceive(context);
					}
					else
					{
						// log only if we were not in error state already
						if (getRunningState() != RunningState.ERROR)
						{
							String errorMsg = LOGGER.translate("FAILED_HTTP_METHOD", clientUrl, httpMethod);
							LOGGER.info(errorMsg);
	
							// set the error state
							setErrorMessage(errorMsg);
							setRunningState(RunningState.ERROR);
						}
					}
				}
				catch (IOException e)
				{
					// log only if we were not in error state already
					if( getRunningState() != RunningState.ERROR )
					{
						
						String errorMsg = LOGGER.translate("ERROR_ACCESSING_URL", clientUrl, e.getMessage());
						LOGGER.error(errorMsg);
						LOGGER.info(e.getMessage(), e);
						
						// set the error state
						setErrorMessage(errorMsg);
						setRunningState(RunningState.ERROR);
					}
				}
			}
		}
		catch (Exception exp)
		{
			LOGGER.error(exp.getMessage(), exp);
		}
	}
	
	protected void doHttp()
	{
		try (GeoEventHttpClient http = httpClientService.createNewClient())
		{
			HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, httpMethod, "", acceptableMimeTypes_client, postBodyType, postBody, headerParams, LOGGER);
			if (request != null && request instanceof HttpUriRequest)
			{
				context.setHttpRequest(request);
				this.beforeConnect(context);
				CloseableHttpResponse response = null;
				try
				{
					response = http.execute(request, httpTimeoutValue);
					if (response != null)
					{
						// check if we were in error state - if so then set state to running
						// - we have reconnected
						if (getRunningState() == RunningState.ERROR)
						{
							LOGGER.info("RECONNECTION_MSG", clientUrl);
							setErrorMessage(null);
							setRunningState(RunningState.STARTED);
						}
	
						context.setHttpResponse(response);
						this.onReceive(context);
					}
					else
					{
						// log only if we were not in error state already
						if (getRunningState() != RunningState.ERROR)
						{
							String errorMsg = LOGGER.translate("RESPONSE_FAILURE", clientUrl);
							LOGGER.info(errorMsg);
	
							// set the error state
							setErrorMessage(errorMsg);
							setRunningState(RunningState.ERROR);
						}
					}
				}
				catch (IOException e)
				{
					// log only if we were not in error state already
					if( getRunningState() != RunningState.ERROR )
					{
						
						String errorMsg = LOGGER.translate("ERROR_ACCESSING_URL", clientUrl, e.getMessage());
						LOGGER.error(errorMsg);
						LOGGER.info(e.getMessage(), e);
						
						// set the error state
						setErrorMessage(errorMsg);
						setRunningState(RunningState.ERROR);
					}
				}
				finally
				{
					IOUtils.closeQuietly(response);
				}
			}
		}
		catch (Exception exp)
		{
			LOGGER.error(exp.getMessage(), exp);
		}
	}*/
	
	private void doHttp(String jsonString, RequestType type)
	{
		try
		{
			if(type == RequestType.GENERATE_TOKEN)
			{
				GeoEventHttpClient http = httpClientService.createNewClient();
				clientUrl = host + "/user/login";
				String requestBody = generateTokenPayLoad();
				//HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, "POST", "", "application/json", "application/x-www-form-urlencoded", requestBody, LOGGER);
				URL url = new URL(clientUrl);
				HttpPost request = http.createPostRequest(url, requestBody, "application/json;charset=UTF-8");
				doHttp(http, request);
			}
			else if(type == RequestType.QUERY)
			{
				GeoEventHttpClient http = httpClientService.createNewClient();
				clientUrl = host + "/rest/services/" + featureService + "/" + layerIndex + "/query";
				String params = "";
				//String params = constructQueryParams();
				
				HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, "GET", params, acceptableMimeTypes_client, postBodyType,  headerParams, LOGGER);
				
				doHttp(http, request);
			}
			else if(type == RequestType.UPDATE)
			{
				//GeoEventHttpClient http = httpClientService.createNewClient();
				//clientUrl = host + "/rest/services/" + featureService + "/" + layerIndex + "/updateFeatures";
				try{
					features = mapper.readTree(jsonString);
				}
				catch (Exception ex)
				{
					LOGGER.error("ERROR_SENDING_JSON", jsonString, ex.getMessage());
					LOGGER.debug(ex.getMessage(), ex);
					return;
				}
				if (!features.isArray())
				{
					LOGGER.error("INPUT_IS_NOT_AN_ARRAY");
					return;
				}
				ArrayList<String> missingTrackIDs = getListOfUncachedTrackIDs(features);
				queryForMissingOIDs(missingTrackIDs);
				buildJSONStrings(features);
				if (!updateFeatureList.isEmpty())
				{
					performTheUpdateOperations(updateFeatureList);
				}
				// Add the new features
				if (!insertFeatureList.isEmpty())
					performTheInsertOperations(insertFeatureList);
				//HttpRequestBase request = HttpUtil.createHttpRequest(http, clientUrl, httpMethod, "", acceptableMimeTypes_client, postBodyType, requestBody, headerParams, LOGGER);
				//doHttp(http, request);
				
				//http = httpClientService.createNewClient();
				//clientUrl = host + "/rest/services/" + featureService + "/" + layerIndex + "/addFeatures";
				//requestBody = "";
				//String requestBody = generateInsertPayLoad(byte[] data);
				//request = HttpUtil.createHttpRequest(http, clientUrl, httpMethod, "", acceptableMimeTypes_client, postBodyType, requestBody, headerParams, LOGGER);
				//doHttp(http, request);
			}
			
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(), e);
		}
	}


	private void doHttp(GeoEventHttpClient http, HttpRequestBase request)
	{
		if (request != null && request instanceof HttpUriRequest)
		{
			context.setHttpRequest(request);
			this.beforeConnect(context);
			CloseableHttpResponse response;
			try
			{
				response = http.execute(request, httpTimeoutValue);
				if (response != null)
				{
				// check if we were in error state - if so then set state to running
					// - we have reconnected
					if (getRunningState() == RunningState.ERROR)
					{
						LOGGER.info("RECONNECTION_MSG", clientUrl);
						setErrorMessage(null);
						setRunningState(RunningState.STARTED);
					}
					
					context.setHttpResponse(response);
					this.onReceive(context);
				}
				else
				{
					// log only if we were not in error state already
					if (getRunningState() != RunningState.ERROR)
					{
						String errorMsg = LOGGER.translate("FAILED_HTTP_METHOD", clientUrl, httpMethod);
						LOGGER.info(errorMsg);

						// set the error state
						setErrorMessage(errorMsg);
						setRunningState(RunningState.ERROR);
					}
				}
			}
			catch (IOException e)
			{
				// log only if we were not in error state already
				if( getRunningState() != RunningState.ERROR )
				{
					
					String errorMsg = LOGGER.translate("ERROR_ACCESSING_URL", clientUrl, e.getMessage());
					LOGGER.error(errorMsg);
					LOGGER.info(e.getMessage(), e);
					
					// set the error state
					setErrorMessage(errorMsg);
					setRunningState(RunningState.ERROR);
				}
			}
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
	
	/*private void generateToken(String username, String pw, HttpTransportContext context) throws Exception
	{
		try
		{
			String userKey = surroundQuotes("username");
			String userString = surroundQuotes(user);
			String pwKey = surroundQuotes("password");
			String password;

			password = cryptoService.decrypt(pw);

			String passwordString = surroundQuotes(password);
			String content = userKey + ":" + userString + "," + pwKey + ":"
					+ passwordString;
			String requestBody = surroundCurlyBrackets(content);
			String url = host + "/user/login";
			GeoEventHttpClient http = httpClientService.createNewClient();
			HttpRequestBase request = HttpUtil.createHttpRequest(http, url, "POST", "", "application/json", "application/x-www-form-urlencoded", requestBody, LOGGER);
			context.setHttpRequest(request);
			
			CloseableHttpResponse response;
			try
			{
				response = http.execute(request, httpTimeoutValue);
				if(response != null)
				{
					if (getRunningState() == RunningState.ERROR)
					{
						LOGGER.info("RECONNECTION_MSG", url);
						setErrorMessage(null);
						setRunningState(RunningState.STARTED);
					}
					context.setHttpResponse(response);
					this.onReceive(context);
				}
				else
				{
					// log only if we were not in error state already
					if (getRunningState() != RunningState.ERROR)
					{
						String errorMsg = LOGGER.translate("RESPONSE_FAILURE", url);
						LOGGER.info(errorMsg);

						// set the error state
						setErrorMessage(errorMsg);
						setRunningState(RunningState.ERROR);
					}
				}
				
			}
			catch (IOException e)
			{
				
			}
		}
		catch(Exception e)
		{
			throw(e);
		}
		
		
	}*/
	
	private ArrayList<String> getListOfUncachedTrackIDs(JsonNode features)
	{
		ArrayList<String> missingTrackIDs = new ArrayList<String>();
		for (JsonNode feature : features)
		{
			JsonNode attributes = feature.get("attributes");
			JsonNode trackIDNode = attributes.get(trackIDField);
			if (trackIDNode != null)
			{
				String trackID = getTrackIdAsString(trackIDNode);
				if (!oidCache.containsKey(trackID))
				{
					if (missingTrackIDs == null)
						missingTrackIDs = new ArrayList<String>();
					if (!missingTrackIDs.contains(trackID))
						missingTrackIDs.add(trackID);
				}
			}
		}
		return missingTrackIDs;
	}
	
	private void performTheUpdateOperations(List<String> featureList) throws IOException
	{
		while (featureList.size() > maxTransactionSize)
			performTheUpdateOperations(featureList.subList(0, maxTransactionSize));

		String responseString = performTheUpdate(featureList);
		try
		{
			validateResponse(responseString);
		}
		catch (Exception e1)
		{
			if (responseString == null)
			{
				LOGGER.error("UPDATE_FAILED_NULL_RESPONSE");
			}
			else
			{
				LOGGER.debug("UPDATE_FAILED_WITH_RESPONSE", responseString);
				List<String> updatedFeatureList = cleanStaleOIDsFromOIDCache(featureList);
				responseString = performTheUpdate(updatedFeatureList);
				try
				{
					validateResponse(responseString);
				}
				catch (Exception e2)
				{
					LOGGER.error(responseString);
					LOGGER.error("FS_WRITE_ERROR", featureService, e2.getMessage());
				}
			}
		}
		LOGGER.debug("RESPONSE_HEADER_MSG", responseString);
		if (responseString != null)
		{
			JsonNode response = mapper.readTree(responseString);
			if (response.has("updateResults"))
			{
				for (JsonNode result : response.get("updateResults"))
				{
					if (result.get("success").asBoolean() == false)
					{
						int errorCode = result.get("error").get("code").asInt();
						if (errorCode == 1011 || errorCode == 1019)
						{
							String trackID = moveOIDToInsertList(result.get("objectId").asText(), features);
							LOGGER.debug("UPDATE_FAILED_TRY_INSERT_MSG", errorCode, trackID);
						}
					}
				}
			}
		}
		featureList.clear();
	}

	private String performTheUpdate(List<String> featureList) throws IOException
	{
		clientUrl = host + "/rest/services/" + featureService + "/FeatureServer/" + layerIndex + "/applyEdits";
		URL url = new URL(clientUrl);
		Collection<KeyValue> params = new ArrayList<KeyValue>();
		params.add(new KeyValue("f", "json"));
		params.add(new KeyValue("updates", makeFeatureListString(featureList)));
		
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("URL_POST_DEBUG", url, paramsToString(params));
		String responseString = postAndGetReply(url, params);
		return responseString;
	}

	private void performTheInsertOperations(List<String> featureList) throws IOException
	{
		while (featureList.size() > maxTransactionSize)
			performTheInsertOperations(featureList.subList(0, maxTransactionSize));

		clientUrl = host + "/rest/services/" + featureService + "/FeatureServer/" + layerIndex + "/applyEdits";
		URL url = new URL(clientUrl);
		Collection<KeyValue> params = new ArrayList<KeyValue>();
		String featureString = makeFeatureListString(featureList);
		params.add(new KeyValue("f", "json"));
		params.add(new KeyValue("adds", featureString));
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("URL_POST_DEBUG", url, paramsToString(params));
		String responseString = postAndGetReply(url, params);
		//String responseString = executeGetAndGetReply(url, params);
		//String responseString = postAndGetReply(url, makeFeatureListString(featureList));
		try
		{
			validateResponse(responseString);
		}
		catch(Exception e)
		{
			featureList.clear();
		}
		LOGGER.debug("RESPONSE_HEADER_MSG", responseString);
		featureList.clear();
	}
	
	

	private String makeFeatureListString(List<String> featureList)
	{
		featureBuffer.setLength(0);
		featureBuffer.append("[");
		for (String feature : featureList)
		{
			featureBuffer.append(feature);
			featureBuffer.append(",");
		}
		featureBuffer.deleteCharAt(featureBuffer.length() - 1);
		featureBuffer.append("]");
		return featureBuffer.toString();
	}

	private String moveOIDToInsertList(String objectId, JsonNode features) throws IOException
	{
		for (JsonNode feature : features)
		{
			JsonNode attributes = feature.get("attributes");
			if (attributes.has(trackIDField) && attributes.has("objectId"))
			{
				String trackID = attributes.get(trackIDField).asText();
				String oid = attributes.get("objectId").asText();
				if (oid != null && oid.equals(objectId))
				{
					((ObjectNode) attributes).remove("objectId");
					if (oidCache.containsKey(trackID))
						oidCache.remove(trackID);
					insertFeatureList.add(mapper.writeValueAsString(feature));
					return trackID;
				}
			}
		}
		return null;
	}
	
	private void buildJSONStrings(JsonNode features) throws NumberFormatException, IOException
	{
		for (JsonNode feature : features)
		{
			/*if (!layerDetails.iszEnabled())
			{
				JsonNode geometry = feature.get("geometry");
				if (geometry != null && geometry.has("z"))
				{
					((ObjectNode) geometry).remove("z");
				}
			}*/
			if (!append)
			{
				JsonNode attributes = feature.get("attributes");
				JsonNode trackIDNode = attributes.get(trackIDField);
				if (trackIDNode == null)
				{
					LOGGER.warn("FAILED_TO_UPDATE_INVALID_TRACK_ID_FIELD", trackIDField);
				}
				else
				{
					// String trackID = trackIDNode.getTextValue();
					String trackID = getTrackIdAsString(trackIDNode);

					if (oidCache.containsKey(trackID))
					{
						String oid = oidCache.get(trackID);
						String newFeatureString = createFeatureWithOID(feature, oid);
						updateFeatureList.add(newFeatureString);
						continue;
					}
				}
			}
			insertFeatureList.add(mapper.writeValueAsString(feature));
		}
	}
	
	private String createFeatureWithOID(JsonNode feature, String oid) throws IOException
	{
		JsonNode attributes = feature.get("attributes");
		String oidField = "objectId";
		if (attributes.has(oidField))
			((ObjectNode) attributes).remove(oidField);
		if (oid != null)
			((ObjectNode) attributes).put(oidField, Integer.parseInt(oid));
		String newFeatureString = mapper.writeValueAsString(feature);
		return newFeatureString;
	}
	
	private void queryForMissingOIDs(List<String> missingTrackIDs) throws IOException
	{
		if (missingTrackIDs.size() == 0)
			return;
		while (missingTrackIDs.size() > maxTransactionSize)
			queryForMissingOIDs(missingTrackIDs.subList(0, maxTransactionSize));

		StringBuffer buf = new StringBuffer(1024);
		for (String trackID : missingTrackIDs)
		{
			/*LOGGER.debug("QUERYING_FOR_MISSING_TRACK_ID", trackID);
			if (buf.length() == 0)
				buf.append(trackIDField + " IN (");
			else
				buf.append(",");
			buf.append("\'" + trackID + "\'");*/
			buf.append(trackID);
		}
		//buf.append(")");
		missingTrackIDs.clear();
		String whereString = buf.toString();
		//String whereString = trackID;
		performMissingOIDQuery(whereString);
	}
	
	private void performMissingOIDQuery(String whereString) throws IOException
	{
		Collection<KeyValue> params = new ArrayList<KeyValue>();
		
		params.add(new KeyValue("f", "json"));
		params.add(new KeyValue("outfields", trackIDField + "," + "objectId"));
		params.add(new KeyValue("returnGeometry", "false"));
		params.add(new KeyValue("where", whereString));
		clientUrl = host + "/rest/services/" + featureService + "/FeatureServer/" + layerIndex + "/query";
		URL url = new URL(clientUrl);
		if (LOGGER.isDebugEnabled())
			LOGGER.debug("URL_POST_DEBUG", url, paramsToString(params));
		//String responseString = postAndGetReply(url, params);
		String responseString = executeGetAndGetReply(url, params);
		try
		{
			validateResponse(responseString);
		}
		catch (IOException ex)
		{
			LOGGER.error("URL_POST_ERROR", ex, url, paramsToString(params));
			throw ex;
		}
		LOGGER.debug("RESPONSE_HEADER_MSG", responseString);
		JsonNode response = mapper.readTree(responseString);
		if (!response.has("features"))
			return;
		for (JsonNode feature : response.get("features"))
		{
			JsonNode attributes = feature.get("attributes");
			String oid = String.valueOf(attributes.get("objectId"));
			// String trackID = attributes.get(trackIDField).getTextValue();
			JsonNode tidNode = attributes.get("trackid");
			String trackID = getTrackIdAsString(tidNode);

			if (trackID != null)
			{
				oidCache.put(trackID, oid);
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

	private String postAndGetReply(URL url, String body) throws IOException
	{
		String responseString = null;
		try (GeoEventHttpClient http = httpClientService.createNewClient())
		{
			HttpPost postRequest = http.createPostRequest(url, body, "application/json;charset=UTF-8");

			postRequest.addHeader("Accept", "text/html,application/xhtml+xml,application/xml");
			postRequest.addHeader("Cookie", token);
			//postRequest.addHeader("Content Type","application/json;charset=UTF-8");
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
	
	private String getTrackIdAsString(JsonNode trackIDNode)
	{
		String output = null;
		if (trackIDNode.isTextual())
			output = trackIDNode.getTextValue();
		else if (trackIDNode.isInt())
			output = Integer.toString(trackIDNode.getIntValue());
		else if (trackIDNode.isLong())
			output = Long.toString(trackIDNode.getLongValue());
		else if (trackIDNode.isDouble())
			output = Double.toString(trackIDNode.getDoubleValue());
		else if (trackIDNode.isFloatingPointNumber())
			output = trackIDNode.getDecimalValue().toString();

		if (!Validator.isEmpty(output))
		{
			output = output.replace("'", "''");
		}
		return output;
	}
	
	private List<String> cleanStaleOIDsFromOIDCache(List<String> featureList) throws JsonProcessingException, IOException
	{

		// Construct a list of oids based on the update list
		ArrayList<String> cachedTrackIDValuesThatMightBeStale = new ArrayList<String>();
		for (String featureString : featureList)
		{
			JsonNode feature = mapper.readTree(featureString);
			JsonNode attributes = feature.get("attributes");
			JsonNode trackIDNode = attributes.get(trackIDField);
			if (trackIDNode != null)
			{
				String trackID = trackIDNode.asText();
				cachedTrackIDValuesThatMightBeStale.add(trackID);
				oidCache.remove(trackID);
			}
		}

		if (cachedTrackIDValuesThatMightBeStale.isEmpty())
			return featureList;

		queryForMissingOIDs(cachedTrackIDValuesThatMightBeStale);

		ArrayList<String> updatedFeatures = new ArrayList<String>();

		for (String featureString : featureList)
		{
			JsonNode feature = mapper.readTree(featureString);
			JsonNode attributes = feature.get("attributes");
			JsonNode trackIDNode = attributes.get(trackIDField);
			if (trackIDNode != null)
			{
				String trackID = trackIDNode.asText();
				if (oidCache.containsKey(trackID))
				{
					updatedFeatures.add(createFeatureWithOID(feature, oidCache.get(trackID)));
				}
				else
				{
					insertFeatureList.add(createFeatureWithOID(feature, null));
				}
			}
		}
		return updatedFeatures;

	}
	
	private void validateResponse(String responseString) throws IOException
	{
		if (responseString == null || mapper.readTree(responseString).has("error"))
			throw new IOException((responseString == null) ? "null response" : responseString);
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
	
	private String paramsToString(Collection<KeyValue> params)
	{
		StringBuilder sb = new StringBuilder();
		for (KeyValue param : params)
		{
			if (sb.length() > 0)
				sb.append('&');
			sb.append(param.getKey());
			sb.append('=');
			sb.append(param.getValue() == null ? "" : param.getValue());
		}
		return sb.toString();
	}
	
	private void startCleanUpThread()
	{
		if (cleanupOldFeatures && !cleanupTimeField.isEmpty())
		{
			if (cleanupThread == null && (getRunningState() == RunningState.STARTED || getRunningState() == RunningState.STARTING))
			{
				cleanupThread = new CleanupThread();
				cleanupThread.setName("OutboundFeatureServiceCleanerThread-" + layerDescriptionForLogs);
				cleanupThread.setDaemon(true);
				cleanupThread.start();
			}
		}
		else
		{
			if (cleanupThread != null)
			{
				cleanupThread.dismiss();
				cleanupThread = null;
			}
		}
	}
	
	private class CleanupThread extends Thread
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
					cleanup();
					sleep(cleanupFrequency * 1000);
				}
				catch (InterruptedException ex)
				{
					LOGGER.error("CLEANUP_THREAD_INTERRUPTED", ex.getMessage());
					LOGGER.info(ex.getMessage(), ex);
				}
				catch (Throwable t)
				{
					LOGGER.error("CLEANUP_ERROR", t, cleanupFrequency);
					LOGGER.info(t.getMessage(), t);
				}
			}
		}
	}
	
	private void cleanup()
	{
		
		URL url;

		Collection<KeyValue> params = new ArrayList<KeyValue>();
		try
		{
			Date now = new Date();
			Date cutoffDate = DateUtil.addMins(now, (-1 * featureLifeSpan));
			// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			// sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
			// String dateStr = sdf.format(cutoffDate);
			// String whereClause = cleanupTimeField + "<'" +dateStr + "'";
			// String whereClause = cleanupTimeField + " < " + ags.getDateTimeQueryOperand(dateStr);
			String whereClause = cleanupTimeField + " " + "<" + " timestamp '" + formatUTCDate(cutoffDate) + "'";
			clientUrl = host + "/rest/services/" + featureService + "/" + layerIndex +"/deleteFeatures";
			url = new URL(clientUrl);
			params.add(new KeyValue("f", "json"));
			params.add(new KeyValue("where", whereClause));

			String responseString = postAndGetReply(url, params);
			validateResponse(responseString);
			LOGGER.debug("RESPONSE_HEADER_MSG", responseString);
		}
		catch (Exception e)
		{
			LOGGER.error("ERROR_DELETING_FS", featureService, paramsToString(params), e.getMessage());
			LOGGER.info(e.getMessage(), e);
		}
	}
	
	/*private boolean internalValidate()
	{
		try
		{
			
			boolean hasField = false;
			List<String> fields = layerDetailsLocal.getFields();
			if (!append)
			{
				if (fields != null)
				{
					for (String field : fields)
					{
						if (field.equals(trackIDField))
						{
							hasField = true;
							break;
						}
					}
				}
				if (!hasField)
					throw new Exception(LOGGER.translate("ID_FIELD_DOES_NOT_EXIST", trackIDField));

				String[] requiredCaps = { "Create", "Query", "Update" };
				validateCapability(layerDetailsLocal.getCapabilities(), requiredCaps);
			}
			else
			{
				String[] requiredCaps = { "Create" };
				validateCapability(layerDetailsLocal.getCapabilities(), requiredCaps);
			}
			boolean hasCleanupTimeField = false;
			if (cleanupOldFeatures && fields != null)
			{
				for (String field : fields)
				{
					if (field.equals(cleanupTimeField))
						hasCleanupTimeField = true;
				}
				if (!hasCleanupTimeField)
					throw new Exception(LOGGER.translate("TIMESTAMP_FIELD_DOES_NOT_EXIST", cleanupTimeField));

				String[] requiredCaps = { "Delete" };
				validateCapability(layerDetailsLocal.getCapabilities(), requiredCaps);
			}
			if (getRunningState() != null && getRunningState() == RunningState.ERROR)
				setRunningState(RunningState.STOPPED);
		}
		catch (Exception e)
		{
			setErrorState(LOGGER.translate("VALIDATION_ERROR", e.getMessage()));
			LOGGER.error(LOGGER.translate("VALIDATION_ERROR", e.getMessage()));
			return false;
		}
		return true;
	}*/
	
	private void validateCapability(List<String> availableCaps, String[] requiredCaps) throws Exception
	{
		for (int i = 0; i < requiredCaps.length; i++)
		{
			if (!availableCaps.contains(requiredCaps[i]))
				throw new Exception(LOGGER.translate("MISSING_CAPABILITY", requiredCaps[i]));
		}
	}
	
	private void stop(boolean unregisterAsListener) {
		if (cleanupThread != null) {
			cleanupThread.dismiss();
			cleanupThread = null;
		}

		setErrorMessage(null);
		setRunningState(RunningState.STOPPED);

		if (unregisterAsListener) {
			try {
				GeoEventHttpClient http = httpClientService.createNewClient();
				String logouturl = host + "/user/logout";
				HttpRequestBase request = HttpUtil.createHttpRequest(http,
						logouturl, "POST", "", "application/json",
						"application/x-www-form-urlencoded", "", LOGGER);
				request.setHeader("Cookie", token);
				
				CloseableHttpResponse response;
				try {
					response = http.execute(request, httpTimeoutValue);
					if (response == null) {
						if (getRunningState() == RunningState.ERROR) {
							LOGGER.info("RECONNECTION_MSG", clientUrl);
							setErrorMessage(null);
							setRunningState(RunningState.STARTED);
						}

						context.setHttpResponse(response);
					} else {
						// log only if we were not in error state already
						if (getRunningState() != RunningState.ERROR) {
							;
						}
					}
				} catch (IOException e) {
					if( getRunningState() != RunningState.ERROR )
					{
						
						String errorMsg = LOGGER.translate("ERROR_ACCESSING_URL", clientUrl, e.getMessage());
						LOGGER.error(errorMsg);
						LOGGER.info(e.getMessage(), e);
						
						// set the error state
						setErrorMessage(errorMsg);
						setRunningState(RunningState.ERROR);
					}
				}

			} catch (Throwable t) {
				// Chances are we're shutting down...
				LOGGER.warn("STOP_ERROR", t.getMessage());
			}
		}
	}
	
	private void setErrorState(String message)
	{
		stop(false);
		setRunningState(RunningState.ERROR);
		setErrorMessage(message);
		LOGGER.error(message);
	}
	
	public static String formatUTCDate(Date date)
	{
		DateFormat format = getFormat();
		format.setTimeZone(TimeZone.getTimeZone("UTC"));
		return format.format(date);
	}
	
	private static DateFormat getFormat()
	{
		return (DateFormat) format.get();
	}

}
