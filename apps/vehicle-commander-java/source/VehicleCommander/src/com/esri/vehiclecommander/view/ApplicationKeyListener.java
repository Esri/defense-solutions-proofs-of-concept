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
package com.esri.vehiclecommander.view;

import com.esri.militaryapps.model.NavigationMode;
import com.esri.vehiclecommander.controller.MapController;
import com.esri.vehiclecommander.util.Utilities;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.Timer;

/**
 * A KeyListener for application-wide key events. This could be added to the application
 * frame or the JMap, for example.
 */
public class ApplicationKeyListener extends KeyAdapter {

    private final Frame frame;
    private final MapController mapController;
    private Timer rotateTimer = null;

    /**
     * Constructor that takes the application frame as a parameter.
     * @param frame The application frame
     */
    public ApplicationKeyListener(Frame frame, MapController mapController) {
        this.frame = frame;
        this.mapController = mapController;
    }

    /**
     * Performs certain actions for certain keys:
     * <ul>
     *     <li>Escape: close application</li>
     *     <li>V and B: cancel rotation</li>
     *     <li>N: clear rotation</li>
     * </ul>
     * @param e The key event
     */
    @Override
    public void keyReleased(KeyEvent e) {
        if (KeyEvent.VK_ESCAPE == e.getKeyCode()) {
            //Close application on escape
            Utilities.closeApplication(frame);
        } else if (KeyEvent.VK_V == e.getKeyCode() || KeyEvent.VK_B == e.getKeyCode()) {
            //Cancel rotation
            rotateTimer.stop();
        } else if (KeyEvent.VK_N == e.getKeyCode()) {
            mapController.setRotation(0);
            mapController.getLocationController().setNavigationMode(NavigationMode.NORTH_UP);
        }
    }

    /**
     * Performs certain actions for certain keys:
     * <ul>
     *     <li>V and B: rotate the map</li>
     * </ul>
     * @param e The key event
     */
    @Override
    public void keyPressed(final KeyEvent e) {
        if (KeyEvent.VK_V == e.getKeyCode() || KeyEvent.VK_B == e.getKeyCode()) {
            mapController.getLocationController().setNavigationMode(NavigationMode.NORTH_UP);
            if (null == rotateTimer || !rotateTimer.isRunning()) {
                //Start rotation
                rotateTimer = new Timer(1000 / 24, new ActionListener() {
                    public void actionPerformed(ActionEvent ae) {
                        double rotation = (KeyEvent.VK_V == e.getKeyCode() ? -360 : 360) / 12;
                        mapController.rotate(rotation);
                    }
                });
                rotateTimer.start();
            }
        }
    }

}
