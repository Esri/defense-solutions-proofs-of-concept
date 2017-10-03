## Copyright 2016 Esri
## Licensed under the Apache License, Version 2.0 (the "License");
## you may not use this file except in compliance with the License.
## You may obtain a copy of the License at
## http://www.apache.org/licenses/LICENSE-2.0
## Unless required by applicable law or agreed to in writing, software
## distributed under the License is distributed on an "AS IS" BASIS,
## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
## See the License for the specific language governing permissions and
## limitations under the License.​

# Name: WindDirectionFromUV

# Description: Python raster function to extract wind direction from u and v components of wind.  It is assumed that the
# wind direction being fed into this function is the normal meterological convention of the direction the wind is coming
# from and not the direction it is blowing.

# Date Edited: 02/06/2016

#----------------------------------------------------------------------------------------------------------------------

import numpy as np



class WindDirectionFromUV():

    def __init__(self):
        self.name = "Wind Direction Function"
        self.description = "This function computes Wind Direction given u/v components of wind."


    def getParameterInfo(self):
        return [
            {
                'name': 'u',
                'dataType': 'raster',
                'value': None,
                'required': True,
                'displayName': "U component of wind Raster",
                'description': "A single-band raster where pixel values represent the u component of wind in metres per second."
            },
            {
                'name': 'v',
                'dataType': 'raster',
                'value': None,
                'required': True,
                'displayName': "V component of wind Raster",
                'description': "A single-band raster where pixel values represent the v component of wind in metres per second."
            },
        ]


    def getConfiguration(self, **scalars):
        return {
          'inheritProperties': 4 | 8,               # inherit all but the pixel type and NoData from the input raster
          'invalidateProperties': 2 | 4 | 8,        # invalidate statistics & histogram on the parent dataset because we modify pixel values.
          'inputMask': False                        # Don't need input raster mask in .updatePixels(). Simply use the inherited NoData.
        }


    def updateRasterInfo(self, **kwargs):
        kwargs['output_info']['bandCount'] = 1      # output is a single band raster
        kwargs['output_info']['statistics'] = ({'minimum': -91, 'maximum': -89}, )
        kwargs['output_info']['histogram'] = ()     # we know nothing about the histogram of the outgoing raster.
        kwargs['output_info']['pixelType'] = 'f4'
        return kwargs


    def updatePixels(self, tlc, size, props, **pixelBlocks):
        u = np.array(pixelBlocks['u_pixels'], dtype='f4')[0]       
        v = np.array(pixelBlocks['v_pixels'], dtype='f4')[0]

        #Getting the UV components of wind into the correct orientation:
        wind_abs = np.sqrt(u**2 + v**2)
        wind_dir_trig_to_degrees = np.arctan2(u/wind_abs, v/wind_abs)
        
        #Then you must convert this wind vector to the meteorological convention of the direction the wind is coming from:
        #wind_dir_trig_to_degrees = wind_dir_trig_to * 180/pi
        
        #Then you must convert that angle from "trig" coordinates to cardinal coordinates:
        wind_dir_trig_from_degrees = wind_dir_trig_to_degrees + 180
    
        outblock = 90 - wind_dir_trig_from_degrees
        
        ##      outBlock = np.degrees(np.arctan2(u, v)) + 180
        pixelBlocks['output_pixels'] = outblock.astype(props['pixelType'])
        return pixelBlocks


    def updateKeyMetadata(self, names, bandIndex, **keyMetadata):
        if bandIndex == -1:
            keyMetadata['datatype'] = 'Scientific'
            keyMetadata['datatype'] = 'WindSpeed'
        elif bandIndex == 0:
            keyMetadata['wavelengthmin'] = None     # reset inapplicable band-specific key metadata
            keyMetadata['wavelengthmax'] = None
            keyMetadata['bandname'] = 'WindSpeed'
        return keyMetadata
