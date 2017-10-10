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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.MapGeometry;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.geometry.SpatialReference;
import com.esri.ges.core.component.ComponentException;
import com.esri.ges.core.geoevent.GeoEvent;
import com.esri.ges.core.validation.ValidationException;
import com.esri.ges.processor.GeoEventProcessorBase;
import com.esri.ges.processor.GeoEventProcessorDefinition;
import com.esri.core.geometry.Geometry;

public class RangeFanProcessor extends GeoEventProcessorBase {
	private static final Log LOG = LogFactory.getLog(RangeFanProcessor.class);
	//public GeoEventDefinitionManager manager;
	private SpatialReference srIn;
	private SpatialReference srBuffer;
	private SpatialReference srOut;
	private String rangeSource;
	private double rangeConstant;
	private String rangeEventFld;
	private String rangeUnits;
	private String bearingSource;
	private double bearingConstant;
	private String bearingEventFld;
	private String traversalSource;
	private double traversalConstant;
	private String traversalEventFld;
	private int inwkid;
	private int outwkid;
	private int bufferwkid;
	private String geosrc = "";
	private String geometryEventFld;
	private String xfield;
	private String yfield;

	public RangeFanProcessor(GeoEventProcessorDefinition definition) throws ComponentException {
		super(definition);
	}

	
	@Override
	public boolean isGeoEventMutator() {
		return true;
	}

	@Override
	public void afterPropertiesSet() {
		try {
			rangeSource = properties.get("rangeSource").getValue().toString();
			rangeConstant = (Double) properties.get("range").getValue();
			rangeEventFld = properties.get("rangeEvent").getValue().toString();
			rangeUnits = properties.get("units").getValue().toString();

			bearingSource = properties.get("bearingSource").getValue()
					.toString();
			bearingConstant = (Double) properties.get("bearing").getValue();
			bearingEventFld = properties.get("bearingEvent").getValue()
					.toString();

			traversalSource = properties.get("traversalSource").getValue()
					.toString();
			traversalConstant = (Double) properties.get("traversal").getValue();
			traversalEventFld = properties.get("traversalEvent").getValue()
					.toString();
			//inwkid = (Integer) properties.get("wkidin").getValue();
			outwkid = (Integer) properties.get("wkidout").getValue();
			bufferwkid = (Integer) properties.get("wkidbuffer").getValue();

			geosrc = properties.get("geosrc").getValueAsString();
			geometryEventFld = properties.get("geoeventfld").getValue()
					.toString();
			xfield = properties.get("xfield").getValueAsString();
			yfield = properties.get("yfield").getValueAsString();
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

	}

	@Override
	public void validate() throws ValidationException {
		super.validate();

		if (rangeConstant <= 0) {
			throw new ValidationException(
					"A constant range must be greater than 0");
		}

		if (bearingConstant < 0 || bearingConstant >= 360) {
			throw new ValidationException(
					"A constant bearing must be >= 0 and < 360");
		}

		if (traversalConstant < 0 || bearingConstant >= 360) {
			throw new ValidationException(
					"A constant traversal must be > 0 and < 360");
		}

		

		try {
			srBuffer = SpatialReference.create(bufferwkid);
		} catch (Exception e) {
			throw new ValidationException(
					"The spatial processing wkid is invalid");
		}

		try {
			srOut = SpatialReference.create(outwkid);
		} catch (Exception e) {
			throw new ValidationException("The output wkid is invalid");
		}
	}

	@Override
	public GeoEvent process(GeoEvent ge) throws Exception {

		try {
			if(!ge.getGeoEventDefinition().getTagNames().contains("GEOMETRY"))
			{
				return null;
			}
			srIn=ge.getGeometry().getSpatialReference();	
			inwkid = srIn.getID();
			double range;
			if (rangeSource.equals("Constant")) {
				range = rangeConstant;
			} else {
				range = (Double) ge.getField(rangeEventFld);
			}

			double bearing;

			if (bearingSource.equals("Constant")) {
				bearing = bearingConstant;
			} else {
				bearing = (Double) ge.getField(bearingEventFld);
			}

			double traversal;

			if (traversalSource.equals("Constant")) {
				traversal = traversalConstant;
			} else {
				traversal = (Double) ge.getField(traversalEventFld);
			}

			Point originGeo = null;
			if (geosrc.equals("event")) {
				MapGeometry mapGeo = ge.getGeometry();
				originGeo = (Point) mapGeo.getGeometry();
			}
			if (geosrc.equals("geodef")) {
				originGeo = (Point) ge.getField(geometryEventFld);
			}
			if (geosrc.equals("coord")) {
				Double ox = (Double) ge.getField(xfield);
				Double oy = (Double) ge.getField(yfield);
				originGeo = new Point(ox, oy, inwkid);
			}
			double x = originGeo.getX();
			double y = originGeo.getY();
			Geometry fan = constructRangeFan(x, y, range, rangeUnits, bearing,
					traversal);
			Geometry fanout = GeometryEngine.project(fan, srBuffer, srOut);
			MapGeometry outMapGeo = new MapGeometry(fanout, srOut);
			ge.setGeometry(outMapGeo);
			return ge;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw e;
		}
	}

	private Geometry constructRangeFan(double x, double y, double range,
			String unit, double bearing, double traversal) throws Exception {
		try {
			Polygon fan = new Polygon();
			Point center = new Point();
			center.setX(x);
			center.setY(y);

			Point centerProj = (Point) GeometryEngine.project(center, srIn,
					srBuffer);

			double centerX = centerProj.getX();
			double centerY = centerProj.getY();
			bearing = GeometryUtility.Geo2Arithmetic(bearing);
			double leftAngle = bearing - (traversal / 2);
			double rightAngle = bearing + (traversal / 2);
			int count = (int) Math.round(Math.abs(leftAngle - rightAngle));
			fan.startPath(centerProj);
			UnitConverter uc = new UnitConverter();
			range = uc.Convert(range, unit, srBuffer);
			for (int i = 0; i < count; ++i) {
				double d = Math.toRadians(leftAngle + i);
				double arcX = centerX + (range * Math.cos(d));
				double arcY = centerY + (range * Math.sin(d));
				Point arcPt = new Point(arcX, arcY);
				fan.lineTo(arcPt);
			}
			fan.closeAllPaths();
			return fan;
		} catch (Exception e) {
			LOG.error(e.getMessage());
			throw e;
		}
	}
}
