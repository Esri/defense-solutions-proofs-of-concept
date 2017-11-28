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

import com.esri.core.map.Graphic;

/**
 * An interface for classes that listen for events from the route tools.
 */
public interface RouteListener {
    
    /**
     * Called when a waypoint is added.
     * @param graphic the added waypoint graphic, whose ID may or may not be populated.
     * @param graphicUid the added waypoint graphic's ID.
     */
    void waypointAdded(Graphic graphic, int graphicUid);
    
    /**
     * Called when a waypoint is removed.
     * @param graphicUid the removed waypoint graphic's ID.
     */
    void waypointRemoved(int graphicUid);
    
    /**
     * Called when a waypoint is selected.
     * @param graphic the selected waypoint graphic.
     */
    void waypointSelected(Graphic graphic);
    
}
