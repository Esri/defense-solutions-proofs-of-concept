package com.esri.geoevent.solutions.processor.rangefan;

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
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class RangeFanProcessorDefinition extends
		GeoEventProcessorDefinitionBase {

	private static final Log LOG = LogFactory
			.getLog(RangeFanProcessorDefinition.class);

	public RangeFanProcessorDefinition() throws PropertyException {
		try {

			List<LabeledValue> allowedGeoSources = new ArrayList<LabeledValue>();
			allowedGeoSources.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_GEOEVENT}","event"));
			allowedGeoSources.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_EVENT_DEF}","geodef"));
			allowedGeoSources.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_COORD_FIELDS}","coord"));
			
			PropertyDefinition procGeometrySource = new PropertyDefinition(
					"geosrc", PropertyType.String, "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_GEOEVENT}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_GEOMETRY_SRC}", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_GEOMETRY_SRC}",
					true, false, allowedGeoSources);
			propertyDefinitions.put(procGeometrySource.getPropertyName(),
					procGeometrySource);

			PropertyDefinition procGeometryEventFld = new PropertyDefinition(
					"geoeventfld", PropertyType.String, "Geometry",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_GEOMETRY_FIELD}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_GEOMETRY_FIELD}", false,
					false);
			procGeometryEventFld.setDependsOn("geosrc=geodef");
			propertyDefinitions.put(procGeometryEventFld.getPropertyName(),
					procGeometryEventFld);

			PropertyDefinition procXField = new PropertyDefinition("xfield",
					PropertyType.String, "longitude", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_X_FIELD}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_X_FIELD}", false, false);
			procXField.setDependsOn("geosrc=coord");
			propertyDefinitions.put(procXField.getPropertyName(), procXField);

			PropertyDefinition procYField = new PropertyDefinition("yfield",
					PropertyType.String, "latitude", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_Y_FIELD}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_Y_FIELD}", false, false);
			procYField.setDependsOn("geosrc=coord");
			propertyDefinitions.put(procYField.getPropertyName(), procYField);

			List<LabeledValue> allowedSources = new ArrayList<LabeledValue>();
			allowedSources.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_CONSTANT}","Constant"));
			allowedSources.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_EVENT}","Event"));
					
			PropertyDefinition procRangeSource = new PropertyDefinition(
							"rangeSource",
							PropertyType.String,
							"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_CONSTANT}",
							"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_RANGE_SRC}", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_RANGE_SRC}", true, false,
							allowedSources);		
			propertyDefinitions.put(procRangeSource.getPropertyName(),
					procRangeSource);

			PropertyDefinition procRange = new PropertyDefinition("range",
					PropertyType.Double, 1000, "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_RANGE}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_RANGE}", true, false);
			procRange.setDependsOn("rangeSource=Constant");
			propertyDefinitions.put(procRange.getPropertyName(), procRange);

			PropertyDefinition procRangeEvent = new PropertyDefinition(
					"rangeEvent", PropertyType.String, "range",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_RANGE_FIELD}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_RANGE_FIELD}", true, false);
			procRangeEvent.setDependsOn("rangeSource=Event");
			propertyDefinitions.put(procRangeEvent.getPropertyName(),
					procRangeEvent);

			List<LabeledValue> unitsAllowedVals = new ArrayList<LabeledValue>();
			unitsAllowedVals.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.UNITS_METERS}","Meters"));
			unitsAllowedVals.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.UNITS_KM}","Kilometers"));
			unitsAllowedVals.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.UNITS_FT}","Feet"));
			unitsAllowedVals.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.UNITS_MILES}","Miles"));
			unitsAllowedVals.add(new
			LabeledValue("${com.esri.geoevent.solutions.processor.rf.rangefan-processor.UNITS_NM}","Nautical Miles"));
			
			PropertyDefinition procUnits = new PropertyDefinition("units",
					PropertyType.String, "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.UNITS_METERS}", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_UNITS}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_UNITS}", true, false, unitsAllowedVals);

			propertyDefinitions.put(procUnits.getPropertyName(), procUnits);

			PropertyDefinition procBearingSource = new PropertyDefinition(
					"bearingSource",
					PropertyType.String,
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_CONSTANT}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_BEARING_SRC}", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_BEARING_SRC}", true, false,
					allowedSources);
			propertyDefinitions.put(procBearingSource.getPropertyName(),
					procBearingSource);

			PropertyDefinition procBearingC = new PropertyDefinition("bearing",
					PropertyType.Double, 0, "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_BEARING}", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_BEARING}", true,
					false);
			procBearingC.setDependsOn("bearingSource=Constant");
			propertyDefinitions.put(procBearingC.getPropertyName(),
					procBearingC);

			PropertyDefinition procBearingEvent = new PropertyDefinition(
					"bearingEvent", PropertyType.String, "bearing",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_BEARING_FIELD}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_BEARING_FIELD}", true, false);
			procBearingEvent.setDependsOn("bearingSource=Event");
			propertyDefinitions.put(procBearingEvent.getPropertyName(),
					procBearingEvent);

			PropertyDefinition procTraversalSource = new PropertyDefinition(
					"traversalSource",
					PropertyType.String,
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.SRC_CONSTANT}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_TRAVERSAL_SRC}", "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_TRAVERSAL_SRC}", true,
					false, allowedSources);
			propertyDefinitions.put(procTraversalSource.getPropertyName(),
					procTraversalSource);

			PropertyDefinition procTraversal = new PropertyDefinition(
					"traversal", PropertyType.Double, 30, "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_TRAVERSAL}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_TRAVERSAL}", true, false);
			procTraversal.setDependsOn("traversalSource=Constant");
			propertyDefinitions.put(procTraversal.getPropertyName(),
					procTraversal);

			PropertyDefinition procTraversalEvent = new PropertyDefinition(
					"traversalEvent", PropertyType.String, "traversal",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_TRAVERSAL_FIELD}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_TRAVERSAL_FIELD}", true,
					false);
			procTraversalEvent.setDependsOn("traversalSource=Event");
			propertyDefinitions.put(procTraversalEvent.getPropertyName(),
					procTraversalEvent);

			PropertyDefinition procWKIDBuffer = new PropertyDefinition(
					"wkidbuffer", PropertyType.Integer, 3857, "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_PROC_WKID}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_PROC_WKID}", true, false);
			propertyDefinitions.put(procWKIDBuffer.getPropertyName(),
					procWKIDBuffer);

			PropertyDefinition procWKIDOut = new PropertyDefinition("wkidout",
					PropertyType.Integer, 4326, "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.LBL_OUTPUT_WKID}",
					"${com.esri.geoevent.solutions.processor.rf.rangefan-processor.DESC_OUTPUT_WKID}", true, false);
			propertyDefinitions.put(procWKIDOut.getPropertyName(), procWKIDOut);
		} catch (PropertyException e) {
			LOG.error(e.getMessage());
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

	}

	@Override
	public String getName() {
		return "RangeFanProcessor";
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
		return "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.PROC_LBL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.rf.rangefan-processor.PROC_DESC}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}
}
