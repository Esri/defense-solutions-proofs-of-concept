/*******************************************************************************
 * Copyright 2013-2015 Esri
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.esri.squadleader.R;
import com.esri.squadleader.controller.MapController;

/**
 * A dialog for letting the user input an MGRS location and navigating to it.
 */
public class GoToMgrsDialogFragment extends DialogFragment {
    
    /**
     * A listener for this class to pass objects back to the Activity that called it.
     */
    public interface GoToMgrsHelper {
        
        /**
         * Gives GoToMgrsDialogFragment a pointer to the MapController.
         * @return the application's MapController.
         */
        public MapController getMapController();
        
        /**
         * Called when GoToMgrsDialog is about to perform the pan to the MGRS location.
         * This is useful for disabling Follow Me, for example.
         * @param mgrs the MGRS string.
         */
        void beforePanToMgrs(String mgrs);
        
        /**
         * Called if the attempt to pan to MGRS is unsuccessful. Most often, this happens
         * when the provided MGRS string is invalid.
         * @param mgrs the MGRS string, which is likely invalid.
         */
        void onPanToMgrsError(String mgrs);
    }
    
    private GoToMgrsHelper listener = null;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof GoToMgrsHelper) {
            listener = (GoToMgrsHelper) activity;
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null != listener) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final View inflatedView = inflater.inflate(R.layout.go_to_mgrs, null, false);
            builder.setView(inflatedView);
            builder.setTitle(getString(R.string.go_to_mgrs));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.go_to_mgrs, new DialogInterface.OnClickListener() {
                
                public void onClick(DialogInterface dialog, int which) {
                    View view = inflatedView.findViewById(R.id.editText_mgrs);
                    if (null != view && view instanceof EditText) {
                        String mgrs = ((EditText) view).getText().toString();
                        if (null != mgrs) {
                            listener.beforePanToMgrs(mgrs);
                            if (null == listener.getMapController().panTo(mgrs)) {
                                Toast.makeText(getActivity(), "Invalid MGRS string: " + mgrs, Toast.LENGTH_LONG).show();
                                listener.onPanToMgrsError(mgrs);
                            }
                        }
                    }
                }
            });
            return builder.create();
        } else {
            return null;
        }
    }

}
