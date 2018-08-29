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

import java.util.ArrayList;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

/**
 * A toolbar button that populates and shows a sub-toolbar. Use this class's methods to specify the
 * buttons you want to appear in the sub-toolbar. Create the sub-toolbar as a JPanel
 * and add it to your UI. Then add this ToolbarOpeningButton to the main toolbar
 * (or wherever you want).
 */
public class ToolbarOpeningButton extends ToolbarToggleButton {
    
    private final ArrayList<AbstractButton> buttons = new ArrayList<AbstractButton>();
    private final JPanel toolbar;

    /**
     * Instantiates a ToolbarOpeningButton.
     * @param toolbar the sub-toolbar that this button populates and shows.
     */
    public ToolbarOpeningButton(JPanel toolbar, JToggleButton unselectButton) {
        super();
        setUnselectButton(unselectButton);
        this.toolbar = toolbar;
    }
    
    /**
     * Adds an action button that will be added to the toolbar when this ToolbarOpeningButton
     * is toggled on.
     * @param button an action button that will be added to the toolbar when this ToolbarOpeningButton
     * is toggled on.
     */
    public void addActionButton(JButton button) {
        buttons.add(button);
    }
    
    /**
     * Adds a ComponentShowingButton that will be added to the toolbar when this ToolbarOpeningButton
     * is toggled on.
     * @param button a ComponentShowingButton that will be added to the toolbar when this ToolbarOpeningButton
     * is toggled on.
     */
    public void addToggleButton(ComponentShowingButton button) {
        buttons.add(button);
    }

    /**
     * Called when this button is toggled, and opens the toolbar.
     * @param selected true if the button was selected; false otherwise.
     */
    @Override
    protected void toggled(boolean selected) {
        toolbar.removeAll();
        if (selected) {
            for (AbstractButton button : buttons) {
                toolbar.add(button);
            }
        }
        toolbar.setVisible(selected);
    }
    
}
