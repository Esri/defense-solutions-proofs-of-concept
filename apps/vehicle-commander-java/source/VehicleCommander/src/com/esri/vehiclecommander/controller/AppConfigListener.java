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

/**
 * A listener for certain changes to the application configuration.
 */
public interface AppConfigListener {

    /**
     * Called when the "decorated" preference changes. A decorated application is
     * one with a title bar that can be moved, resized, maximized, etc.
     * @param isDecorated true if the preference was changed to decorated, or false
     *                    if the preference was changed to undecorated.
     */
    public void decoratedChanged(boolean isDecorated);
    
    /**
     * Called when the "showMessageLabels" preference changes. This preference
     * indicates whether labels will appear with military symbols.
     */
    public void showMessageLabelsChanged(boolean showMessageLabels);

}
