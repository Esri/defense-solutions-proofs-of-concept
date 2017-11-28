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
 * An abstract implementation of MapOverlayListener for convenience. Override the
 * methods you need. Any methods not overridden do nothing.
 * @see MapController#trackAsync(com.esri.vehiclecommander.MapOverlayListener, short)
 */
public abstract class MapOverlayAdapter implements MapOverlayListener {

    public void mouseClicked(MouseEvent event) {
        
    }

    public void mousePressed(MouseEvent event) {
        
    }

    public void mouseReleased(MouseEvent event) {
        
    }
    
    public void mouseDragged(MouseEvent event) {
        
    }

}
