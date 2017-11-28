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

import com.esri.map.ArcGISDynamicMapServiceLayer;

/**
 * A listener for geoprocessing tools.
 */
public interface GPListener {
    
    /**
     * Called when GP functionality becomes available.
     */
    public void gpEnabled();
    
    /**
     * Called when GP functionality becomes unavailable.
     */
    public void gpDisbled();
    
    /**
     * Called when GP calculation starts.
     */
    public void gpStarted();
    
    /**
     * Called when GP calculation ends, whether successful or not.
     * @param resultLayer the GP result layer, or null if the GP failed.
     */
    public void gpEnded(ArcGISDynamicMapServiceLayer resultLayer);
    
}
