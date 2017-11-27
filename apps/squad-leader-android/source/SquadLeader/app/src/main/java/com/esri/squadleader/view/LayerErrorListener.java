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
package com.esri.squadleader.view;

import android.content.Context;
import android.widget.Toast;

import com.esri.android.map.Layer;
import com.esri.android.map.event.OnStatusChangedListener;

/**
 * A listener that listens for layer initialization errors and provides UI feedback.
 */
public class LayerErrorListener implements OnStatusChangedListener {

    private static final long serialVersionUID = 2780740452655213139L;
    
    private final Context context;
    
    /**
     * Creates a new LayerErrorListener
     * @param context the Context used for displaying a Toast when layer initialization fails.
     */
    public LayerErrorListener(Context context) {
        this.context = context;
    }

    @Override
    public void onStatusChanged(Object source, STATUS status) {
        if (STATUS.INITIALIZATION_FAILED.equals(status)) {
            if (source instanceof Layer) {
                Layer layer = (Layer) source;
                Toast.makeText(context, "Error loading layer " + layer.getUrl() + ": " + status.getError().getDescription(), Toast.LENGTH_LONG).show();
            }
        }
    }    

}
