/*******************************************************************************
 * Copyright 2015 Esri
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

import com.esri.android.map.Layer;

import java.util.HashMap;

/**
 * An ordered list of IdentifyResult objects with references to the layers from which they
 * came.
 */
public class IdentifyResultList extends com.esri.militaryapps.model.IdentifyResultList {

    private final HashMap<IdentifiedItem, Layer> resultToLayer = new HashMap<IdentifiedItem, Layer>();

    /**
     * Adds a result and maps it to a layer.
     * @param result the result to add.
     * @param layer the layer to which the result should be mapped.
     */
    public void add(IdentifiedItem result, Layer layer) {
        addResult(result);
        resultToLayer.put(result, layer);
    }

    /**
     * Adds a result and maps it to a layer.
     * @param item the result to add.
     * @param layer the layer to which the result is mapped.
     * @throws ClassCastException if the item is not a Squad Leader IdentifiedItem or if the layer is
     *                            not an ArcGIS Runtime for Android Layer.
     */
    @Override
    public void add(com.esri.militaryapps.model.IdentifiedItem item, Object layer) throws ClassCastException {
        add((IdentifiedItem) item, (Layer) layer);
    }

    @Override
    protected void clearResultToLayer() {
        resultToLayer.clear();
    }

    /**
     * @param item the item.
     * @return
     * @throws ClassCastException if the item is not a Squad Leader IdentifiedItem.
     */
    @Override
    public Object getLayer(com.esri.militaryapps.model.IdentifiedItem item) throws ClassCastException {
        return getLayer((IdentifiedItem) item);
    }

    public Layer getLayer(IdentifiedItem item) {
        return resultToLayer.get(item);
    }

}
