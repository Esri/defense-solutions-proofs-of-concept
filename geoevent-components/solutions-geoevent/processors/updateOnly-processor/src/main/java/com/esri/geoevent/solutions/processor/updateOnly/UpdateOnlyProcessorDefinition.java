/*
 | Copyright 2014 Esri
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
package com.esri.geoevent.solutions.processor.updateOnly;

import com.esri.ges.core.property.PropertyDefinition;
import com.esri.ges.core.property.PropertyException;
import com.esri.ges.core.property.PropertyType;
import com.esri.ges.processor.GeoEventProcessorDefinitionBase;

public class UpdateOnlyProcessorDefinition extends GeoEventProcessorDefinitionBase
{
	protected static final String CLEAR_CACHE_PROPERTY = "ClearCache";
	
	public UpdateOnlyProcessorDefinition()
	{
		try
		{
			// When defining properties, first create the property definition.
			// The first parameter is the internal name of the property and is not exposed to the user.
			// The second parameter is the property type, and allows the server to validate property values provided by the user (e.g. "hello" is not a valid integer value).
			// The third parameter is the default value, and is shown to the user as a suggested value.  This is optional.
			// The fourth parameter is the name shown to users when the look at a list of properties for your processor.
			// The fifth parameter is the Detailed description, which is shown to users when they hover over the property.
			// The sixth parameter specifies if the property is mandatory (must be provided by the user)
			// The seventh parameter specifies if the property is read-only, meaning it is shown to the user but cannot be modified.
			// Additional parameters can be added to the constructor as a list of allowed values.
			PropertyDefinition clearCacheProperty = new PropertyDefinition( CLEAR_CACHE_PROPERTY, PropertyType.Boolean, false, "Clear Cache", "Set this property to clear the cache. For example, if anything has gone wrong in GeoEvent Processing, such that outputs have not been reached their destination(s), then this Processor may prevent those outputs from being re-processed immediately unless the cache is cleared. ", false, false );
			// after creating the property definition, put it in the base class's "propertyDefinitions" structure.  These definitions will be provided to the
			// server when the processor is installed, and provided to client applications on-demand
			propertyDefinitions.put( CLEAR_CACHE_PROPERTY, clearCacheProperty );
		}
		catch (PropertyException e)
		{
			;
		}
	}

	@Override
	public String getName()
	{
		return "UpdateOnlyProcessor";
	}

	@Override
	public String getDomain()
	{
		return "com.esri.geoevent.solutions.processor.updateOnly";
	}

	@Override
	public String getVersion()
	{
		return "10.5.0";
	}

	@Override
	public String getLabel()
	{
		return "UpdateOnly Processor";
	}

	@Override
	public String getDescription()
	{
		return "This processor keeps track of the TRACK_ID and TIME_START tagged fields and filters out those that have not updated since last passed through. It may be useful where scheduled updates are received regardless of whether new data is available, and you want to filter out events about which you already know. For example, this could help to reduce the number of updates being made to a Feature Service, and hence reduce the pressure on ArcGIS Server resources.";
	}

	@Override
	public String getContactInfo()
	{
		return "geoeventprocessor@esri.com";
	}
}