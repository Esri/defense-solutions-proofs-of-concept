package com.esri.geoevent.solutions.processor.incrementalPoint;

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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.LabeledValue;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class IncrementalPointProcessorDefinition extends GeoEventProcessorDefinitionBase {
	
	private static final Log LOG = LogFactory
			.getLog(IncrementalPointProcessorDefinition.class);
	
	
	public IncrementalPointProcessorDefinition() throws PropertyException {

		
		List<LabeledValue> allowedIn = new ArrayList<LabeledValue>();
		allowedIn.add(new LabeledValue("${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.SEGMENTATION_TYPE_DISTANCE_LBL}","distance"));
		allowedIn.add(new LabeledValue("${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.SEGMENTATION_TYPE_TIME_LBL}","time"));
		allowedIn.add(new LabeledValue("${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.SEGMENTATION_TYPE_VERTEX_LBL}","vertex"));
		
		PropertyDefinition pdInterval= new PropertyDefinition("intervalType", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.LBL_INTERVAL_TYPE}", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.DESC_INTERVAL_TYPE}",true, false, allowedIn);
		propertyDefinitions.put(pdInterval.getPropertyName(), pdInterval);
		PropertyDefinition pdTimeInterval = new PropertyDefinition("timeinterval", PropertyType.Long, 600000, "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.LBL_TIME_INTERVAL}", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.DESC_TIME_INTERVAL}", false, false);
		pdTimeInterval.setDependsOn("intervalType=time");
		propertyDefinitions.put(pdTimeInterval.getPropertyName(),pdTimeInterval);
		PropertyDefinition pdDistInterval = new PropertyDefinition("distanceinterval", PropertyType.Double, 0.0, "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.LBL_DISTANCE_INTERVAL}", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.DESC_DISTANCE_INTERVAL}", false, false);
		pdDistInterval.setDependsOn("intervalType=distance");
		propertyDefinitions.put(pdDistInterval.getPropertyName(),pdDistInterval);
		PropertyDefinition pdwkid = new PropertyDefinition("wkid", PropertyType.Integer, "", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.LBL_PROCESS_WKID}", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.DESC_PROCESS_WKID}",true, false);
		propertyDefinitions.put(pdwkid.getPropertyName(), pdwkid);
		PropertyDefinition pdOutDef = new PropertyDefinition("outdefname", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.LBL_OUTDEFNAME}", "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.DESC_OUTDEFNAME}", true, false);
		propertyDefinitions.put(pdOutDef.getPropertyName(), pdOutDef);
	}

	
	

	@Override
	public String getName() {
		return "IncrementalPointProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.incrementalPoint";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.incrementalPoint.incrementalPoint-processor.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}
