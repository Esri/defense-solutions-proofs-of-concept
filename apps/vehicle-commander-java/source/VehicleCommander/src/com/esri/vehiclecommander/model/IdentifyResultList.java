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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * An ordered list of IdentifyResult objects with references to the layers from which they
 * came.
 */
public class IdentifyResultList {
    
    private final ArrayList<IdentifiedItem> results = new ArrayList<IdentifiedItem>();
    private final HashMap<IdentifiedItem, Layer> resultToLayer = new HashMap<IdentifiedItem, Layer>();
    
    public void add(IdentifiedItem result, Layer layer) {
        results.add(result);
        resultToLayer.put(result, layer);
    }
    
    public int size() {
        return results.size();
    }
    
    public IdentifiedItem get(int index) {
        return results.get(index);
    }
    
    public Layer getLayer(IdentifiedItem result) {
        return resultToLayer.get(result);
    }
    
    public void clear() {
        results.clear();
        resultToLayer.clear();
    }
    
}
