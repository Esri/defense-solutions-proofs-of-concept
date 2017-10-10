package com.esri.geoevent.solutions.processor.eventjoiner;

/*
 * #%L
 * FieldGrouperProcessorDefinition.java - fieldgrouper - Esri - 2013
 * org.codehaus.mojo-license-maven-plugin-1.5
 * $Id: update-file-header-config.apt.vm 17764 2012-12-12 10:22:04Z tchemit $
 * $HeadURL: https://svn.codehaus.org/mojo/tags/license-maven-plugin-1.5/src/site/apt/examples/update-file-header-config.apt.vm $
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




import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class EventJoinerProcessorDefinition extends
		GeoEventProcessorDefinitionBase {
	//GeoEventDefinitionManager manager;
	private static final Log LOG = LogFactory.getLog(EventJoinerProcessor.class);
	private String lblJoinField = "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.LBL_JOIN_FIELD}";
	private String descJoinField = "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.DESC_JOIN_FIELD}";
	private String lblOutDef = "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.LBL_OUT_DEF}";
	private String descOutDef = "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.DESC_OUT_DEF}";
	private String lblInDefs = "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.LBL_IN_DEFS}";
	private String descInDefs = "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.DESC_IN_DEFS}";
	public EventJoinerProcessorDefinition() throws PropertyException{
		PropertyDefinition pdJoin = new PropertyDefinition("join", PropertyType.String, "TRACK_ID", lblJoinField, descJoinField, true, false);
		propertyDefinitions.put(pdJoin.getPropertyName(), pdJoin);
		PropertyDefinition pdInDefs = new PropertyDefinition(
				"indefs",
				PropertyType.String,
				2, lblInDefs, 
				descInDefs,
				true, false);
		propertyDefinitions.put(pdInDefs.getPropertyName(), pdInDefs);

		PropertyDefinition pdOutDef = new PropertyDefinition(
				"outdef", PropertyType.String, "", lblOutDef,
				descOutDef, true, false);
		propertyDefinitions.put(pdOutDef.getPropertyName(),
				pdOutDef);
	}
	
	public String getName()
	{
		return "FieldGrouperProcessor";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.processor.fg";
	}

	@Override
	public String getVersion()
	{
		return "10.5.0";
	}

	@Override
	public String getLabel()
	{
		return "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.solutions.processor.eventjoiner.eventjoiner.PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo()
	{
		return "geoeventprocessor@esri.com";
	}
}
