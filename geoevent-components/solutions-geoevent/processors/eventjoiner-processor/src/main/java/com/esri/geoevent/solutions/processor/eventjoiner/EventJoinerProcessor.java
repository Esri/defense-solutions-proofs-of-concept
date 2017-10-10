package com.esri.geoevent.solutions.processor.eventjoiner;

/*
 * #%L
 * FieldGrouperProcessor.java - fieldgrouper - Esri - 2013
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.ges.core.ConfigurationException;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.DefaultFieldDefinition;
import com.esri.ges.core.geoevent.DefaultGeoEventDefinition;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldException;
import com.esri.ges.core.geoevent.FieldGroup;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;
import com.esri.ges.core.geoevent.GeoEventPropertyName;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManager;
import com.esri.ges.manager.geoeventdefinition.GeoEventDefinitionManagerException;
import com.esri.ges.messaging.GeoEventCreator;
import com.esri.ges.messaging.Messaging;
import com.esri.ges.messaging.MessagingException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;

public class EventJoinerProcessor extends GeoEventProcessorBase {
	Messaging messaging;
	GeoEventDefinitionManager manager;
	Map<String, TrackRecord> recordCache;
	private static final Log LOG = LogFactory
			.getLog(EventJoinerProcessor.class);
	private String indefs;
	private String joinfield;
	private String outdefname;
	private List<String> defList;
	private Map<String, FieldItem> fldMgr;
	private final Double THRESHOLD_MILLISEC = 100000.0;
	private Boolean createNewDef;
	private GeoEventDefinition outDef = null;

	class TrackRecord {
		String id;
		List<HashMap<String, GeoEvent>> records = new ArrayList<HashMap<String, GeoEvent>>();

		public void setId(String id) {
			this.id = id;
		}
	}

	class FieldItem {
		String id;
		Integer count = 0;
		FieldType type;
		ArrayList<String> tags = new ArrayList<String>();

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public FieldType getType() {
			return type;
		}

		public void setType(FieldType type) {
			this.type = type;
		}

		public void appendTags(List<String> inTags) {

			for (String t : inTags) {
				if (!tags.contains(t)) {
					tags.add(t);
				}
			}
		}

		public ArrayList<String> getTags() {
			return tags;
		}

		public Integer getCount() {
			return count;
		}

		public void advance() {
			++count;
		}

	}

	public EventJoinerProcessor(GeoEventProcessorDefinition definition)
			throws ComponentException {
		super(definition);

	}

	public void setGeoEventDefinitionManager(GeoEventDefinitionManager gedm) {
		manager = gedm;
	}

	public void setMessaging(Messaging m) {
		messaging = m;
	}

	@Override
	public synchronized void  afterPropertiesSet() {
		indefs = properties.get("indefs").getValueAsString();
		String[] defs = indefs.split(",", 0);
		defList = Arrays.asList(defs);
		outdefname = properties.get("outdef").getValueAsString();
		joinfield = properties.get("join").getValueAsString();
		if(fldMgr!=null)
		{
			fldMgr.clear();
			fldMgr = null;
		}
		if(recordCache!=null)
		{
			recordCache.clear();
			recordCache = null;
		}
		
		createNewDef = true;
	}

	@Override
	public void shutdown() {
		if(fldMgr!=null)
		{
			fldMgr.clear();
			fldMgr = null;
		}
		if(recordCache!=null)
		{
			recordCache.clear();
			recordCache = null;
		}
		
		createNewDef = true;
		super.shutdown();
	}

	public GeoEvent process(GeoEvent evt) throws Exception {
		try {
			if(recordCache == null)
				recordCache = new HashMap<String, TrackRecord>();
			String curDefName = evt.getGeoEventDefinition().getName();

			if (!defList.contains(curDefName))
				return null;
			String uid = evt.getField(joinfield).toString();
			if (!recordCache.containsKey(uid)) {
				TrackRecord tr = new TrackRecord();
				tr.setId(uid);
				HashMap<String, GeoEvent> joinEvents = new HashMap<String, GeoEvent>();
				joinEvents.put(curDefName, evt);
				tr.records.add(joinEvents);
				recordCache.put(uid, tr);
			} else {
				TrackRecord tr = recordCache.get(uid);
				Boolean exitLoop = false;
				while (!exitLoop) {
					for (HashMap<String, GeoEvent> rec : tr.records) {
						if (!rec.containsKey(curDefName)) {
							rec.put(curDefName, evt);
							if (rec.size() == defList.size()) {
								if (createNewDef) {
									ConstructGeoEventDef(rec);
									createNewDef = false;
								}
								return CreateGeoEvent(rec);

							}
							exitLoop = true;
						}

					}
					exitLoop = true;

				}
				HashMap<String, GeoEvent> joinEvents = new HashMap<String, GeoEvent>();
				joinEvents.put(curDefName, evt);
				tr.records.add(joinEvents);
			}
			return null;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw (e);
		}
	}

	private void ConstructGeoEventDef(HashMap<String, GeoEvent> rec)
			throws Exception {
		try {
			fldMgr = new HashMap<String, FieldItem>();
			Set<String> keys = rec.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				String key = it.next();
				GeoEvent evt = rec.get(key);
				List<FieldDefinition> fldDefs = evt.getGeoEventDefinition()
						.getFieldDefinitions();
				for (FieldDefinition fldDef : fldDefs) {
					String fldName = fldDef.getName();
					FieldType type = fldDef.getType();
					if (fldMgr.containsKey(fldName)) {
						FieldItem item = fldMgr.get(fldName);
						if (!fldDef.getTags().isEmpty())
							item.appendTags(fldDef.getTags());
						item.advance();
					} else {
						FieldItem item = new FieldItem();
						item.setId(fldName);
						item.setType(type);
						if (!fldDef.getTags().isEmpty())
							item.appendTags(fldDef.getTags());
						item.advance();
						fldMgr.put(fldName, item);
					}
				}
			}
			Set<String> fldKeys = fldMgr.keySet();
			Iterator<String> fldIt = fldKeys.iterator();
			List<FieldDefinition> mergedFldDef = new ArrayList<FieldDefinition>();
			while (fldIt.hasNext()) {
				String fldName = fldIt.next();
				FieldItem item = fldMgr.get(fldName);
				FieldType type;
				FieldType fldtype = item.getType();
				ArrayList<String> tags = item.getTags();
				if (tags.contains("TRACK_ID")) {
					tags.remove("TRACK_ID");
				}
				FieldDefinition fd = null;
				if (item.getCount() > 1) {
					if (fldName.equals(joinfield)) {
						tags.add("JOIN_ID");
						tags.add("TRACK_ID");
						String[]tagarr = new String[tags.size()];
						tagarr = tags.toArray(tagarr);
						fd = new DefaultFieldDefinition(fldName, fldtype,
								(String[]) tagarr);
					} else {
						type = FieldType.Group;
						String[] groupedTag = { "GROUPED" };
						fd = new DefaultFieldDefinition(fldName, type,
								groupedTag);
						for (Integer i = 0; i < item.getCount(); ++i) {
							String childName = defList.get(i);
							FieldDefinition child = null;
							if (tags.isEmpty()) {
								child = new DefaultFieldDefinition(childName,
										fldtype);
							} else {
								String[]tagarr = new String[tags.size()];
								tagarr = tags.toArray(tagarr);
								child = new DefaultFieldDefinition(childName,
										fldtype, tagarr);
							}
							fd.addChild(child);
						}
					}
				} else {
					type = fldtype;
					if (!tags.isEmpty()) {
						fd = new DefaultFieldDefinition(fldName, fldtype,
								(String[]) tags.toArray());
					} else {
						fd = new DefaultFieldDefinition(fldName, fldtype);
					}

				}

				mergedFldDef.add(fd);
			}
			outDef = new DefaultGeoEventDefinition();
			outDef.setFieldDefinitions(mergedFldDef);
			outDef.setName(outdefname);
			outDef.setOwner(definition.getUri().toString());
			Collection<GeoEventDefinition> eventDefs = manager
					.searchGeoEventDefinitionByName(outdefname);
			Iterator<GeoEventDefinition> eventDefIt = eventDefs.iterator();
			while (eventDefIt.hasNext()) {
				GeoEventDefinition currentDef = eventDefIt.next();
				manager.deleteGeoEventDefinition(currentDef.getGuid());
			}
			manager.addGeoEventDefinition(outDef);
		} catch (ConfigurationException e) {
			LOG.error(e.getMessage());
			throw (e);
		} catch (GeoEventDefinitionManagerException e) {
			LOG.error(e.getMessage());
			throw (e);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw (e);
		}
	}

	private GeoEvent CreateGeoEvent(HashMap<String, GeoEvent> rec)
			throws Exception {
		try {
			GeoEventCreator creator = messaging.createGeoEventCreator();

			GeoEvent evt = creator.create(outDef.getGuid());
			Set<String> keys = rec.keySet();
			Iterator<String> it = keys.iterator();
			while (it.hasNext()) {
				GeoEvent ge = rec.get(it.next());
				List<FieldDefinition> fldDefs = ge.getGeoEventDefinition()
						.getFieldDefinitions();
				for (FieldDefinition fd : fldDefs) {
					String name = fd.getName();
					FieldItem item = fldMgr.get(name);
					if (item.getCount() > 1) {
						if(name.equals(joinfield))
						{
							evt.setField(joinfield, ge.getField(joinfield));
						}
						else
						{
							String gedname = ge.getGeoEventDefinition().getName();
							String groupName = name + "." + gedname;
							evt.setField(groupName, ge.getField(name));
						}
					} else {
						evt.setField(name, ge.getField(name));
					}
				}

			}
			evt.setProperty(GeoEventPropertyName.TYPE, "event");
			evt.setProperty(GeoEventPropertyName.OWNER_ID, getId());
			evt.setProperty(GeoEventPropertyName.OWNER_URI, definition.getUri());
			return evt;
		} catch (MessagingException e) {
			LOG.error(e.getMessage());
			throw (e);
		} catch (FieldException e) {
			LOG.error(e.getMessage());
			throw (e);
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw (e);
		}
	}
}
