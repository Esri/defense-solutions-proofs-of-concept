package com.esri.geoevent.solutions.processor.queryreport;


/*
 * #%L
 * Esri :: AGES :: Solutions :: Processor :: Geometry
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2013 - 2014 Esri
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */



import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.manager.datastore.agsconnection.ArcGISServerConnectionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class QueryReportProcessorDefinition extends
		GeoEventProcessorDefinitionBase {
	//public GeoEventDefinitionManager manager;
	private Tokenizer tokenizer = new Tokenizer();
	private static final Log LOG = LogFactory
			.getLog(QueryReportProcessorDefinition.class);
	private String lblFileName = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_FILE_NAME}";
	private String descFileName = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_FILE_NAME}";
	private String lblReportTitle = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_REPORT_TITLE}";
	private String descReportTitle = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_REPORT_TITLE}";
	private String lblReportHeader = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_REPORT_HEADER}";
	private String descReportHeader = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_REPORT_HEADER}";
	private String lblGeoSrc = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_GEO_SRC}";
	private String descGeoSrc = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_GEO_SRC}";
	private String lblGeoFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_GEO_FIELD}";
	private String descGeoFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_GEO_FIELD}";
	private String lblBufferDist = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_BUFFER_DISTANCE}";
	private String descBufferDist = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_BUFFER_DISTANCE}";
	private String lblBufferUnits = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_BUFFER_UNITS}";
	private String descBufferUnits = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_BUFFER_UNITS}";
	private String lblWKIDIn = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_WKID_IN}";
	private String descWKIDIn = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_WKID_IN}";
	private String lblWKIDBuffer = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_WKID_BUFFER}";
	private String descWKIDBuffer = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_WKID_BUFFER}";	
	private String lblWKIDOut = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_WKID_OUT}";
	private String descWKIDOut = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_WKID_OUT}";	
	private String lblGenerateTimestampToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_GENERATE_TIMESTAMP_TOKEN}";
	private String descGenerateTimestampToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_GENERATE_TIMESTAMP_TOKEN}";	
	private String lblTimeStampFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_TIMESTAMP_FIELD}";
	private String descTimeStampFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_TIMESTAMP_FIELD}";
	private String lblTimeStampToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_TIMESTAMP_Token}";
	private String descTimeStampToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_TIMESTAMP_Token}";
	private String lblHost = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_HOST}";
	private String descHost =  "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_HOST}";
	private String lblGeoDefName = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_GEO_DEF_NAME}";
	private String descGeoDefName = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_GEO_DEF_NAME}";
	private String lblConnection = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_CONNECTION}";
	private String descConnection = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_CONNECTION}";
	private String lblFolder = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_FOLDER}";
	private String descFolder = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_FOLDER}";
	private String lblService = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_SERVICE}";
	private String descService = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_SERVICE}";
	private String lblLayer = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_LAYER}";
	private String descLayer = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_LAYER}";
	private String lblLayerHeader = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_LAYER_HEADER}";
	private String descLayerHeader = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_LAYER_HEADER}";
	private String lblWC = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_WC}";
	private String descWC = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_WC}";
	private String lblCalculateDist = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_CALCULATE_DISTANCE}";
	private String descCalculateDist = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_CALCULATE_DISTANCE}";
	private String lblUseCentroid = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_USE_CENTROID}";
	private String descUseCentroid = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_USE_CENTROID}";
	private String lblSortByDist = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_SORT_BY_DIST}";
	private String descSortByDist = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_SORT_BY_DIST}";
	private String lblDistUnits = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_DIST_UNITS}";
	private String descDistUnits = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_DIST_UNITS}";
	private String distLabel= "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DISTANCE_LABEL}";
	private String lblDistToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_DIST_TOKEN}";
	private String descDistToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_DIST_TOKEN}";
	private String lblQueryFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_QUERY_FIELD}";
	private String descQueryFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_QUERY_FIELD}";
	private String lblSortFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_SORT_FIELD}";
	private String descSortFld = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_SORT_FIELD}";
	private String itemLabel = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.ITEM_LABEL}";
	private String lblItemToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_ITEM_TOKEN}";
	private String descItemToken = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_ITEM_TOKEN}";
	private String lblItemCfg = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_ITEM_CONFIG}";
	private String descItemCfg = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_ITEM_CONFIG}";
	private String lblEndPoint = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.LBL_ENDPOINT}";
	private String descEndPoint = "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.DESC_ENDPOINT}";		
	public QueryReportProcessorDefinition() {
		try {
			PropertyDefinition procReportName = new PropertyDefinition(
					"filename", PropertyType.String, "", lblFileName,
					descFileName, true, false);
			propertyDefinitions.put(procReportName.getPropertyName(),
					procReportName);

			PropertyDefinition procReportTitle = new PropertyDefinition(
					"title", PropertyType.String, "", lblReportTitle,
					descReportTitle, true, false);
			propertyDefinitions.put(procReportTitle.getPropertyName(),
					procReportTitle);

			PropertyDefinition procReportHeader = new PropertyDefinition(
					"header", PropertyType.String, "", lblReportHeader,
					descReportHeader, false, false);
			propertyDefinitions.put(procReportHeader.getPropertyName(),
					procReportHeader);

			List<LabeledValue> allowedGeoSources = new ArrayList<LabeledValue>();
			allowedGeoSources.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.SRC_GEO_EVENT_LBL}","Geoevent"));
			allowedGeoSources.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.SRC_GEO_EVENT_DEF_LBL}","Event_Definition"));
			allowedGeoSources.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.SRC_GEO_BUFFER_LBL}","Buffer"));
			PropertyDefinition procGeometrySource = new PropertyDefinition(
					"geosrc", PropertyType.String, "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.SRC_GEO_EVENT_LBL}",
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
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.UNITS_METERS_LBL}","Meters"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.UNITS_KM_LBL}","Kilometers"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.UNITS_FT_LBL}","Feet"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.UNITS_MILES_LBL}","Miles"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.UNITS_NM_LBL}","Nautical Miles"));
			PropertyDefinition procUnits = new PropertyDefinition("units",
					PropertyType.String, "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.UNITS_METERS_LBL}", lblBufferUnits, descBufferUnits,
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

			PropertyDefinition procUseTimeStamp = new PropertyDefinition(
					"usetimestamp",
					PropertyType.Boolean,
					false,
					lblGenerateTimestampToken,
					descGenerateTimestampToken,
					true, false);
			propertyDefinitions.put(procUseTimeStamp.getPropertyName(),
					procUseTimeStamp);

			PropertyDefinition procHost = new PropertyDefinition("host",
					PropertyType.String, "", lblHost, descHost,
					true, false);
			propertyDefinitions.put(procHost.getPropertyName(), procHost);
			
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
			
			PropertyDefinition pdLyrHeader = new PropertyDefinition("lyrheader", PropertyType.String, "", lblLayerHeader, descLayerHeader, false, false);
			propertyDefinitions.put(pdLyrHeader.getPropertyName(), pdLyrHeader);
			
			PropertyDefinition pdRE = new PropertyDefinition("endpoint", PropertyType.String, "", lblEndPoint, descEndPoint, false, false);
			propertyDefinitions.put(pdRE.getPropertyName(), pdRE);
			
			PropertyDefinition pdwc = new PropertyDefinition( "wc", PropertyType.String, "", lblWC, descWC, false, false); 
			propertyDefinitions.put(pdwc.getPropertyName(), pdwc);

			PropertyDefinition pdCalculateDistance = new PropertyDefinition(
					"calcDistance", PropertyType.Boolean, false,
					lblCalculateDist,
					descCalculateDist, false, false);
			propertyDefinitions.put(pdCalculateDistance.getPropertyName(),
					pdCalculateDistance);
			
			PropertyDefinition pdUseCentroid = new PropertyDefinition(
					"usecentroid", PropertyType.Boolean, true,
					lblUseCentroid,
					descUseCentroid, false, false);
			pdUseCentroid.setDependsOn("calcDistance=true");
			propertyDefinitions.put(pdUseCentroid.getPropertyName(),
					pdUseCentroid);
			
			PropertyDefinition pdSortByDist = new PropertyDefinition("sortdist", PropertyType.Boolean, "false", lblSortByDist, descSortByDist, false, false);
			pdSortByDist.setDependsOn("calcDistance=true");
			propertyDefinitions.put(pdSortByDist.getPropertyName(),
					pdSortByDist);			
			
			PropertyDefinition procDistUnits = new PropertyDefinition(
					"dist_units", PropertyType.String, "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.UNITS_METERS_LBL}", lblDistUnits,
					descDistUnits, true, false, unitsAllowedVals);
			procDistUnits.setDependsOn("calcDistance=true");
			propertyDefinitions.put(procDistUnits.getPropertyName(),
					procDistUnits);

			PropertyDefinition pField = new PropertyDefinition("field",
					PropertyType.String, null, lblQueryFld, descQueryFld,
					true, false);
			propertyDefinitions.put(pField.getPropertyName(), pField);

			PropertyDefinition pdSortField = new PropertyDefinition("sortfield", PropertyType.String, "", lblSortFld, descSortFld, false, false);
			pdSortField.setDependsOn("sortdist=false");
			propertyDefinitions.put(pdSortField.getPropertyName(),
					pdSortField);

			PropertyDefinition pdItemConfig = new PropertyDefinition(
					"item-config",
					PropertyType.String,
					"",
					lblItemCfg,
					descItemCfg,
					true, false);
			propertyDefinitions.put(pdItemConfig.getPropertyName(),
					pdItemConfig);
			
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

	//public void setManager(GeoEventDefinitionManager m) {
		//this.manager = m;
	//}

	@Override
	public String getName() {
		return "QueryReportProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.geometry";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.PROCESSOR_LBL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.queryreport.query-report-processor.PROCESSOR_DESC}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}
