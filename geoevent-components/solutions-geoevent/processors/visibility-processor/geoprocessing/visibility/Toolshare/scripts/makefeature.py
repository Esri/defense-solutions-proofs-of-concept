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
import os, json

def makeFeature(geo, wkid):
    sr = arcpy.SpatialReference(wkid);
    arcpy.CreateFeatureclass_management('in_memory', 'tmpPoly', POLYGON,'#','#','#',sr)
    fc = os.path.join('in_memory', 'tmpPoly')
    fields = ["SHAPE@"]
    insert = arcpy.da.InsertCursor(fc, fields)
    insert.insertRow(geo);
    return fc

def makePolygon(json):

    jsonPoly = json.loads(json)
    rings=arcpy.Array()
    for ring in jsonPoly['rings']:
        points = arcpy.Array();
        for coord in ring:
            x=coord[0]
            y=coord[1]
            z=None
            if len(coord)>2:
                z=coord[2]
            #z=coord[3]
            p=arcpy.Point()
            p.X=x
            p.Y=y
            if z:
                p.Z=z
            points.add(p)
        rings.add(points)
    return arcpy.Polygon(rings)


if __name__ == '__main__':
    jsonPolygon = arcpy.GetParameterAsTextsText(0)
    wkid = arcpy.GetParameter(1)
    polygon = makePolygon(jsonPolygon)
    fc = makeFeature(polygon, wkid)
    arcpy.SetParameter(2, fc)
