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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A bean containing map configuration details, typically read from XML by MapConfigReader.
 */
public class MapConfig {

    private List<BasemapLayer> layers = new ArrayList<BasemapLayer>();
    private final List<Map<String, String>> toolbarItems;
    
    /**
     * Instantiates a MapConfig with an empty list of toolbar items.
     */
    public MapConfig() {
        this.toolbarItems = new ArrayList<Map<String, String>>();
    }

    /**
     * Instantiates a MapConfig with a list of toolbar items.
     * @param toolbarItems the toolbar items. Each item in the list is a map of
     *                     key-value pairs used to instantiate a toolbar item.
     */
    public MapConfig(List<Map<String, String>> toolbarItems) {
        this.toolbarItems = toolbarItems;
    }

    /**
     * Returns the basemap layers contained by this MapConfig.
     * @return The basemap layers contained by this MapConfig.
     */
    public List<BasemapLayer> getBasemapLayers() {
        return layers;
    }

    /**
     * Sets this MapConfig's basemap layers.
     * @param layers the basemap layers to be stored by this MapConfig
     */
    public void setBasemapLayers(List<BasemapLayer> layers) {
        this.layers = layers;
    }
    
    /**
     * Returns the toolbar items as a list of key-value pairs.
     * @return the toolbar items.
     */
    public List<Map<String, String>> getToolbarItems() {
        return toolbarItems;
    }

}
