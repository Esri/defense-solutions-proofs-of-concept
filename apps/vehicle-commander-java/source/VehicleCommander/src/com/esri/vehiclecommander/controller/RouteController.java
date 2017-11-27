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

import com.esri.core.geometry.Line;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import com.esri.core.map.Graphic;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;
import java.awt.Color;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * A controller to handle routing and navigation.
 */
public class RouteController extends GPController {
    
    private static final SimpleMarkerSymbol SYMBOL_WAYPOINT =
            new SimpleMarkerSymbol(new Color(0, 0, 255, 180), 20, SimpleMarkerSymbol.Style.CIRCLE);
    private static final SimpleMarkerSymbol SYMBOL_WAYPOINT_SELECTED =
            new SimpleMarkerSymbol(new Color(0, 0, 255, 180), 30, SimpleMarkerSymbol.Style.CIRCLE);
    private static final SimpleLineSymbol SYMBOL_ROUTE =
            new SimpleLineSymbol(new Color(0, 0, 255, 180), 5);
    
    private final ArrayDeque<Integer> graphicsAdded = new ArrayDeque<Integer>();
    private final ArrayDeque<Integer> waypointGraphicIds = new ArrayDeque<Integer>();
    private final ArrayList<RouteListener> routeListeners = new ArrayList<RouteListener>();
    
    private Point lastRoutePoint = null;
    private Polyline currentRouteLine = null;
    private int currentRouteLineId = -1;
    private int nextWaypointNumber = 1;
    private Integer selectedWaypointGraphicId = null;
    
    /**
     * Instantiates the RouteController.
     * @param mapController the MapController.
     */
    public RouteController(MapController mapController) {
        super(mapController, "Route");
        setOverlayLayer(true);
    }
    
    /**
     * Adds a RouteListener to this controller.
     * @param listener the listener to add.
     */
    public void addRouteListener(RouteListener listener) {
        synchronized (routeListeners) {
            routeListeners.add(listener);
        }
    }
    
    /**
     * Adds a waypoint to the route layer.
     * @param point the location of the new waypoint.
     */
    public void addWaypoint(Point point) {
        HashMap<String, Object> attributes = new HashMap<String, Object>();
        attributes.put("name", "Waypoint " + (nextWaypointNumber++));
        Graphic graphic = new Graphic(point, SYMBOL_WAYPOINT, attributes, 1);
        int graphicId = addGraphic(graphic);
        graphicsAdded.push(graphicId);
        waypointGraphicIds.push(graphicId);
        synchronized (routeListeners) {
            for (RouteListener listener : routeListeners) {
                listener.waypointAdded(graphic, graphicId);
            }
        }
    }
    
    /**
     * Adds a point to the current route line, and displays the line.
     * @param point the point to add to the current route line.
     */
    public void addPointToRouteLine(Point point) {
        if (null == currentRouteLine) {
            currentRouteLine = new Polyline();
        } else {
            Line segment = new Line();
            segment.setStart(lastRoutePoint);
            segment.setEnd(point);
            currentRouteLine.addSegment(segment, true);
            if (-1 == currentRouteLineId) {
                currentRouteLineId = addGraphic(new Graphic(currentRouteLine, SYMBOL_ROUTE));
                graphicsAdded.push(currentRouteLineId);
            } else {
                updateGraphic(currentRouteLineId, currentRouteLine);
            }
        }
        
        lastRoutePoint = point;
    }
    
    /**
     * Ends the current route line, so that the next call to addPointToRouteLine
     * will start a new route line.
     */
    public void endRouteLine() {
        currentRouteLine = null;
        currentRouteLineId = -1;
    }
    
    /**
     * Sets the currently selected waypoint graphid ID.
     * @param graphicId the currently selected waypoint graphid ID.
     */
    public void setSelectedWaypoint(Integer graphicId) {
        if (null != selectedWaypointGraphicId) {
            Graphic graphic = getGraphic(selectedWaypointGraphicId);
            updateGraphic(selectedWaypointGraphicId, SYMBOL_WAYPOINT);
        }
        selectedWaypointGraphicId = graphicId;
        Graphic graphic = getGraphic(graphicId);
        fireWaypointSelected(graphic);
        if (null != graphic) {
            updateGraphic(graphicId, SYMBOL_WAYPOINT_SELECTED);
        }
    }
    
    private void fireWaypointSelected(Graphic waypoint) {
        synchronized (routeListeners) {
            for (RouteListener listener : routeListeners) {
                listener.waypointSelected(waypoint);
            }
        }
    }
    
    private void fireWaypointRemoved(int graphicUid) {
        synchronized (routeListeners) {
            for (RouteListener listener : routeListeners) {
                listener.waypointRemoved(graphicUid);
            }
        }
    }
    
    /**
     * Removes all route lines and waypoints from the route layer.
     */
    public void clear() {
        synchronized (waypointGraphicIds) {
            for (int id : waypointGraphicIds) {
                fireWaypointRemoved(id);
            }
        }
        clearGraphics();
        graphicsAdded.clear();
        waypointGraphicIds.clear();
        nextWaypointNumber = 1;
    }
    
    /**
     * Removes the last waypoint or route line added to the route layer.
     */
    public void undo() {
        if (!graphicsAdded.isEmpty()) {
            int graphicId = graphicsAdded.pop();
            if (graphicId == waypointGraphicIds.peek()) {
                int id = waypointGraphicIds.pop();
                fireWaypointRemoved(id);
                nextWaypointNumber--;
            }
            removeGraphic(graphicId);
        }
    }
    
    /**
     * Returns a copy of the list of waypoints.
     * @return a copy of the list of waypoints.
     */
    public List<Graphic> getWaypoints() {
        ArrayList<Graphic> waypointsList = new ArrayList<Graphic>();
        Iterator<Integer> iterator = waypointGraphicIds.descendingIterator();
        while (iterator.hasNext()) {
            int id = iterator.next();
            Graphic graphic = getGraphic(id);
            waypointsList.add(graphic);
        }
        
        return waypointsList;
    }
    
}
