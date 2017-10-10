#-------------------------------------------------------------------------------
# Name:        module1
# Purpose:
#
# Author:      patr5136
#
# Created:     28/08/2013
# Copyright:   (c) patr5136 2013
# Licence:     <your licence>
#-------------------------------------------------------------------------------

import arcpy, os, visibility

if __name__ == '__main__':
    observers= arcpy.GetParameterAsText(0)
    imgService=arcpy.GetParameterAsText(1)
    radius=arcpy.GetParameter(2)
    height=arcpy.GetParameter(3)
    mask=arcpy.GetParameterAsText(4)
    wkid=arcpy.GetParameter(5)
    #wkidin=arcpy.GetParameter(5)
    #wkidproc=arcpy.GetParameter(6)
    #wkidout=arcpy.GetParameter(7)
    #v = visibility.Viewshed(observers,imgService,radius,height,mask,wkidin,wkidproc,wkidout)
    v = visibility.Viewshed(observers,imgService,radius,height,mask,wkid)
    vshed = v.createViewshed()
    arcpy.SetParameter(6,vshed)
