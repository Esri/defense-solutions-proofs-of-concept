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

import com.esri.core.geometry.Point;
import com.esri.map.Layer;
import com.esri.vehiclecommander.model.IdentifiedItem;
import java.util.Map;

/**
 * Listens for identify operations to complete. ArcGIS Runtime has something
 * similar for a single IdentifyTask. IdentifyListener is designed to work for a
 * set of concurrent identify tasks.
 */
public interface IdentifyListener {

    /**
     * Called when a complete identify operation finishes, which may include the
     * completion of more than one IdentifyTask.
     * @param identifyPoint the point that was used for the identify operation,
     *                      for display purposes. Can be null.
     * @param results the combined results from all identify tasks.
     * @param resultToLayer a map of results to the layer from which each result comes.
     */
    public void identifyComplete(Point identifyPoint, IdentifiedItem[] results, Map<IdentifiedItem, Layer> resultToLayer);

}
