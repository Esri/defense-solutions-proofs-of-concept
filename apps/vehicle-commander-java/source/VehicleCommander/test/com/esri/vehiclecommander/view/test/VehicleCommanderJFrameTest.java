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
package com.esri.vehiclecommander.view.test;

import com.esri.runtime.ArcGISRuntime;
import com.esri.client.local.LocalServer;
import com.esri.core.runtime.LicenseResult;
import com.esri.vehiclecommander.view.VehicleCommanderJFrame;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

/**
 * VehicleCommanderJFrame unit tests.
 */
public class VehicleCommanderJFrameTest {

    public VehicleCommanderJFrameTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        LocalServer.getInstance().shutdown();
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test to make sure we're not publishing a valid license string with this source
     * code.
     */
    @Test
    public void testLicenseInvalid() {
        String fakeClientId = "abcdefghijklmnop";
        ArcGISRuntime.setClientID(fakeClientId);
        LicenseResult licenseResult = ArcGISRuntime.License.setLicense(VehicleCommanderJFrame.BUILT_IN_LICENSE_STRING);
        if (!LicenseResult.INVALID.equals(licenseResult)) {
            Assert.fail("Warning: your code contains a license string that is " + licenseResult + "! It should be " + LicenseResult.INVALID + " instead.");
        }
    }

}