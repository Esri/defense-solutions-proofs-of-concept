/*******************************************************************************
 * Copyright 2012-2014 Esri
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.esri.vehiclecommander.controller;

import com.esri.core.geometry.AngularUnit;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.symbol.TextSymbol;
import com.esri.militaryapps.controller.LocationListener;
import com.esri.militaryapps.model.Location;
import com.esri.militaryapps.model.LocationProvider;
import com.esri.vehiclecommander.util.Utilities;
import java.awt.Color;

/**
 * A layer for displaying the MGRS location to which the user navigated.
 */
public class MgrsLayerController extends GraphicsLayerController implements LocationListener {
    
    private static final int SYMBOL_SIZE = 14;    
    private static final SimpleMarkerSymbol POINT_SYMBOL = new SimpleMarkerSymbol(
            Color.RED, SYMBOL_SIZE, SimpleMarkerSymbol.Style.CIRCLE);
    
    private final Point currentGpsPointLatLon = new Point();
    private final TextSymbol textSymbol;
    private final TextSymbol haloSymbol;
    private final Object graphicUpdateLock = new Object();
    private final AppConfigController appConfigController;
    
    private int pointGraphicId = -1;
    private int labelGraphicId = -1;
    private int[] labelHaloGraphicIds = new int[4];
    private Point lastPointShownLatLon = null;
    
    public MgrsLayerController(
            MapController mapController,
            AppConfigController appConfig) {
        super(mapController, "MGRS");
        appConfigController = appConfig;
        textSymbol = new TextSymbol(SYMBOL_SIZE, "", Color.BLACK);
        textSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.CENTER);
        textSymbol.setVerticalAlignment(TextSymbol.VerticalAlignment.MIDDLE);
        haloSymbol = new TextSymbol(SYMBOL_SIZE, "", Color.WHITE);
        haloSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.CENTER);
        haloSymbol.setVerticalAlignment(TextSymbol.VerticalAlignment.MIDDLE);
        setOverlayLayer(true);
        if (null != mapController.getLocationController()) {
            mapController.getLocationController().addListener(this);
        }
    }
    
    /**
     * Tells the controller to display the given point.
     * @param pt the point to display.
     * @param sr the spatial reference of the point to display.
     */
    public void showPoint(Point pt, SpatialReference sr) {
        synchronized (graphicUpdateLock) {
            lastPointShownLatLon = (Point) GeometryEngine.project(pt, sr, Utilities.WGS84);
            if (-1 == pointGraphicId) {
                createPointGraphic(pt);
                createTextGraphics(pt);
            } else {
                updateGraphic(pointGraphicId, pt);
                updateTextGraphics(pt);
                updateTextGraphics(getDistanceBearingString(currentGpsPointLatLon, lastPointShownLatLon));
            }
        }
        
        setLayerVisibility(true);
        moveLayerToTop();
    }
    
    /**
     * Note: this method is not thread-safe unless you synchronize your call to it.
     * @param pt 
     */
    private void createPointGraphic(Point pt) {
        Graphic g = new Graphic(pt, POINT_SYMBOL);
        pointGraphicId = addGraphic(g);
    }
    
    /**
     * Note: this method is not thread-safe unless you synchronize your call to it.
     * @param pt 
     */
    private void createTextGraphics(Point pt) {
        String text = "";
        if (!currentGpsPointLatLon.isEmpty()) {
            text = getDistanceBearingString(currentGpsPointLatLon, lastPointShownLatLon);
        }
        //Loop for DIY halo
        haloSymbol.setText(text);
        int haloGraphicIndex = 0;
        for (float x = -2f; x <= 2f; x += 4f) {
            for (float y = -2f; y <= 2f; y += 4f) {
                haloSymbol.setOffsetX(x);
                haloSymbol.setOffsetY(y);
                Graphic g = new Graphic(pt, haloSymbol);
                labelHaloGraphicIds[haloGraphicIndex++] = addGraphic(g);
            }
        }

        textSymbol.setText(text);
        Graphic g = new Graphic(pt, textSymbol);
        labelGraphicId = addGraphic(g);
    }
    
    /**
     * Note: this method is not thread-safe unless you synchronize your call to it.
     * @param pt 
     */
    private void updateTextGraphics(Point pt) {
        for (int id : labelHaloGraphicIds) {
            updateGraphic(id, pt);
        }
        updateGraphic(labelGraphicId, pt);
    }
    
    /**
     * Note: this method is not thread-safe unless you synchronize your call to it.
     * @param text
     */
    private void updateTextGraphics(String text) {
        //Loop for DIY halo
        haloSymbol.setText(text);
        int haloGraphicIndex = 0;
        for (float x = -2f; x <= 2f; x += 4f) {
            for (float y = -2f; y <= 2f; y += 4f) {
                haloSymbol.setOffsetX(x);
                haloSymbol.setOffsetY(y);
                updateGraphic(labelHaloGraphicIds[haloGraphicIndex++], haloSymbol);
            }
        }

        textSymbol.setText(text);
        updateGraphic(labelGraphicId, textSymbol);
    }

    private String getDistanceBearingString(Point from, Point to) {
        double bearingDegrees = Utilities.calculateBearingDegrees(from, to);
        AngularUnit destUnit = Utilities.getAngularUnit(appConfigController.getHeadingUnits());
        AngularUnit degreesUnit = Utilities.getAngularUnit(AngularUnit.Code.DEGREE);
        long bearingInDestUnit = Math.round(bearingDegrees * degreesUnit.getConversionFactor(destUnit));
        
        long distance = Math.round(GeometryEngine.distance(
                GeometryEngine.project(from, Utilities.WGS84, mapController.getSpatialReference()),
                GeometryEngine.project(to, Utilities.WGS84, mapController.getSpatialReference()),
                mapController.getSpatialReference()));
        
        String ret = bearingInDestUnit + destUnit.getAbbreviation() + "\n\n"
                + distance + mapController.getSpatialReference().getUnit().getAbbreviation();
        return ret;
        
    }

    public void onLocationChanged(Location location) {
        synchronized (currentGpsPointLatLon) {
            currentGpsPointLatLon.setXY(location.getLongitude(), location.getLatitude());
            
            synchronized (graphicUpdateLock) {
                if (-1 != pointGraphicId) {
                    updateTextGraphics(getDistanceBearingString(currentGpsPointLatLon, lastPointShownLatLon));
                }
            }
        }
    }

    public void onStateChanged(LocationProvider.LocationProviderState state) {
        
    }
    
}
