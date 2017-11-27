/*******************************************************************************
 * Copyright 2013-2014 Esri
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
package com.esri.squadleader.model;

import java.net.URL;

import com.esri.android.map.Layer;

/**
 * A bean to contain a Layer object and other information, such as a thumbnail image,
 * that would go with a basemap layer.
 */
public class BasemapLayer {
    
    private final Layer layer;
    private final URL thumbnailUrl;//TODO change to an image or button object

    public BasemapLayer(Layer layer, URL thumbnailUrl) {
        this.layer = layer;
        this.thumbnailUrl = thumbnailUrl;
    }

    /**
     * @return the layer
     */
    public Layer getLayer() {
        return layer;
    }
    
}
