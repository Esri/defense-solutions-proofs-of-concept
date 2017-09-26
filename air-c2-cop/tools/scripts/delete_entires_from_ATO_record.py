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
arcpy.MakeTableView_management("%s/AirC2_ATO_MISSION"  % (targetWS), "ATO_POINT_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("ATO_POINT_FILTER")

# Process: Make Table View
arcpy.MakeTableView_management("%s/AirC2_ATO_GENTEXT"  % (targetWS), "ATO_GENTEXT_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("ATO_GENTEXT_FILTER")

# Process: Make Table View
arcpy.MakeTableView_management("%s/AirC2_ATO_MISSION_BRIEFING"  % (targetWS), "ATO_BRIEFING_FILTER", "AMSID like '" + AMS_Id + "'", "", "")

# Process: Delete Rows
arcpy.DeleteRows_management("ATO_BRIEFING_FILTER")

