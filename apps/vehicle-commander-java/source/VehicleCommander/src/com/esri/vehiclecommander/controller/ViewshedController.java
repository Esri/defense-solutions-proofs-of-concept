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

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polygon;
import com.esri.core.map.CallbackListener;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import com.esri.core.tasks.ags.geoprocessing.GPDouble;
import com.esri.core.tasks.ags.geoprocessing.GPFeatureRecordSetLayer;
import com.esri.core.tasks.ags.geoprocessing.GPJobResource;
import com.esri.core.tasks.ags.geoprocessing.GPJobResource.JobStatus;
import com.esri.core.tasks.ags.geoprocessing.GPLinearUnit;
import com.esri.core.tasks.ags.geoprocessing.GPParameter;
import com.esri.core.tasks.ags.geoprocessing.GPString;
import com.esri.map.ArcGISDynamicMapServiceLayer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages the creation and display of viewsheds.
 */
public class ViewshedController extends GPController {
    
    private static final SimpleFillSymbol VIEWSHED_BOUNDS_SYMBOL = new SimpleFillSymbol(
            new Color(255, 255, 255, 0),
            new SimpleLineSymbol(Color.RED, 3));
    private static final SimpleMarkerSymbol VIEWSHED_CENTER_SYMBOL =
            new SimpleMarkerSymbol(Color.RED, 10, SimpleMarkerSymbol.Style.X);
    
    private int viewshedBoundsGraphicId = -1;
    private int viewshedCenterGraphicId = -1;
    private String elevationPath = "C:/defensetemplates/vehiclecommander/GP/Elevation/Elevation.gdb/Elevation";
    private double observerHeight = 2.0;
    private String observerParamName = "Observer";
    private String observerHeightParamName = "ObserverHeight";
    private String radiusParamName = "Radius";
    private String elevationParamName = "Elevation";

    /**
     * Creates a new ViewshedController, which adds a graphics layer to the map.
     * @param mapController the MapController on which viewsheds will be displayed.
     */
    public ViewshedController(MapController mapController) {
        super(mapController, "Viewshed");
    }
    
    /**
     * Displays a temporary graphic on the map representing the bounds of the viewshed
     * that will be calculated if the given points are used.
     * @param viewshedCenter the center of the proposed viewshed (i.e. the observer).
     * @param viewshedOuter the outer point of the proposed viewshed's boundaries.
     * @return the distance between the two points.
     * @see #showViewshedBoundsGraphic(com.esri.core.geometry.Point, double)
     * @see #removeViewshedBoundsGraphic()
     */
    public double showViewshedBoundsGraphic(Point viewshedCenter, Point viewshedOuter) {
        double radius = GeometryEngine.distance(
                viewshedCenter,
                viewshedOuter,
                mapController.getSpatialReference());
        showViewshedBoundsGraphic(viewshedCenter, radius);
        return radius;
    }
    
    /**
     * Displays a temporary graphic on the map representing the bounds of the viewshed
     * that will be calculated if the given points are used.
     * @param viewshedCenter the center of the proposed viewshed (i.e. the observer).
     * @param radius the radius of the proposed viewshed.
     * @see #showViewshedBoundsGraphic(com.esri.core.geometry.Point, com.esri.core.geometry.Point)
     * @see #removeViewshedBoundsGraphic()
     */
    public void showViewshedBoundsGraphic(Point viewshedCenter, double radius) {
        Polygon buffer = GeometryEngine.buffer(
                viewshedCenter,
                mapController.getSpatialReference(),
                radius,
                mapController.getSpatialReference().getUnit());
        if (0 > viewshedBoundsGraphicId) {
            viewshedBoundsGraphicId = addGraphic(
                    new Graphic(buffer, VIEWSHED_BOUNDS_SYMBOL));
        } else {
            updateGraphic(viewshedBoundsGraphicId, buffer);
        }
    }
    
    /**
     * Removes the viewshed bounds graphic from the map.
     * @see #showViewshedBoundsGraphic(com.esri.core.geometry.Point, com.esri.core.geometry.Point)
     * @see #showViewshedBoundsGraphic(com.esri.core.geometry.Point, double)
     */
    public void removeViewshedBoundsGraphic() {
        removeGraphic(viewshedBoundsGraphicId);
        viewshedBoundsGraphicId = -1;
    }
    
    /**
     * Displays a graphic on the map representing the center of the viewshed.
     * @param viewshedCenter the center of the viewshed.
     * @see #removeViewshedCenterGraphic()
     */
    public void showViewshedCenterGraphic(Point viewshedCenter) {
        if (0 > viewshedCenterGraphicId) {
            viewshedCenterGraphicId = addGraphic(new Graphic(viewshedCenter, VIEWSHED_CENTER_SYMBOL));
        } else {
            updateGraphic(viewshedCenterGraphicId, viewshedCenter);
        }
    }
    
