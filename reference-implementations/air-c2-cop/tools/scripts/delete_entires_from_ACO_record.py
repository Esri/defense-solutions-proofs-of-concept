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

# Import arcpy module
import arcpy

# Script arguments
targetWS = arcpy.GetParameterAsText(0)

AMS_Id = arcpy.GetParameterAsText(1)

# Process: Make Table View
arcpy.MakeTableView_management("%s/AirC2_AMS_RECORD" % (targetWS), "AMS_RECORD_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("AMS_RECORD_FILTER")

# Process: Make Table View
arcpy.MakeTableView_management("%s/AirC2_ACO_POLYGON" % (targetWS), "AMS_POLYGON_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("AMS_POLYGON_FILTER")

# Process: Make Table View
arcpy.MakeTableView_management("%s/AirC2_ACO_PERIOD" % (targetWS), "AMS_ORDER_PERIOD_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("AMS_ORDER_PERIOD_FILTER")

# Process: Make Table View
arcpy.MakeTableView_management("%s/AirC2_ACO_LINE" % (targetWS), "AMS_ORDER_LINE_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("AMS_ORDER_LINE_FILTER")

# Process: Make Table View
arcpy.MakeTableView_management("%s/AirC2_ACO_POINT" % (targetWS), "ACO_POINT_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("ACO_POINT_FILTER")

