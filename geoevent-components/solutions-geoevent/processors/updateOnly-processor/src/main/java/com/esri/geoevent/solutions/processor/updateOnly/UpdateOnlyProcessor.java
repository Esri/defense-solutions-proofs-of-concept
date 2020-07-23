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

import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.property.Property;
import com.esri.ges.framework.i18n.BundleLogger;
import com.esri.ges.framework.i18n.BundleLoggerFactory;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class UpdateOnlyProcessor extends GeoEventProcessorBase
{
  private static final BundleLogger LOGGER     = BundleLoggerFactory.getLogger(UpdateOnlyProcessor.class);

  // This class attempts to reduce the volume of output geoevents by filtering out events that have no updates
  // (i.e. the TIME_START tagged field is no later than last time a TRACK_ID was processed)
  // Use of this processor requires that incoming geoevent definitions has TRACK_ID and TIME_START tags applied to their
  // fields

  private Map<String, Date>         trackCache = new HashMap<String, Date>();

  protected UpdateOnlyProcessor(GeoEventProcessorDefinition definition) throws ComponentException
  {
    super(definition);
    LOGGER.trace("Constructed UpdatesOnly Processor: {0}", this);
  }

  @Override
  public GeoEvent process(GeoEvent geoEvent) throws Exception
  {
    if (geoEvent != null)
    {
      String trackID = geoEvent.getTrackId();
      Date startTime = geoEvent.getStartTime();
      if (trackID != null && startTime != null)
      {
        if (trackCache.containsKey(trackID))
        {
          Date lastTime = trackCache.get(trackID);
          // Filter out any tracks that haven't been updated since last time
          if (!startTime.after(lastTime))
          {
            LOGGER.trace("UpdateOnlyProcessor ignoring track as nothing new since last time {0}: {1}", trackID, startTime);
            return null;
          }
          LOGGER.trace("UpdateOnlyProcessor is handling new data for track {0}: {1}", trackID, startTime.toString() + " is more recent than " + lastTime);
        }
        else
        {
          LOGGER.trace("UpdateOnlyProcessor is handling a new track {0}: {1}", trackID, startTime);
        }
        // Either there's an update to a track in the cache, or there's a new track, record it in the cache
        trackCache.put(trackID, startTime);
        // ... and allow the geoEvent through
      }
      else if (LOGGER.isInfoEnabled())
      {
        LOGGER.info("Event TRACK_ID={0} and TIME_START={1}. Check that the GeoEvent Defintion {2} has tags for TRACK_ID and TIME_START set: {3}", trackID, startTime, geoEvent.getGeoEventDefinition().getName(), geoEvent);
      }
    }
    else
    {
      LOGGER.trace("Event is NULL, nothing to process.");
    }
    return geoEvent;

  }

  @Override
  public void afterPropertiesSet()
  {
    // If a user sets the "Clear Cache" property, then clear it, and un-set the property again
    // Users might want to clear the cache when another element later in the process chain has broken, or the outbound
    // connector failed,
    // ... as this could mean the cache has a record of updates that never reached their final destination
    Boolean propValue = (Boolean) getProperty(UpdateOnlyProcessorDefinition.CLEAR_CACHE_PROPERTY).getValue();
    if (propValue)
    {
      trackCache.clear();
      LOGGER.info("UpdateOnlyProcessor cache was cleared by user setting the Clear Cache property. Resetting to false: {0}", this);
      getProperty(UpdateOnlyProcessorDefinition.CLEAR_CACHE_PROPERTY).setValue(false);
    }
    super.afterPropertiesSet();
    LOGGER.debug("UpdateOnlyProcessor after properties set: {0}", this);
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
