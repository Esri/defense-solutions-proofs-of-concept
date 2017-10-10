/*
 | Copyright 2013 Esri
 |
 | Licensed under the Apache License, Version 2.0 (the "License");
 | you may not use this file except in compliance with the License.
 | You may obtain a copy of the License at
 |
 |    http://www.apache.org/licenses/LICENSE-2.0
 |
 | Unless required by applicable law or agreed to in writing, software
 | distributed under the License is distributed on an "AS IS" BASIS,
 | WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 | See the License for the specific language governing permissions and
 | limitations under the License.
 */
package com.esri.geoevent.solutions.processor.symbollookup;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class SymbolIdToNameProcessorDefinition extends GeoEventProcessorDefinitionBase
{
	private GeoEventDefinitionManager manager = null;	
	
	public SymbolIdToNameProcessorDefinition()
	{
		;
	}

	public void setManager(GeoEventDefinitionManager m) throws PropertyException {
		this.manager = m;
		
		/*PropertyDefinition procSymbolIdSource = new PropertyDefinition("symbolIdSource", 
				PropertyType.String, "", "SymbolId Source", "Source of SymbolId Value", true, false);
		procSymbolIdSource.addAllowedValue("Event");
		propertyDefinitions.put(procSymbolIdSource.getPropertyName(), procSymbolIdSource);*/
		
		PropertyDefinition procSymbolIdEvent = new PropertyDefinition("symbolIdEvent", 
		PropertyType.String, "", "${com.esri.geoevent.solutions.processor.symbol-lookup.symbol-lookup-processor.LBL_SYMBOL_ID_FIELD}", "${com.esri.geoevent.solutions.processor.geomessage.geomessage-processor.DESC_SYMBOL_ID_FIELD}", true, false);
		//procSymbolIdEvent.setDependsOn("symbolIdSource=Event");

		propertyDefinitions.put(procSymbolIdEvent.getPropertyName(), procSymbolIdEvent);
		
		/*PropertyDefinition procSymbolNameSource = new PropertyDefinition("symbolNameSource", 
				PropertyType.String, "", "SymbolName Source", "Source of SymbolName Value", true, false);
		procSymbolNameSource.addAllowedValue("Event");
		propertyDefinitions.put(procSymbolNameSource.getPropertyName(), procSymbolNameSource);*/
		
		PropertyDefinition procSymbolNameEvent = new PropertyDefinition("symbolNameEvent", 
				PropertyType.String, "", "${com.esri.geoevent.solutions.processor.symbol-lookup.symbol-lookup-processor.LBL_SYMBOL_NAME_FIELD}", "${com.esri.geoevent.solutions.processor.geomessage.geomessage-processor.DESC_SYMBOL_NAME_FIELD}", true, false);
		//procSymbolNameEvent.setDependsOn("symbolNameSource=Event");

		propertyDefinitions.put(procSymbolNameEvent.getPropertyName(), procSymbolNameEvent);
		
	}
	
	
	
	@Override
	public String getName()
	{
		return "SymbolIdToNameProcessor";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.processor.symbol-lookup";
	}

	@Override
	public String getVersion()
	{
		return "10.5.0";
	}

	@Override
	public String getLabel()
	{
		return "${com.esri.geoevent.solutions.processor.symbol-lookup.symbol-lookup-processor.SYMBOL_TO_NAME_PROCESSOR_LABEL}";
	}

	@Override
	public String getDescription()
	{
		return "${com.esri.geoevent.solutions.processor.symbol-lookup.symbol-lookup-processor.SYMBOL_TO_NAME_PROCESSOR_DESCRIPTION}";
	}

	@Override
	public String getContactInfo()
	{
		return "geoeventprocessor@esri.com";
	}
}