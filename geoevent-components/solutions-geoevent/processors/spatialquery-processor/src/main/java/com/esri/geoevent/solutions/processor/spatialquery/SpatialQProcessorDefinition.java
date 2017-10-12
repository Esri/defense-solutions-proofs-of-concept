package com.esri.geoevent.solutions.processor.spatialquery;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.AccessType;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class SpatialQProcessorDefinition extends GeoEventProcessorDefinitionBase
{
	private static final Log LOG = LogFactory
			.getLog(SpatialQProcessorDefinition.class);
	private String lblGeoSrc = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_GEO_SRC}";
	private String descGeoSrc = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_GEO_SRC}";
	private String lblGeoFld = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_GEO_FIELD}";
	private String descGeoFld = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_GEO_FIELD}";
	private String lblBufferDist = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_BUFFER_DISTANCE}";
	private String descBufferDist = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_BUFFER_DISTANCE}";
	private String lblBufferUnits = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_BUFFER_UNITS}";
	private String descBufferUnits = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_BUFFER_UNITS}";
	private String lblWKIDIn = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_WKID_IN}";
	private String descWKIDIn = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_WKID_IN}";
	private String lblWKIDBuffer = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_WKID_BUFFER}";
	private String descWKIDBuffer = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_WKID_BUFFER}";	
	private String lblWKIDOut = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_WKID_OUT}";
	private String descWKIDOut = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_WKID_OUT}";	
	private String lblGeoDefName = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_GEO_DEF_NAME}";
	private String descGeoDefName = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_GEO_DEF_NAME}";
	private String lblConnection = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_CONNECTION}";
	private String descConnection = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_CONNECTION}";
	private String lblFolder = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_FOLDER}";
	private String descFolder = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_FOLDER}";
	private String lblService = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_SERVICE}";
	private String descService = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_SERVICE}";
	private String lblLayer = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_LAYER}";
	private String descLayer = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_LAYER}";
	private String lblWC = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_WC}";
	private String descWC = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_WC}";
	private String lblQueryFld = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_QUERY_FIELD}";
	private String descQueryFld = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_QUERY_FIELD}";
	private String lblEndPoint = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.LBL_ENDPOINT}";
	private String descEndPoint = "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.DESC_ENDPOINT}";
	
	public SpatialQProcessorDefinition()
	{
try {
			

			List<LabeledValue> allowedGeoSources = new ArrayList<LabeledValue>();
			allowedGeoSources.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.SRC_GEO_EVENT_LBL}","Geoevent"));
			allowedGeoSources.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.SRC_GEO_EVENT_DEF_LBL}","Event_Definition"));
			allowedGeoSources.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.SRC_GEO_BUFFER_LBL}","Buffer"));
			PropertyDefinition procGeometrySource = new PropertyDefinition(
					"geosrc", PropertyType.String, "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.SRC_GEO_EVENT_LBL}",
					lblGeoSrc, descGeoSrc,
					true, false, allowedGeoSources);
			
			propertyDefinitions.put(procGeometrySource.getPropertyName(),
					procGeometrySource);

			PropertyDefinition procGeometryEventDef = new PropertyDefinition(
					"geoeventdef", PropertyType.String, "",
					lblGeoFld,
					descGeoFld, false,
					false);
			procGeometryEventDef.setDependsOn("geosrc=Event_Definition");
			propertyDefinitions.put(procGeometryEventDef.getPropertyName(),
					procGeometryEventDef);

			PropertyDefinition procSetAsEventGeo = new PropertyDefinition(
					"setgeo", PropertyType.Boolean, false,
					"Set events geometry to input",
					"Set geoevent's geometry to input geometry", false, false);
			procSetAsEventGeo.setDependsOn("geosrc=Event_Definition");
			propertyDefinitions.put(procSetAsEventGeo.getPropertyName(),
					procSetAsEventGeo);

			PropertyDefinition procRadius = new PropertyDefinition("radius",
					PropertyType.Double, 1000, lblBufferDist, descBufferDist, true,
					false);
			procRadius.setDependsOn("geosrc=Buffer");
			propertyDefinitions.put(procRadius.getPropertyName(), procRadius);
			
			List<LabeledValue> unitsAllowedVals = new ArrayList<LabeledValue>();
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.UNITS_METERS_LBL}","Meters"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.UNITS_KM_LBL}","Kilometers"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.UNITS_FT_LBL}","Feet"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.UNITS_MILES_LBL}","Miles"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.UNITS_NM_LBL}","Nautical Miles"));
			PropertyDefinition procUnits = new PropertyDefinition("units",
					PropertyType.String, "${com.esri.geoevent.solutions.processor.spatialquery.spatialquery-processor.UNITS_METERS_LBL}", lblBufferUnits, descBufferUnits,
					true, false, unitsAllowedVals);
			procUnits.setDependsOn("geosrc=Buffer");

			propertyDefinitions.put(procUnits.getPropertyName(), procUnits);

			PropertyDefinition procWKIDBuffer = new PropertyDefinition(
					"wkidbuffer", PropertyType.Integer, 3857, lblWKIDBuffer,
					descWKIDBuffer, true, false);
			procWKIDBuffer.setDependsOn("geosrc=Buffer");
			propertyDefinitions.put(procWKIDBuffer.getPropertyName(),
					procWKIDBuffer);

			PropertyDefinition procWKIDOut = new PropertyDefinition("wkidout",
					PropertyType.Integer, 4326,lblWKIDOut,
					descWKIDOut, true, false);
			propertyDefinitions.put(procWKIDOut.getPropertyName(), procWKIDOut);

			
			PropertyDefinition procGEDName = new PropertyDefinition("gedname",
					PropertyType.String, "", lblGeoDefName, descGeoDefName,
					true, false);
			propertyDefinitions.put(procGEDName.getPropertyName(), procGEDName);
			
			PropertyDefinition pConn = new PropertyDefinition("connection", PropertyType.ArcGISConnection, null, lblConnection, descConnection, true, false);
			propertyDefinitions.put(pConn.getPropertyName(), pConn);
			
			PropertyDefinition pFolder = new PropertyDefinition("folder", PropertyType.ArcGISFolder, null, lblFolder, descFolder, true, false);
			propertyDefinitions.put(pFolder.getPropertyName(), pFolder);
			
			PropertyDefinition pService = new PropertyDefinition("service", PropertyType.ArcGISFeatureService, null, lblService, descService, true, false);
			propertyDefinitions.put(pService.getPropertyName(), pService);
			
			PropertyDefinition pLayer = new PropertyDefinition("layer", PropertyType.ArcGISLayer, null, lblLayer, descLayer, true, false);
			propertyDefinitions.put(pLayer.getPropertyName(), pLayer);
			
			PropertyDefinition pdRE = new PropertyDefinition("endpoint", PropertyType.String, "", lblEndPoint, descEndPoint, false, false);
			propertyDefinitions.put(pdRE.getPropertyName(), pdRE);
			
			PropertyDefinition pdToken = new PropertyDefinition("token", PropertyType.String, "", "token", "token", false, false);
			propertyDefinitions.put(pdToken.getPropertyName(), pdToken);
			
			PropertyDefinition pdwc = new PropertyDefinition( "wc", PropertyType.String, "", lblWC, descWC, false, false); 
			propertyDefinitions.put(pdwc.getPropertyName(), pdwc);

			PropertyDefinition pField = new PropertyDefinition("field",
					PropertyType.String, null, lblQueryFld, descQueryFld,
					true, false);
			propertyDefinitions.put(pField.getPropertyName(), pField);
			
		} catch (PropertyException e) {
			LOG.error("Geometry processor");
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			return;
		} catch (Exception e) {
			LOG.error("Geometry processor");
			LOG.error(e.getMessage());
			LOG.error(e.getStackTrace());
			return;
		}
	}

	@Override
	public String getName() {
		return "SpatialQueryProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.spatalQuery";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "Spatial Query Processor";
	}

	@Override
	public String getDescription() {
		return "Executes spatial query and packages returned fields as a group field in event";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}