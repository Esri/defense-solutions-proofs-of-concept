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
package com.esri.vehiclecommander.model;

import com.esri.map.Layer;
import java.awt.MediaTracker;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * A bean to contain a Layer object and other information, such as a thumbnail image,
 * that would go with a basemap layer.
 */
public class BasemapLayer {
    
    private final Layer layer;
    private final Icon thumbnail;

    public BasemapLayer(Layer layer, Icon thumbnail) {
        this.layer = layer;
        this.thumbnail = thumbnail;
    }
    
    public BasemapLayer(Layer layer, String thumbnailFilename) {
        this.layer = layer;
        if (null == thumbnailFilename) {
            thumbnail = null;
        } else {
            ImageIcon imageIcon = new ImageIcon(thumbnailFilename);
            thumbnail = MediaTracker.COMPLETE == imageIcon.getImageLoadStatus() ? imageIcon : null;
        }
    }

    /**
     * @return the layer
     */
    public Layer getLayer() {
        return layer;
    }

    /**
     * @return the thumbnail
     */
    public Icon getThumbnail() {
        return thumbnail;
    }
    
}
