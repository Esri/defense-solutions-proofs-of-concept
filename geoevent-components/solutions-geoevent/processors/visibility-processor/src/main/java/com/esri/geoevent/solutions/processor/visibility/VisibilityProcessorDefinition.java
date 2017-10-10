package com.esri.geoevent.solutions.processor.visibility;

/*
 * #%L
 * Esri :: AGES :: Solutions :: Processor :: Visibility
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;

import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class VisibilityProcessorDefinition extends GeoEventProcessorDefinitionBase {
	
	// private static final Log LOG = LogFactory.getLog(VisibilityProcessorDefinition.class);
			
	private static final BundleLogger LOG = BundleLoggerFactory.getLogger(VisibilityProcessorDefinition.class);

			
	private String lblGPService="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_GP_SERVICE}";
	private String descGPService="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_GP_SERVICE}";
	private String lblElevService="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_ELEVATION_SERVICE}";
	private String descElevService="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_ELEVATION_SERVICE}";
	private String lblObsSrc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_OBS_SRC}";
	private String descObsSrc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_OBS_SRC}";
	private String lblObsX="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_OBS_X}";
	private String descObsX="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_OBS_X}";
	private String lblObsY="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_OBS_Y}";
	private String descObsY="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_OBS_Y}";
	private String lblObsXFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_OBS_X_FIELD}";
	private String descObsXFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_OBS_X_FIELD}";
	private String lblObsYFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_OBS_Y_FIELD}";
	private String descObsYFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_OBS_Y_FIELD}";
	private String lblRadSrc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_RAD_SRC}";
	private String descRadSrc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_RAD_SRC}";
	private String lblRad="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_RADIUS}";
	private String descRad="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_RADIUS}";
	private String lblRadFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_RADIUS_FIELD}";
	private String descRadFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_RADIUS_FIELD}";
	private String lblRadUnits="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_RADIUS_UNITS}";
	private String descRadUnits="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_RADIUS_UNITS}";
	private String lblObsElevSrc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_OBS_ELEVATION_SRC}";
	private String descObsElevSrc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_OBS_ELEVATION_SRC}";
	private String lblElev="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_ELEVATION}";
	private String descElev="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_ELEVATION}";
	private String lblElevFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_ELEVATION_FIELD}";
	private String descElevFld="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_ELEVATION_FIELD}";
	private String lblElevUnits="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_ELEVATION_UNITS}";
	private String descElevUnits="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_ELEVATION_UNITS}";
	private String lblWKIDIn="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_WKID_IN}";
	private String descWKIDIn="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_WKID_IN}";
	private String lblWKIDProc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_WKID_PROCESS}";
	private String descWKIDProc="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_WKID_PROCESS}";
	private String lblWKIDOut="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_WKID_OUT}";
	private String descWKIDOut="${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_WKID_OUT}";
	
	public VisibilityProcessorDefinition() {
		try {
			
			LOG.info("VisibilityProcessorDefinition(constructor) starting");
						
			PropertyDefinition procGpService = new PropertyDefinition(
					"gpservice", PropertyType.String, "", lblGPService,
					descGPService, true, false);
			propertyDefinitions.put(procGpService.getPropertyName(),
					procGpService);

			PropertyDefinition procImageService = new PropertyDefinition(
					"imageservice", PropertyType.String, "",
					lblElevService, descElevService, true,
					false);
			propertyDefinitions.put(procImageService.getPropertyName(),
					procImageService);

			List<LabeledValue> allowedObsSrc = new ArrayList<LabeledValue>();
			allowedObsSrc.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.OBS_SRC_GEOEVENT}", "Geoevent"));
			allowedObsSrc.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.OBS_SRC_FIELD}", "Field"));
			PropertyDefinition procObserverSource = new PropertyDefinition(
					"observerSource", PropertyType.String, "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.OBS_SRC_GEOEVENT}",
					lblObsSrc, descObsSrc, true,
					false, allowedObsSrc);
			propertyDefinitions.put(procObserverSource.getPropertyName(),
					procObserverSource);

			PropertyDefinition procObserverX = new PropertyDefinition(
					"observerX", PropertyType.Double, 0.0, lblObsX,
					descObsX, true, false);
			procObserverX.setDependsOn("observerSource=Constant");
			propertyDefinitions.put(procObserverX.getPropertyName(),
					procObserverX);

			PropertyDefinition procObserverY = new PropertyDefinition(
					"observerY", PropertyType.Double, 0.0, lblObsY,
					descObsY, true, false);
			procObserverY.setDependsOn("observerSource=Constant");
			propertyDefinitions.put(procObserverY.getPropertyName(),
					procObserverY);


			PropertyDefinition procEventX = new PropertyDefinition(
					"observerXEvent", PropertyType.String, "X_FIELD_NAME",
					lblObsXFld, descObsXFld,
					true, false);
			procEventX.setDependsOn("observerSource=Field");
			propertyDefinitions.put(procEventX.getPropertyName(), procEventX);

			PropertyDefinition procEventY = new PropertyDefinition(
					"observerYEvent", PropertyType.String, "Y_FIELD_NAME",
					lblObsYFld, descObsYFld,
					true, false);
			procEventY.setDependsOn("observerSource=Field");
			
			propertyDefinitions.put(procEventY.getPropertyName(), procEventY);


			List<LabeledValue> allowedSrc = new ArrayList<LabeledValue>();
			allowedSrc.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.SRC_CONSTANT}", "Constant"));
			allowedSrc.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.SRC_GEOEVENT}", "Event"));
			PropertyDefinition procRadiusSource = new PropertyDefinition(
					"radiusSource", PropertyType.String, "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.SRC_CONSTANT}", lblRadSrc,
					descRadSrc, true, false, allowedSrc);

			propertyDefinitions.put(procRadiusSource.getPropertyName(),
					procRadiusSource);

			PropertyDefinition procRadius = new PropertyDefinition("radius",
					PropertyType.Double, 1000, lblRad,
					descRad, true, false);
			procRadius.setDependsOn("radiusSource=Constant");
			propertyDefinitions.put(procRadius.getPropertyName(), procRadius);

			PropertyDefinition procRadiusEvent = new PropertyDefinition(
					"radiusEvent", PropertyType.String, "RADIUS_FIELD_NAME",
					lblRadFld,
					descRadFld, true, false);
			procRadiusEvent.setDependsOn("radiusSource=Event");
			propertyDefinitions.put(procRadiusEvent.getPropertyName(),
					procRadiusEvent);

			List<LabeledValue> unitsAllowedVals = new ArrayList<LabeledValue>();
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_METERS_LBL}","Meters"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_KM_LBL}","Kilometers"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_FT_LBL}","Feet"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_MILES_LBL}","Miles"));
			unitsAllowedVals.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_NM_LBL}","Nautical Miles"));
			PropertyDefinition procUnits = new PropertyDefinition("units",
					PropertyType.String, "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_METERS_LBL}", lblRadUnits, descRadUnits,
					true, false, unitsAllowedVals);
			
			propertyDefinitions.put(procUnits.getPropertyName(), procUnits);

			PropertyDefinition procElevSource = new PropertyDefinition(
					"elevationSource", PropertyType.String, "",
					lblObsElevSrc, descObsElevSrc, true,
					false, allowedSrc);

			propertyDefinitions.put(procElevSource.getPropertyName(),
					procElevSource);

			PropertyDefinition procElev = new PropertyDefinition("elevation",
					PropertyType.Double, 0, lblElev,
					descElev, true, false);
			procElev.setDependsOn("elevationSource=Constant");
			propertyDefinitions.put(procElev.getPropertyName(), procElev);

			PropertyDefinition procElevEvent = new PropertyDefinition(
					"elevationEvent", PropertyType.String, "ELEVATION_FIELD",
					lblElevFld,
					descElevFld, true, false);
			procElevEvent.setDependsOn("elevationSource=Event");
			propertyDefinitions.put(procElevEvent.getPropertyName(),
					procElevEvent);

			List<LabeledValue> allowedElevUnits = new ArrayList<LabeledValue>();
			allowedElevUnits.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_METERS_LBL}","Meters"));
			allowedElevUnits.add(new LabeledValue("${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_FT_LBL}","Feet"));
			PropertyDefinition procUnitsElev = new PropertyDefinition(
					"units_elev", PropertyType.String, "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.UNITS_METERS_LBL}", lblElevUnits,
					descElevUnits, true, false, allowedElevUnits);

			propertyDefinitions.put(procUnitsElev.getPropertyName(),
					procUnitsElev);
			PropertyDefinition pdOutDefName = new PropertyDefinition("outdefname", PropertyType.String, "viewshed_out", "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.LBL_OUT_DEF_NAME}", "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.DESC_OUT_DEF_NAME}", true, false);
			propertyDefinitions.put(pdOutDefName.getPropertyName(), pdOutDefName);
			PropertyDefinition procWKIDBuffer = new PropertyDefinition(
					"wkidbuffer", PropertyType.Integer, 3857, lblWKIDProc,
					descWKIDProc, true, false);
			propertyDefinitions.put(procWKIDBuffer.getPropertyName(),
					procWKIDBuffer);

			PropertyDefinition procWKIDOut = new PropertyDefinition("wkidout",
					PropertyType.Integer, 4326, lblWKIDOut,
					descWKIDOut, true, false);
			propertyDefinitions.put(procWKIDOut.getPropertyName(), procWKIDOut);
		} catch (PropertyException e) {
			LOG.error("VisibilityProcessorDefinition exception");
			LOG.error(e.getMessage());
			//LOG.error(e.getStackTrace());

		} catch (Exception e) {
			LOG.error("VisibilityProcessorDefinition exception");
			LOG.error(e.getMessage());
			//LOG.error(e.getStackTrace());

		}
		
		LOG.info("VisibilityProcessorDefinition ending");

	}
	

	@Override
	public String getName() {
		return "VisibilityProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.visibility";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.visibility.visibility-processor.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}
}
