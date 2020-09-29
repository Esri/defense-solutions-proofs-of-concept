/*******************************************************************************
 * Copyright 2015 Esri
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
package com.esri.squadleader.controller;

import java.io.FileNotFoundException;

import android.graphics.Color;
import android.os.Build;

import com.esri.android.map.RasterLayer;
import com.esri.core.analysis.Viewshed;
import com.esri.core.geometry.Point;
import com.esri.core.renderer.Colormap;
import com.esri.core.renderer.Colormap.UniqueValue;
import com.esri.core.renderer.ColormapRenderer;

/**
 * A controller that calculates viewsheds based on an elevation raster. The Android OS must support an API level
 * greater than or equal to MIN_API_LEVEL to use this class.<br/>
 * <br/>
 * To make wise use of resources, call dispose() when you are done with this class. A good place to call dispose()
 * is the onDestroy() method of the Activity that uses this class.
 */
public class ViewshedController {
    
    private static final String TAG = ViewshedController.class.getSimpleName();
    
    /**
     * The minimum API level supported by this class. The constructor will check and throw an exception for lower
     * API levels.
     */
    public static final int MIN_API_LEVEL = 16;
    
    private final String elevationFilename;
    
    private Viewshed viewshed;
    private RasterLayer layer;    
    private double observerHeight = 2.0;
    private boolean started = false;

    /**
     * Instantiates a ViewshedController with an immutable elevation raster dataset. The caller does not need to call
     * start() in this case.
     * @param elevationFilename the full path to the elevation dataset to be used for viewshed analysis.
     *        This could be a TIF file, for example. The dataset should be in the same spatial reference
     *        as the MapView.
     * @throws RuntimeException if the elevation raster could not be opened and used for viewshed analysis, or if the
     *         Android version is too old for viewshed analysis.
     * @throws FileNotFoundException if elevationFilename represents a file that does not exist.
     * @throws IllegalArgumentException if elevationFilename is null or an empty string.
     */
    public ViewshedController(String elevationFilename) throws IllegalArgumentException, FileNotFoundException, RuntimeException {
        if (MIN_API_LEVEL > Build.VERSION.SDK_INT) {
            throw new RuntimeException(getClass().getSimpleName() + " not supported below Android API level " + MIN_API_LEVEL);
        }
        this.elevationFilename = elevationFilename;
        start();
    }
    
    /**
     * Gets the viewshed analysis and layer ready but does not add the layer to the map. The caller should add the
     * layer to the map after this method returns.
     * @throws IllegalArgumentException
     * @throws FileNotFoundException
     * @throws RuntimeException
     */
    private void start() throws IllegalArgumentException, FileNotFoundException, RuntimeException {
        if (!started) {
            viewshed = new Viewshed(elevationFilename);
            layer = new RasterLayer(viewshed.getOutputFunctionRasterSource());
            layer.setVisible(false);
            Colormap colormap = new Colormap();
            colormap.addUniqueValue(new UniqueValue(1, Color.rgb(76, 230, 0), "Visible"));
            ColormapRenderer renderer = new ColormapRenderer(colormap);
            layer.setRenderer(renderer);
            started = true;
        }
    }
    
    /**
     * Finalizes the controller by disposing the private Viewshed object and removing the viewshed layer from the map.
     */
    private void stop() {
        started = false;
        if (null != viewshed) {
            viewshed.dispose();
        }
    }
    
    /**
     * Disposes this controller, releasing the resources it uses. 
     */
    public void dispose() {
        stop();
    }
    
    /**
     * Returns the layer that displays viewshed results.
     * @return the layer that displays viewshed results.
     */
    public RasterLayer getLayer() {
        return layer;
    }
    
    /**
     * Returns the observer height used for viewshed analysis.
     * @return the observer height used for viewshed analysis.
     */
    public double getObserverHeight() {
        return observerHeight;
    }
    
    /**
     * Sets the observer height used for viewshed analysis.
     * @param observerHeight the observer height used for viewshed analysis.
     */
    public void setObserverHeight(double observerHeight) {
        this.observerHeight = observerHeight;
        if (null != viewshed) {
            viewshed.setObserverZOffset(observerHeight);
        }
    }
    
    /**
     * Calculates the viewshed based on an observer point.
     * @param observer the observer from which the viewshed will be calculated. This point must be
     *        in the spatial reference of the underlying elevation dataset, which should be in the
     *        same spatial reference as the MapView.
     */
    public void calculateViewshed(Point observer) {
        viewshed.setObserver(observer);
        layer.setVisible(true);
    }

}
