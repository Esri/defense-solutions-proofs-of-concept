package com.esri.geoevent.solutions.processor.line2pt;

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

public class Line2PtProcessorDefinition extends GeoEventProcessorDefinitionBase {
	
	private static final Log LOG = LogFactory
			.getLog(Line2PtProcessorDefinition.class);
	
	
	public Line2PtProcessorDefinition() throws PropertyException {
		List<LabeledValue> allowedIn = new ArrayList<LabeledValue>();
		allowedIn.add(new LabeledValue("${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.STARTPOINT_LBL}","start"));
		allowedIn.add(new LabeledValue("${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.ENDPOINT_LBL}","end"));
		allowedIn.add(new LabeledValue("${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.MIDPOINT_LBL}","mid"));
		
		PropertyDefinition pdPoint = new PropertyDefinition("pointType", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.LBL_POINT_TYPE}", "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.DESC_POINT_TYPE}",true, false, allowedIn);
		propertyDefinitions.put(pdPoint.getPropertyName(), pdPoint);
		PropertyDefinition pdwkid = new PropertyDefinition("wkid", PropertyType.Integer, "", "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.LBL_PROCESS_WKID}", "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.DESC_PROCESS_WKID}",true, false);
		propertyDefinitions.put(pdwkid.getPropertyName(), pdwkid);
		PropertyDefinition pdOutDef = new PropertyDefinition("outdefname", PropertyType.String, "", "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.LBL_OUTDEFNAME}", "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.DESC_OUTDEFNAME}", true, false);
		propertyDefinitions.put(pdOutDef.getPropertyName(), pdOutDef);
	}

	
	

	@Override
	public String getName() {
		return "Line2PtProcessor";
	}

	@Override
	public String getDomain() {
		return "com.esri.geoevent.solutions.processor.line2pt";
	}

	@Override
	public String getVersion() {
		return "10.5.0";
	}

	@Override
	public String getLabel() {
		return "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription() {
		return "${com.esri.geoevent.solutions.processor.line2pt.line2pt-processor.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo() {
		return "geoeventprocessor@esri.com";
	}

}
