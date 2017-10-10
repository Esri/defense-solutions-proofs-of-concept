import arcpy, sys, os
from arcpy import env
import Shared.ErrorHandling as ErrorHandling
import Shared.Logger as logger

class CoordsToFeatureClass:
    def __init__(self):
        parent = os.path.dirname((os.path.dirname(__file__)))
        sw = os.path.join(parent, 'scratch')
        ws = os.path.join(parent, 'data')
        arcpy.AddMessage(sw)
        arcpy.AddMessage(parent)
        if not env.scratchWorkspace:
            env.scratchWorkspace = sw
        if not env.workspace:
            env.workspace = ws
        env.overwriteOutput = True
        self.scratch = str(env.scratchWorkspace)
    def CreateFC(self, points, sr, name, z=None):
        try:
            #'-34.04 68.5,-34.05'
            coordpairs = points.split(';')
            pointGeometryList = []

            coords = points.split(' ')
            for coordpair in coordpairs:
                pt = arcpy.Point()
                coords = coordpair.split(' ')
                pt.X = coords[0]
                pt.Y = coords[1]
                if z:
                    pt.Z = z
                pointGeometry = arcpy.PointGeometry(pt, sr)
                pointGeometryList.append(pointGeometry)


            path = self.scratch + os.sep + 'scratch.gdb' + os.sep + name
            arcpy.AddMessage('path to sourcept: ' + path)
            arcpy.AddMessage(path)
            arcpy.CopyFeatures_management(pointGeometryList, path)
            fset = arcpy.FeatureSet()
            fset.load(path)
            self.pt = fset
            return fset

        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)

    def CreateFCFromGeoList(self, points, sr, name):
        try:
            path = self.scratch + os.sep + 'scratch.gdb' + os.sep + name
            arcpy.AddMessage('path to sourcept: ' + path)
            arcpy.AddMessage(path)
            arcpy.CopyFeatures_management(points, path)
            #fset = arcpy.FeatureSet()
            #fset.load(path)
            #self.pt = fset
            return path
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)
