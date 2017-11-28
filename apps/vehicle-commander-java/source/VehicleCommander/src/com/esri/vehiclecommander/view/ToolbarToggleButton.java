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

import com.esri.vehiclecommander.controller.MapController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;
import javax.swing.JToggleButton;

/**
 * A toolbar button that can be toggled on and off (i.e. selected and unselected).
 * If you want instead a button that isn't toggled, use JButton. 
 */
public abstract class ToolbarToggleButton extends JToggleButton {
    
    protected JToggleButton unselectButton = null;
    private MapController mapController = null;
    private boolean itemStateChanged = false;
    private Map<String, String> properties = null;
    
    /**
     * Instantiates a ToolbarToggleButton.
     */
    protected ToolbarToggleButton() {
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusable(false);
        
        addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!itemStateChanged && null != unselectButton) {
                    unselectButton.setSelected(true);
                }
                toggled(isSelected());
                itemStateChanged = false;
            }
        });
        addItemListener(new ItemListener() {

            public void itemStateChanged(ItemEvent e) {
                itemStateChanged = true;
            }
        });
    }

    /**
     * Sets the toggle button that is to be selected when this button is unselected.
     * @param unselectButton the button that should be selected to unselect all
     *                       buttons in the button group. Pass null if you don't
     *                       need to "unselect" this button.
     */
    public void setUnselectButton(JToggleButton unselectButton) {
        this.unselectButton = unselectButton;
    }
    
    /**
     * Gives a MapController reference to this button.
     * @param mapController the map controller.
     */
    public void setMapController(MapController mapController) {
        this.mapController = mapController;
    }
    
    /**
     * Returns this button's MapController reference.
     * @return the map controller, or null if it has not been set.
     */
    public MapController getMapController() {
        return mapController;
    }
    
    /**
     * Called when this button is toggled. Implement this method to
     * make something happen when this button is toggled.
     * @param selected true if the button was selected; false otherwise.
     */
    protected abstract void toggled(boolean selected);

    /**
     * Returns any custom properties that have been set.
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }

    /**
     * Sets custom properties.
     * @param properties the properties to set
     */
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }
    
}
