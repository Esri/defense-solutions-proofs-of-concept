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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.property.Property;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class UpdateOnlyProcessor extends GeoEventProcessorBase
{
	// This class attempts to reduce the volume of output geoevents by filtering out events that have no updates
	// (i.e. the TIME_START tagged field is no later than last time a TRACK_ID was processed)
	// Use of this processor requires that incoming geoevent definitions has TRACK_ID and TIME_START tags applied to their fields
	
	protected static String CLEAR_CACHE_PROPERTY_NAME = "ClearCache";
	private static final Log LOG = LogFactory.getLog(UpdateOnlyProcessor.class);
	private Map<String, Date> trackCache = new HashMap<String, Date>();

	protected UpdateOnlyProcessor(GeoEventProcessorDefinition definition) throws ComponentException
	{
		super(definition);
	}

	@Override
	public GeoEvent process(GeoEvent geoEvent) throws Exception
	{
		String trackID = geoEvent.getTrackId();
		Date startTime = geoEvent.getStartTime();
		if (trackCache.containsKey(trackID))
		{
			Date lastTime = trackCache.get(trackID);
			// Filter out any tracks that haven't been updated since last time
			if (!startTime.after(lastTime))
			{
				LOG.trace("UpdateOnlyProcessor ignoring track as nothing new since last time: " + trackID + " : " + startTime.toString());
				return null;
			}
			LOG.trace("UpdateOnlyProcessor is handling new data for track " + trackID + " : " + startTime.toString() + " is more recent than " + lastTime.toString());
		}
		else
		{
			LOG.trace("UpdateOnlyProcessor is handling a new track: " + trackID + " : " + startTime.toString());
		}
		// If we've reached here, then either there's an update to a track in the cache, or there's a new track, so record it in the cache
		trackCache.put(trackID,  startTime);
		//... and allow the geoEvent through
		return geoEvent;
	}
	
	@Override
	public void afterPropertiesSet()
	{
		// If a user sets the "Clear Cache" property, then clear it, and un-set the property again
		// Users might want to clear the cache when another element later in the process chain has broken, or the outbound connector failed,
		// ... as this could mean the cache has a record of updates that never reached their final destination
		Boolean propValue = (Boolean)getProperty(CLEAR_CACHE_PROPERTY_NAME).getValue();
		if (propValue)
		{
			trackCache.clear();
			LOG.info("UpdateOnlyProcessor cache was cleared by user setting the Clear Cache property. Resetting to false...");
			getProperty(CLEAR_CACHE_PROPERTY_NAME).setValue(false);
		}
		super.afterPropertiesSet();
	}

	@Override
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(definition.getName());
		sb.append("/");
		sb.append(definition.getVersion());
		sb.append("[");
		for (Property p : getProperties())
		{
			sb.append(p.getDefinition().getPropertyName());
			sb.append(":");
			sb.append(p.getValue());
			sb.append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
}