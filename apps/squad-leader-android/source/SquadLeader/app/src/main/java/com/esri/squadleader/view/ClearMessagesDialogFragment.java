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
package com.esri.squadleader.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.squadleader.R;
import com.esri.squadleader.controller.AdvancedSymbolController;

/**
 * A dialog for clearing messages from the display.
 */
public class ClearMessagesDialogFragment extends DialogFragment {
    
    private static final String TAG = ClearMessagesDialogFragment.class.getSimpleName();
    
    /**
     * A listener for this class to pass objects back to the Activity that called it.
     */
    public interface ClearMessagesHelper {
        
        /**
         * Gives ClearMessagesDialogFragment a pointer to the AdvancedSymbolController.
         * @return the application's AdvancedSymbolController.
         */
        public AdvancedSymbolController getAdvancedSymbolController();
    }
    
    private ClearMessagesHelper listener = null;
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof ClearMessagesHelper) {
            listener = (ClearMessagesHelper) activity;
        }
    }
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null != listener) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            final View inflatedView = inflater.inflate(R.layout.clear_messages, null, false);
            builder.setView(inflatedView);
            builder.setTitle(getString(R.string.clear_messages));
            builder.setNegativeButton(R.string.cancel, null);
            ListView listView = (ListView) inflatedView.findViewById(R.id.listView_layerToClear);
            String[] layerNames = listener.getAdvancedSymbolController().getMessageLayerNames();
            String[] layerNamesPlusAll = new String[layerNames.length + 1];
            layerNamesPlusAll[0] = getString(R.string.all_layers);
            System.arraycopy(layerNames, 0, layerNamesPlusAll, 1, layerNames.length);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, layerNamesPlusAll);
            listView.setAdapter(adapter);
            final Dialog dialog = builder.create();
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long id) {
                    if (view instanceof TextView) {
                        boolean sendRemoveMessage = false;
                        try {
                            sendRemoveMessage = ((Checkable) inflatedView.findViewById(R.id.checkBox_sendRemoveMessage)).isChecked();
                        } catch (Throwable t) {
                            Log.e(TAG, null, t);
                        }
                        final String text = ((TextView) view).getText().toString();
                        if (getString(R.string.all_layers).equals(text)) {
                            listener.getAdvancedSymbolController().clearAllMessages(sendRemoveMessage);
                        } else {
                            listener.getAdvancedSymbolController().clearLayer(text, sendRemoveMessage);
                        }
                    }
                    
                    dialog.dismiss();
                }
            });
            return dialog;
        } else {
            return null;
        }
    }

}
