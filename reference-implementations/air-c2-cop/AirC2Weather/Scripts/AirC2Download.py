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

##Name: Download NOAA data
##
##Description: Downloads the most up to date data from the NOAA site by getting the present date.
##
##                Script works as follow;
##                    Gets the present time date in UTC
##                    Uses the OPeNDAP to NetCDF tool from Multidimension Supplemental Tool
##                    Downloads the specified variables into NetCDF format files and saves them in the relative location, based on where the script file is located.
##                    The present data is removed from the Mosaic Dataset
##                    The new data is then loaded into the Mosiac Dataset
##                    The wind data is then combined into a Multiband raster.
##
##Date Edited: 25/08/2016


#Import modules
import arcpy
import os
import datetime
from arcpy import env
from datetime import datetime, timedelta
from datetime import time

arcpy.env.overwriteOutput = True

start = str(datetime.utcnow())
print "The script started at" + " " + start

#Gets the current directory where the script is sitting so that everything else can work off relative paths.
folder = os.path.dirname(__file__)
topFolder = os.path.dirname(folder)
        
#Names of folders to be added to topFolder generated above
gdb = "Geodatabase"
NetCDFData = "NetCDFdata"
tls = "Tools"

#Declaration of variables used later
variable = "rh2m;tcdcclm;tmpsfc;hgtclb;vissfc;ugrd10m;vgrd10m;ugrdmwl;vgrdmwl;snodsfc;gustsfc;apcpsfc"
extent = "-126 30 -109 45"
dimension = "time '2016-01-01 00:00:00' '2016-12-31 00:00:00'"
env.workspace = topFolder + os.sep + gdb + "\OperationalWeather.gdb"

#_____________________________________________________________________________________________________________________________________________

def download(paramFN,paramDL):
    #Import custom toolbox required
    arcpy.ImportToolbox(topFolder + os.sep + tls + "\MultidimensionSupplementalTools\Multidimension Supplemental Tools.pyt")

    print ("Toolbox imported")

    #Get present date and time
    patternDate = '%Y%m%d'
    patternTime = '%H:%M:%S'

    utcdate = datetime.utcnow()
    
##    stringDateNow = datetime.utcnow().strftime(patternDate)
##    stringTimeNow = datetime.utcnow().strftime(patternTime)

    stringDateNow = utcdate.strftime(patternDate)
    stringTimeNow = utcdate.strftime(patternTime)

    print ("datetime returned")

    #Insert present date into string for out_file
    stringToChange =  topFolder + os.sep + NetCDFData + r"\nam%s" + paramFN + ".nc"
    stringToChange4 = r"http://nomads.ncep.noaa.gov/dods/nam/nam%s/nam" + paramDL
     
    stringToInsert = stringDateNow
    
    stringFinal = stringToChange % stringToInsert
    stringFinal4 = stringToChange4 % stringToInsert
    filename = "nam%s1hr00z.nc" % stringToInsert

    #------------------------------------------------------------------------------------------------------------------------------------------

    #Declare variables to be added into OPeNDAP to NetCDF tool for general data
    in_url = stringFinal4
    out_file = stringFinal

    #Run OPeNDAP to NetCDF tool
    arcpy.OPeNDAPtoNetCDF_mds( in_url, variable, out_file, extent, dimension, "BY_VALUE")

    #-------------------------------------------------------------------------------------------------------------------------------------------

    finishDownload = str(datetime.utcnow())
    print "OPeNDAp Tool run and data download finished at" + " " + finishDownload
    print out_file
    #____________________________________________________________________________________________________________________________________________

    #Data loading into Mosaic datasets.

    Input_Data = out_file

    output = topFolder + os.sep + gdb + "\OperationalWeather.gdb\\OperationalData"
    output2 = topFolder + os.sep + gdb + "\OperationalWeather.gdb\\OperationalWind"

    Raster_Type = "NetCDF"
    Raster_Type2 = topFolder + os.sep + NetCDFData + os.sep + "NETCDF_type.art.xml"
    print Raster_Type2

    
    #Check if the geodatabases stated above exist
    if arcpy.Exists(output):
        print output + " " + "exists"
    else:
        print output + " " + "does not exist"

    if arcpy.Exists(output2):
        print output2 + " " + "exists"
    else:
        print output2 + " " + "does not exist"

    # Process: Remove Rasters From Mosaic Dataset
    arcpy.RemoveRastersFromMosaicDataset_management(output, "OBJECTID >=0", "NO_BOUNDARY", "NO_MARK_OVERVIEW_ITEMS", "NO_DELETE_OVERVIEW_IMAGES", "NO_DELETE_ITEM_CACHE", "REMOVE_MOSAICDATASET_ITEMS", "NO_CELL_SIZES")
    arcpy.RemoveRastersFromMosaicDataset_management(output2, "OBJECTID >=0", "NO_BOUNDARY", "NO_MARK_OVERVIEW_ITEMS", "NO_DELETE_OVERVIEW_IMAGES", "NO_DELETE_ITEM_CACHE", "REMOVE_MOSAICDATASET_ITEMS", "NO_CELL_SIZES")
    
    # Process: Add Rasters To Mosaic Dataset
    arcpy.AddRastersToMosaicDataset_management(output, Raster_Type, Input_Data, "UPDATE_CELL_SIZES", "UPDATE_BOUNDARY", "NO_OVERVIEWS", "", "0", "1500", "", "*.nc", "SUBFOLDERS", "ALLOW_DUPLICATES", "NO_PYRAMIDS", "NO_STATISTICS", "NO_THUMBNAILS", "", "NO_FORCE_SPATIAL_REFERENCE")

    print ("Rasters added to" + " " + output)

    arcpy.AddRastersToMosaicDataset_management(output2,Raster_Type2 ,Input_Data)

    print ("Rasters added to" + " " + output2)
    
    finishDataLoad = str(datetime.utcnow())
    print "Data loading finished at" + " " + finishDataLoad

    #_____________________________________________________________________________________________________________________________________________

    finishWindDataProcessing = str(datetime.utcnow())
    print "Wind data processing finished at" + " " + finishWindDataProcessing
    print " "
    print " "
    print "The script finished at" + " " + finishWindDataProcessing
#_________________________________________________________________________________________________________________________________________________

# get the present time in utc.
# set times are set around the times that the data is released by NOAA


now_time = time(int(datetime.utcnow().strftime("%H")), int(datetime.utcnow().strftime("%M")), int(datetime.utcnow().strftime("%S")))

if now_time >= time(02,50,00) and now_time < time(8,50,00):
    download("1hr00z", "1hr_00z")
    
elif now_time >= time(8,50,00) and now_time < time(14,50,00):
    download("1hr06z", "1hr_06z")
 
elif now_time >= time(14,50,00) and now_time < time(21,00,00):
    download("1hr12z", "1hr_12z")

elif ((now_time >= time(21,00,00) and now_time <= time(23,59,59)) or (now_time >= time(00,00,00) and now_time <= time(02,49,59))):
    download("1hr18z", "1hr_18z")

