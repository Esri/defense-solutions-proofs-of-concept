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

import com.esri.client.local.GPServiceType;
import com.esri.client.local.LocalGeoprocessingService;
import com.esri.client.local.LocalServiceStartCompleteEvent;
import com.esri.client.local.LocalServiceStartCompleteListener;
import com.esri.client.local.LocalServiceStatus;
import com.esri.client.local.LocalServiceStopCompleteEvent;
import com.esri.client.local.LocalServiceStopCompleteListener;
import com.esri.core.io.UserCredentials;
import com.esri.core.tasks.ags.geoprocessing.Geoprocessor;
import com.esri.map.ArcGISDynamicMapServiceLayer;
import java.io.File;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An abstract class for controllers that run geoprocessing.
 */
public abstract class GPController extends GraphicsLayerController {
    private Geoprocessor geoprocessor = null;
    protected final LocalGeoprocessingService gpService;
    protected final HashSet<GPListener> listeners = new HashSet<GPListener>();
    private String taskName = null;

    public GPController(MapController mapController, String layerName) {
        super(mapController, layerName);
        gpService = new LocalGeoprocessingService();
        gpService.setServiceType(GPServiceType.SUBMIT_JOB_WITH_MAP_SERVER_RESULT);
        gpService.addLocalServiceStartCompleteListener(new LocalServiceStartCompleteListener() {

            public void localServiceStartComplete(LocalServiceStartCompleteEvent e) {
                if (LocalServiceStatus.STARTED.equals(gpService.getStatus())) {
                    String url = gpService.getUrlGeoprocessingService() + "/" + taskName;
                    geoprocessor = new Geoprocessor(url, new UserCredentials());
                    fireGPEnabled();
                }
            }
        });
    }

    /**
     * Adds a GP listener.
     * @param listener the GP listener to add.
     * @return true if the listener was added, or false if the listener was already
     *         present.
     * @see #removeListener(com.esri.vehiclecommander.analysis.GPListener)
     */
    public boolean addGPListener(GPListener listener) {
        synchronized (listeners) {
            return listeners.add(listener);
        }
    }

    protected void fireGPDisbled() {
        synchronized (listeners) {
            for (final GPListener listener : listeners) {
                new Thread() {
                    @Override
                    public void run() {
                        listener.gpDisbled();
                    }
                }.start();
            }
        }
    }

    protected void fireGPEnabled() {
        synchronized (listeners) {
            for (final GPListener listener : listeners) {
                new Thread() {
                    @Override
                    public void run() {
                        listener.gpEnabled();
                    }
                }.start();
            }
        }
    }

    protected void fireGPEnded(final ArcGISDynamicMapServiceLayer resultLayer) {
        synchronized (listeners) {
            for (final GPListener listener : listeners) {
                new Thread() {
                    @Override
                    public void run() {
                        listener.gpEnded(resultLayer);
                    }
                }.start();
            }
        }
    }

    protected void fireGPStarted() {
        synchronized (listeners) {
            for (final GPListener listener : listeners) {
                new Thread() {
                    @Override
                    public void run() {
                        listener.gpStarted();
                    }
                }.start();
            }
        }
    }

    /**
     * Gets the name of the GP task.
     * @return the name of the GP task.
     */
    public String getTaskName() {
        return taskName;
    }

    /**
     * Removes a GP listener.
     * @param listener the GP listener to remove.
     * @return true if the listener was removed, or false if the listener was not
     *         present.
     * @see #addListener(com.esri.vehiclecommander.analysis.GPListener)
     */
    public boolean removeListener(GPListener listener) {
        synchronized (listeners) {
            return listeners.remove(listener);
        }
    }

    /**
     * Sets the GP service path (URL or GPK), and, if GPK, starts the local
     * service.
     * @param path the URL to a GP service, or a GPK file path.
     */
    public void setServicePath(final String path) {
        fireGPDisbled();
        File gpk = new File(path);
        
        if (!gpk.exists()) {
            if (!path.contains("http")) {
                Logger.getLogger(GPController.class.getName()).log(Level.WARNING, "Warning - GPK does not exist: {0}", path);
                return;
            }
        }
        
        if (gpk.exists()) {
            if (LocalServiceStatus.STARTED.equals(gpService.getStatus())) {
                gpService.addLocalServiceStopCompleteListener(new LocalServiceStopCompleteListener() {
                    public void localServiceStopComplete(LocalServiceStopCompleteEvent e) {
                        gpService.removeLocalServiceStopCompleteListener(this);
                        setGPKPath(path);
                    }
                });
                gpService.stopAsync();
            } else {
                setGPKPath(path);
            }
        } else {
            try {
                geoprocessor = new Geoprocessor(path);
                fireGPEnabled();
            } catch (Throwable t) {
                Logger.getLogger(GPController.class.getName()).log(Level.WARNING, null, t);
            }
        }
    }

    /**
     * Sets the name of the GP task.
     * @param taskName the name of the GP task.
     */
    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    protected void setGPKPath(String gpkPath) {
        gpService.setPath(gpkPath);
        gpService.startAsync();
    }

    /**
     * @return the geoprocessor
     */
    public Geoprocessor getGeoprocessor() {
        return geoprocessor;
    }
    
}