    /**
     * Removes the graphic on the map representing the center of the viewshed.
     * @see #showViewshedCenterGraphic(com.esri.core.geometry.Point)
     */
    public void removeViewshedCenterGraphic() {
        removeGraphic(viewshedCenterGraphicId);
        viewshedCenterGraphicId = -1;
    }
    
    /**
     * Calculates the viewshed based on the given parameters.
     * @param centerPoint the center of the viewshed.
     * @param radius the viewshed radius, in map units.
     */
    public void calculateViewshed(Point centerPoint, double radius) {
        try {
            fireGPStarted();
            ArrayList<GPParameter> parameters = new ArrayList<GPParameter>(); 

            Graphic centerGraphic = new Graphic(centerPoint, VIEWSHED_CENTER_SYMBOL);
            GPFeatureRecordSetLayer pointParam = new GPFeatureRecordSetLayer(observerParamName); 
            pointParam.setGeometryType(Geometry.Type.POINT); 
            pointParam.setSpatialReference(mapController.getSpatialReference()); 
            pointParam.addGraphic(centerGraphic); 

            GPLinearUnit radiusParam = new GPLinearUnit(radiusParamName); 
            radiusParam.setUnits("esri" + mapController.getSpatialReference().getUnit().toString()); 
            radiusParam.setDistance(radius); 

            GPDouble heightParam = new GPDouble(observerHeightParamName);
            heightParam.setValue(observerHeight);

            GPString rasterParam = new GPString(elevationParamName);
            rasterParam.setValue(elevationPath);

            parameters.add(pointParam); 
            parameters.add(radiusParam);
            parameters.add(heightParam);
//            parameters.add(rasterParam);//TODO add this back in if/when we want to pass a raster as a parameter
            getGeoprocessor().submitJobAsync(parameters, new CallbackListener<GPJobResource>() {

                public void onCallback(GPJobResource gpJobResource) {
                    //Display the raster as an overlay
                    JobStatus jobStatus = gpJobResource.getJobStatus();
                    ArcGISDynamicMapServiceLayer layer = null;
                    switch (jobStatus) {
                        case SUCCEEDED: {
                            layer = new ArcGISDynamicMapServiceLayer(getGeoprocessor().getUrl(), gpJobResource);
                            //Don't break
                        }

                        case CANCELLED:
                        case DELETED:
                        case FAILED:
                        case TIMED_OUT: {
                            fireGPEnded(layer);
                            break;
                        }
                        
                        default: {
                            try {
                                Thread.sleep(1000 / 24);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(ViewshedController.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            getGeoprocessor().getJobStatusAsync(this);
                        }
                    }
                }

                public void onError(Throwable e) {
                    fireGPEnded(null);
                    Logger.getLogger(ViewshedController.class.getName()).log(Level.SEVERE, null, e);
                }
            });
        } catch (Throwable t) {
            fireGPEnded(null);
            Logger.getLogger(ViewshedController.class.getName()).log(Level.SEVERE, null, t);
        }
    }

    /**
     * Gets the path to the elevation raster being used for viewshed analysis.
     * @return the elevation path.
     */
    public String getElevationPath() {
        return elevationPath;
    }

    /**
     * Sets the path to the elevation raster being used for viewshed analysis.
     * @param elevationPath the elevation path to set.
     */
    public void setElevationPath(String elevationPath) {
        this.elevationPath = elevationPath;
    }

    /**
     * @return the observerParamName
     */
    public String getObserverParamName() {
        return observerParamName;
    }

    /**
     * @param observerParamName the observerParamName to set
     */
    public void setObserverParamName(String observerParamName) {
        this.observerParamName = observerParamName;
    }

    /**
     * @return the observerHeightParamName
     */
    public String getObserverHeightParamName() {
        return observerHeightParamName;
    }

    /**
     * @param observerHeightParamName the observerHeightParamName to set
     */
    public void setObserverHeightParamName(String observerHeightParamName) {
        this.observerHeightParamName = observerHeightParamName;
    }

    /**
     * @return the radiusParamName
     */
    public String getRadiusParamName() {
        return radiusParamName;
    }

    /**
     * @param radiusParamName the radiusParamName to set
     */
    public void setRadiusParamName(String radiusParamName) {
        this.radiusParamName = radiusParamName;
    }

    /**
     * @return the elevationParamName
     */
    public String getElevationParamName() {
        return elevationParamName;
    }

    /**
     * @param elevationParamName the elevationParamName to set
     */
    public void setElevationParamName(String elevationParamName) {
        this.elevationParamName = elevationParamName;
    }

    /**
     * @return the observerHeight
     */
    public double getObserverHeight() {
        return observerHeight;
    }

    /**
     * @param observerHeight the observerHeight to set
     */
    public void setObserverHeight(double observerHeight) {
        this.observerHeight = observerHeight;
    }
    
}
