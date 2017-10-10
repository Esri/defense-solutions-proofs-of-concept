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
package com.esri.geoevent.solutions.adapter.geomessage;

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.ges.adapter.AdapterDefinition;
import com.esri.ges.adapter.OutboundAdapterBase;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.FieldDefinition;
import com.esri.ges.core.geoevent.FieldType;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.geoevent.GeoEventDefinition;

public class DefenseOutboundAdapter extends OutboundAdapterBase {
	private static final Log LOG = LogFactory
			.getLog(DefenseOutboundAdapter.class);

	private Charset charset = Charset.forName("UTF-8");
	private String dateFormat = "yyyy-MM-dd HH:mm:ss";
	private SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
	private String messageType;

	public DefenseOutboundAdapter(AdapterDefinition definition)
			throws ComponentException {
		super(definition);
	}

	@SuppressWarnings("incomplete-switch")
	@Override
	public synchronized void receive(GeoEvent geoEvent) {

		ByteBuffer byteBuffer = ByteBuffer.allocate(10 * 1024);
		Integer wkid = -1;
		String message = "";

		message += "<geomessage v=\"1.0\">\n\r";
		message += "<_type>";
		message += messageType;
		message += "</_type>\n\r";
		message += "<_action>";
		message += "update";
		message += "</_action>\n\r";
		String messageid = UUID.randomUUID().toString();
		message += "<_id>";
		message += "{" + messageid + "}";
		message += "</_id>\n\r";
		MapGeometry geom = geoEvent.getGeometry();
		if (geom.getGeometry().getType() == com.esri.core.geometry.Geometry.Type.Point) {
			Point p = (Point) geom.getGeometry();
			message += "<_control_points>";
			message += ((Double) p.getX()).toString();
			message += ",";
			message += ((Double) p.getY()).toString();
			message += "</_control_points>\n\r";
			wkid = ((Integer) geom.getSpatialReference().getID());
		}

		if (wkid > 0) {
			String wkidValue = wkid.toString();
			message += "<_wkid>";
			message += wkidValue.toString();
			message += "</_wkid>\n\r";
		}
		GeoEventDefinition definition = geoEvent.getGeoEventDefinition();
		for (FieldDefinition fieldDefinition : definition.getFieldDefinitions()) {

			String attributeName = fieldDefinition.getName();
			Object value = geoEvent.getField(attributeName);

			if (value == null || value.equals("null")) {
				continue;
			}
			FieldType t = fieldDefinition.getType();
			if (t != FieldType.Geometry) {
				message += "<" + attributeName + ">";

				switch (t) {
				case String:
					// if(((String)value).isEmpty())
					// continue;
					message += value;
					break;
				case Date:
					Date date = (Date) value;
					message += (formatter.format(date));
					break;
				case Double:
					Double doubleValue = (Double) value;
					message += doubleValue.toString();
					break;
				case Float:
					Float floatValue = (Float) value;
					message += floatValue.toString();
					break;
				
				case Integer:
					Integer intValue = (Integer) value;
					message += intValue.toString();
					break;
				case Long:
					Long longValue = (Long) value;
					message += longValue.toString();
					break;
				case Short:
					Short shortValue = (Short) value;
					message += shortValue.toString();
					break;
				case Boolean:
					Boolean booleanValue = (Boolean) value;
					message += booleanValue.toString();
					break;

				}
				message += "</" + attributeName + ">\n\r";
			}
			else
			{
				if (definition.getIndexOf(attributeName) == definition
						.getIndexOf("GEOMETRY")) {
					continue;
				} else {
					String json = GeometryEngine.geometryToJson(wkid, (Geometry)value);
					message += "<" + attributeName + ">";
					message += json;
					message += "</" + attributeName + ">\n\r";
				}
				break;
			}

		}
		message += "</geomessage>";
		// stringBuffer.append("</geomessages>");
		message += "\r\n";

		ByteBuffer buf = charset.encode(message);
		if (buf.position() > 0)
			buf.flip();

		try {
			byteBuffer.put(buf);
		} catch (BufferOverflowException ex) {
			LOG.error("Csv Outbound Adapter does not have enough room in the buffer to hold the outgoing data.  Either the receiving transport object is too slow to process the data, or the data message is too big.");
		}
		byteBuffer.flip();
		super.receive(byteBuffer, geoEvent.getTrackId(), geoEvent);
		byteBuffer.clear();
	}

	@Override
	public void afterPropertiesSet() {
		messageType = properties.get("messagetype").getValueAsString();
	}
}