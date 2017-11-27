/*******************************************************************************
 * Copyright 2013-2017 Esri
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
package com.esri.squadleader.controller;

import android.util.Log;

import com.esri.militaryapps.controller.MessageControllerListener;
import com.esri.militaryapps.model.Geomessage;

/**
 * A listener that receives messages and adds them to a graphics layer.
 */
public class MessageListener implements MessageControllerListener {

    private static final String TAG = MessageListener.class.getSimpleName();

    private final AdvancedSymbolController advancedSymbolController;

    /**
     * Instantiates a MessageListener that will add symbols to the specified AdvancedSymbolController.
     *
     * @param advancedSymbolController the AdvancedSymbolController to which symbols will be added
     *                                 when messages are received.
     */
    public MessageListener(AdvancedSymbolController advancedSymbolController) {
        this.advancedSymbolController = advancedSymbolController;
        if (null == advancedSymbolController) {
            Log.w(TAG, "MessageListener received a null AdvancedSymbolController. That means this MessageListener won't do much.");
        }
    }

    @Override
    public void geomessageReceived(Geomessage geomessage) {
        if (null != advancedSymbolController) {
            advancedSymbolController.handleGeomessage(geomessage);
        }
    }

    @Override
    public void datagramReceived(String contents) {
    }

}
