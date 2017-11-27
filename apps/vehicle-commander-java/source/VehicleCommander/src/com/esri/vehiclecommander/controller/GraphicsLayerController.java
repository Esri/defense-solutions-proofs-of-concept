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
import com.esri.core.map.Graphic;
import com.esri.core.symbol.Symbol;
import com.esri.map.GraphicsLayer;

/**
 * Creates a graphics layer and adds it to the map. You can extend this class as needed.
 */
public class GraphicsLayerController {

    protected final MapController mapController;
    private GraphicsLayer graphicsLayer;
    private final String layerName;

    private boolean isOverlay = false;
    
    /**
     * Creates a new GraphicsLayerController.
     * @param mapController the application's MapController.
     * @param layerName the name of the GraphicsLayer that this controller will
     *                  create and manage.
     */
    public GraphicsLayerController(MapController mapController, String layerName) {
        this.mapController = mapController;
        this.layerName = layerName;
    }

    private void verifyGraphicsLayer() {
        if (null == graphicsLayer || !mapController.hasLayer(graphicsLayer)) {
            graphicsLayer = new GraphicsLayer();
            graphicsLayer.setName(layerName);
            mapController.addLayer(graphicsLayer, isOverlay);
        }
    }

    /**
     * Returns true if this controller's GraphicsLayer has a graphic with the given ID.
     * @param graphicUid the graphic ID.
     * @return true if this controller's GraphicsLayer has a graphic with the given ID.
     */
    public boolean hasGraphic(int graphicUid) {
        return null != graphicsLayer.getGraphic(graphicUid);
    }
    
    /**
     * Returns the graphic with the specified ID, or null if there is no graphic with that ID.
     * @param graphicUid the graphic ID.
     * @return the graphic with the specified ID, or null if there is no graphic with that ID.
     *         Also returns null if graphicUid is null.
     */
    public Graphic getGraphic(Integer graphicUid) {
        return (null == graphicUid) ? null : graphicsLayer.getGraphic(graphicUid);
    }

    /**
     * Adds a graphic to this controller's GraphicsLayer.
     * @param graphic the new graphic.
     * @return the graphic ID.
     */
    public int addGraphic(Graphic graphic) {
        verifyGraphicsLayer();        
        return graphicsLayer.addGraphic(graphic);
    }

    /**
     * Moves an existing graphic to a new location.
     * @param graphicUid the graphic ID.
     * @param geom the new location.
     */
    public void updateGraphic(int graphicUid, Geometry geom) {
        verifyGraphicsLayer();
        graphicsLayer.updateGraphic(graphicUid, geom);
    }

    /**
     * Changes an existing graphic's symbol.
     * @param graphicUid the graphic ID.
     * @param symbol the new symbol.
     */
    public void updateGraphic(int graphicUid, Symbol symbol) {
        verifyGraphicsLayer();
        graphicsLayer.updateGraphic(graphicUid, symbol);
    }

    /**
     * Removes a graphic from this controller's GraphicsLayer.
     * @param graphicUid the graphic ID.
     */
    public void removeGraphic(int graphicUid) {
        verifyGraphicsLayer();
        graphicsLayer.removeGraphic(graphicUid);
    }
    
    /**
     * Sets whether the layer this controller manages should be an overlay layer.
     * @param isOverlayLayer true if the layer should be an overlay layer.
     */
    public void setOverlayLayer(boolean isOverlayLayer) {
        this.isOverlay = isOverlayLayer;
    }
    
    /**
     * Deletes all the graphics from the graphics layer.
     */
    public void clearGraphics() {
        verifyGraphicsLayer();
        graphicsLayer.removeAll();
    }
    
    /**
     * Moves the layer to the top of the map, i.e. to the end of the layer list.
     */
    public void moveLayerToTop() {
        verifyGraphicsLayer();
        mapController.moveLayer(graphicsLayer, Integer.MAX_VALUE);
    }
    
    /**
     * Sets this controller's graphics layer's visibility.
     * @param visible true if the layer should be visible, and false otherwise.
     */
    public void setLayerVisibility(boolean visible) {
        verifyGraphicsLayer();
        graphicsLayer.setVisible(visible);
    }

}
