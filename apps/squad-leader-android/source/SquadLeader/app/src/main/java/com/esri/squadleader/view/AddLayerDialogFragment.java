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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.esri.militaryapps.model.LayerInfo;
import com.esri.militaryapps.model.LayerType;
import com.esri.militaryapps.model.RestServiceReader;
import com.esri.squadleader.R;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.net.URL;
import java.security.cert.CertPathValidatorException;

import javax.net.ssl.SSLHandshakeException;

/**
 * A dialog for adding a layer. Use this class instead of AddLayerFromWebDialogFragment.
 * @see AddLayerFromWebDialogFragment
 */
public class AddLayerDialogFragment extends DialogFragment implements View.OnClickListener {

    /**
     * A listener for this class to pass objects back to the Activity that called it.
     */
    public interface AddLayerListener {

        /**
         * Called when the add layer dialog retrieves valid layers based on the URL
         * specified by the user.
         * @param layerInfos the LayerInfo objects for the layers being returned. The
         *                   first layer on the list should display on top, the next layer
         *                   next until the last layer, which should display on the bottom.
         *                   Of course, you can use these LayerInfo objects in any order
         *                   desired.
         */
        public void onValidLayerInfos(LayerInfo[] layerInfos);
    }

    private static final String TAG = AddLayerDialogFragment.class.getSimpleName();
    private static final String EXTRA_USE_AS_BASEMAP = "UseAsBasemap";

    private int addLayerFromFileRequestCode = 31313;
    private AddLayerListener listener = null;
    private Activity activity = null;
    private View fragmentView = null;

    /**
     * Sets the request code for adding a layer from a file.
     * @param requestCode
     */
    public void setAddLayerFromFileRequestCode(int requestCode) {
        this.addLayerFromFileRequestCode = requestCode;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        if (activity instanceof AddLayerListener) {
            listener = (AddLayerListener) activity;
        }
    }

    private void addLayerFromWeb(final boolean useAsBasemap, final String urlString) {
        new AsyncTask<Void, Void, LayerInfo[]>() {

            @Override
            protected LayerInfo[] doInBackground(Void... params) {
                try {
                    return RestServiceReader.readService(new URL(urlString), useAsBasemap);
                } catch (final Exception e) {
                    Log.e(TAG, "Couldn't read and parse " + urlString, e);
                    if (e instanceof SSLHandshakeException) {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                boolean foundCpve = false;
                                Throwable cause = e;
                                while (null != cause && !foundCpve) {
                                    if (cause instanceof CertPathValidatorException) {
                                        foundCpve = true;
                                    } else {
                                        cause = cause.getCause();
                                    }
                                }
                                if (!foundCpve) {
                                    Toast.makeText(activity, "Couldn't add layer from web: " + e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(activity, "Couldn't add layer: Untrusted certificate for " + urlString, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                        return null;
                    } else {
                        activity.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                Toast.makeText(activity, "Couldn't add layer from web: " + e.getClass().getName() + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                        return null;
                    }
                }
            }

            @Override
            protected void onPostExecute(LayerInfo[] layerInfos) {
                if (null != layerInfos) {
                    listener.onValidLayerInfos(layerInfos);
                }
            };

        }.execute((Void[]) null);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (null != listener) {
            final Activity activity = getActivity();
            LayoutInflater inflater = activity.getLayoutInflater();
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final View inflatedView = inflater.inflate(R.layout.add_layer, null);
            fragmentView = inflatedView;

            inflatedView.findViewById(R.id.radioButton_fromFile).setOnClickListener(this);
            inflatedView.findViewById(R.id.radioButton_fromWeb).setOnClickListener(this);

            builder.setView(inflatedView);
            builder.setTitle(getString(R.string.add_layer));
            builder.setNegativeButton(R.string.cancel, null);
            builder.setPositiveButton(R.string.add_layer, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    if (((RadioButton) inflatedView.findViewById(R.id.radioButton_fromFile)).isChecked()) {
                        //Add from file
                        Intent getContentIntent = FileUtils.createGetContentIntent();
                        Intent intent = Intent.createChooser(getContentIntent, "Select a file");
                        getActivity().startActivityForResult(intent, addLayerFromFileRequestCode);
                    } else {
                        //Add from web
                        boolean useAsBasemap = false;
                        View checkboxView = inflatedView.findViewById(R.id.checkBox_basemap);
                        if (null != checkboxView && checkboxView instanceof CheckBox) {
                            useAsBasemap = ((CheckBox) checkboxView).isChecked();
                        }

                        View serviceUrlView = inflatedView.findViewById(R.id.editText_serviceUrl);
                        if (null != serviceUrlView && serviceUrlView instanceof EditText) {
                            final String urlString = ((EditText) serviceUrlView).getText().toString();
                            addLayerFromWeb(useAsBasemap, urlString);
                        }
                    }
                }
            });
            return builder.create();
        } else {
            return null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == addLayerFromFileRequestCode) {
            if (resultCode == Activity.RESULT_OK) {
                final Uri uri = data.getData();
                File file = new File(FileUtils.getPath(activity.getApplicationContext(), uri));
                LayerInfo[] layerInfos = new LayerInfo[1];
                layerInfos[0] = new LayerInfo();
                layerInfos[0].setDatasetPath(file.getAbsolutePath());
                final LayerType layerType = file.getAbsolutePath().toLowerCase().endsWith(".gpkg") ? LayerType.GEOPACKAGE
                        : file.getAbsolutePath().toLowerCase().endsWith(".shp") ? LayerType.SHAPEFILE
                        : LayerType.MIL2525C_MESSAGE;
                if (LayerType.GEOPACKAGE == layerType) {
                    // TODO ask the user if they want vectors, rasters, and/or editable.
                    layerInfos[0].setShowVectors(true);
                    layerInfos[0].setShowRasters(true);
                    layerInfos[0].setEditable(true);
                }
                layerInfos[0].setLayerType(layerType);
                layerInfos[0].setName(file.getName());
                layerInfos[0].setVisible(true);
                listener.onValidLayerInfos(layerInfos);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.radioButton_fromFile:
                fragmentView.findViewById(R.id.layout_fromWeb).setVisibility(View.GONE);
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setText(getText(R.string.choose_file));
                break;

            case R.id.radioButton_fromWeb:
                fragmentView.findViewById(R.id.layout_fromWeb).setVisibility(View.VISIBLE);
                ((AlertDialog) getDialog()).getButton(AlertDialog.BUTTON_POSITIVE).setText(getText(R.string.add_layer));
                break;
        }
    }
}
