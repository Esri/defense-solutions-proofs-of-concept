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
package com.esri.vehiclecommander.view;

import java.awt.event.MouseEvent;

/**
 * An interface for classes that listen for MapOverlay events.
 * @see MapController
 * @see com.esri.map.MapOverlay
 */
public interface MapOverlayListener {

    /**
     * Called when the mouse is clicked on the map.
     * @param event the mouse click event.
     */
    public void mouseClicked(MouseEvent event);
    
    /**
     * Called when the mouse is pressed down on the map.
     * @param event the mouse press event.
     */
    public void mousePressed(MouseEvent event);
    
    /**
     * Called when the mouse is released on the map.
     * @param event the mouse release event.
     */
    public void mouseReleased(MouseEvent event);
    
    /**
     * Called when the mouse is dragged on the map.
     * @param event the mouse drag event.
     */
    public void mouseDragged(MouseEvent event);

}
