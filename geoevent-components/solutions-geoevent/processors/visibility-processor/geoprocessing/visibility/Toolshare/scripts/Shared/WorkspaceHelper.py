import arcpy, sys, os
from arcpy import env
import Shared.ErrorHandling as ErrorHandling
import Shared.Logger as logger

class WorkspaceHelper():
    

    def fixFeatureClassPath(ws, fcname):
        newname=None
        desc = arcpy.Describe(ws)
        wsType = desc.workspaceFactoryProgId
        if wsTytpe == "":
            # append .shp
            newname = fcname + '.shp'
        return newname
