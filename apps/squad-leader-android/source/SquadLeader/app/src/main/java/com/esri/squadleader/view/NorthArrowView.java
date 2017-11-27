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

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.esri.squadleader.controller.MapController;
import com.esri.squadleader.util.Utilities;

/**
 * A View that displays a north arrow according to the current rotation of the map.
 * After instantiating the view, call the setMapController(MapController) method so
 * that the north arrow will rotate with the map.<br/>
 * <br/>
 * IMPORTANT: be sure to call startRotation when the view is displayed and stopRotation
 * when the view is hidden.
 */
public class NorthArrowView extends ImageView {
    
    private final Handler handler = new Handler() {
        
        @Override
        public void handleMessage(Message msg) {
            float nextAngle = 360 - msg.getData().getFloat("rotation");
            Matrix matrix = new Matrix();
            setScaleType(ScaleType.MATRIX);
            matrix.postRotate(nextAngle, getDrawable().getBounds().width()/2, getDrawable().getBounds().height()/2);
            setImageMatrix(matrix);
        }
        
    };
    
    private final Timer timer = new Timer(true);
    private TimerTask timerTask = null;
    
    private MapController mapController = null;
    
    public NorthArrowView(Context context) {
        super(context);
    }

    public NorthArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    public NorthArrowView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    /**
     * Sets this north arrow's MapController so that it can rotate. Call startRotation
     * to start the rotation and stopRotation to stop it.
     * @param mapController the MapController.
     */
    public void setMapController(MapController mapController) {
        this.mapController = mapController;
    }
    
    /**
     * Starts rotating this view according to the map's rotation.
     */
    public void startRotation() {
        stopRotation();
        timerTask = new TimerTask() {
            
            @Override
            public void run() {
                if (null != mapController) {
                    Bundle bundle = new Bundle();
                    bundle.putFloat("rotation", (float) mapController.getRotation());
                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            }
            
        };
        timer.schedule(timerTask, 0, Utilities.ANIMATION_PERIOD_MS);
    }
    
    /**
     * Stops rotating this view. Ideally you should call stopRotation when the view will
     * no longer be displayed.
     */
    public void stopRotation() {
        if (null != timerTask) {
            timerTask.cancel();
        }
    }

}
