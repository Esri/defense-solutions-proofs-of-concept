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

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JLayeredPane;
import javax.swing.RootPaneContainer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A toolbar button that shows a Component when toggled.
 */
public class ComponentShowingButton extends ToolbarToggleButton {
    
    private Component component = null;
    private Container componentParent = null;

    /**
     * Instantiates the button.
     */
    public ComponentShowingButton() {
        super();
        
        addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                if (!isSelected() && null != component) {
                    component.setVisible(false);
                }
            }
        });
    }
    
    /**
     * Sets the component to be shown.
     * @param component the component to be shown.
     * @param parent the container to which the shown component should be added.
     *               This parameter can be null if you add the component yourself.
     */
    public void setComponent(Component component) {
        this.component = component;
        if (null != component) {
            component.addComponentListener(new ComponentAdapter() {

                @Override
                public void componentHidden(ComponentEvent e) {
                    if (null != unselectButton && isSelected()) {
                        unselectButton.setSelected(true);
                    }
                }
                
            });
        }
    }
    
    /**
     * Sets the container to which the shown component should be added.
     * @param parent the container to which the shown component should be added.
     *               This parameter can be null if you add the component yourself.
     */
    public void setComponentParent(Container parent) {
        this.componentParent = parent;
    }

    /**
     * Called when this button is toggled, and shows or hides the component.
     * @param selected true if the button was selected; false otherwise.
     */
    @Override
    protected void toggled(boolean selected) {
        if (null != component) {
            if (selected) {
                //If the component hasn't been inserted, insert it
                if (null == component.getParent()) {
                    if (null != componentParent) {
                        if (componentParent instanceof RootPaneContainer) {
                            ((RootPaneContainer) componentParent).getLayeredPane().add(component, JLayeredPane.MODAL_LAYER);
                        } else {
                            componentParent.add(component);
                        }
                    }
                }

                //Position the component
                if (null != getParent()) {
                    Point location = getParent().getLocation();
                    location.y += getParent().getHeight() + 5;
                    component.setLocation(location);
                }
            }

            component.setVisible(selected);
        }
    }
    
}
