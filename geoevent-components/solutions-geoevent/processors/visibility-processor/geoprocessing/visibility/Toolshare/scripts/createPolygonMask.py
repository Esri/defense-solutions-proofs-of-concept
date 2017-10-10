#-------------------------------------------------------------------------------
# Name:        module2
# Purpose:
#
# Author:      patr5136
#
# Created:     23/08/2013
# Copyright:   (c) patr5136 2013
# Licence:     <your licence>
#-------------------------------------------------------------------------------
import arcpy
import os

def makeFeature(geo, wkid):
    sr = arcpy.SpatialReference(wkid);
    arcpy.CreateFeatureclass_management('in_memory', 'tmpPoly', POLYGON,'#','#','#',sr)
    fc = os.path.join('in_memory', 'tmpPoly')
    fields = ["SHAPE@"]
    insert = arcpy.da.InsertCursor(fc, fields)
    insert.insertRow(polygon);
    return fc

if __name__ == '__main__':
    polygon = arcpy.GetParameter(0)
    wkid = arcpy.GetParameter(1)
    fc = makeFeature(polygon, wkid)
    arcpy.SetParameter(2, fc)
