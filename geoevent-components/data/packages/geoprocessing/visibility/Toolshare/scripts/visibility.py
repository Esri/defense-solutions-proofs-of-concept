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

import arcpy, os, math, json
from arcpy import env
from arcpy import sa
from arcpy.sa import *
import arcpy.mapping as map
import ErrorHandling as ErrorHandling

class Viewshed:

    #def __init__(self, observers, imageService, radius, height, mask, wkidin, wkidproc, wkidout):
    def __init__(self, observers, imageService, radius, height, mask, wkid):
        if arcpy.CheckExtension("Spatial") == "Available":
            arcpy.CheckOutExtension("Spatial")
        else:
            arcpy.AddMessage('License error')
        parent = os.path.dirname((os.path.dirname(__file__)))
        sw = os.path.join(parent, 'scratch')
        ws = os.path.join(parent, 'data', 'data.gdb')
        arcpy.AddMessage(sw)
        arcpy.AddMessage(parent)
        if not env.scratchWorkspace:
            env.scratchWorkspace = sw
        if not env.workspace:
            env.workspace = ws
        self.workspace = str(env.workspace)
        env.overwriteOutput = True
        self.scratch = str(env.scratchWorkspace)
        self.scratchgdb = env.scratchGDB
        self.service = imageService
        self.height = height
        self.mask = mask
        self.sref = arcpy.SpatialReference(wkid)
        self.radius=radius
        #self.wkidin = wkidin
        #self.wkidout=wkidout
        #self.wkidproc= wkidproc
        #self.srIn = arcpy.SpatialReference(wkidin)
        #self.srProc = arcpy.SpatialReference(wkidproc)
        #self.srOut = arcpy.SpatialReference(wkidout)
        self.obsproc=self.__makeObserver__(observers, 'obs')
        self.buffer = self.__makeBuffers__(radius)
        self.cellsize = self.__CalculateCellSize__(self.buffer)
        self.islyr = self.__CreateISLayer__(imageService)
        #self.islyr = imageService

        #observersz = self.__appendZs__(self.obsproc)
        #self.obsz = self.__makeObserver__(observersz, 'obsz', self.wkidproc)
        self.mask = self.__CreateMask__(mask)

    def __makeObserver__(self, observers, name, wkid = None):
        try:
            arcpy.AddMessage("Creating observer...")
##            curwkid = None
##            if wkid:
##                curwkid = wkid
##                sref = arcpy.SpatialReference(wkid)
##            else:
##                curwkid = self.wkidin
##                sref = self.srIn
##            arcpy.SpatialReference(self.wkidin)
            obs = self.__createFC__(observers, self.sref, name)
            arcpy.AddMessage("observation fc: " + arcpy.Describe(obs).name)
            #obsproj = os.path.join(self.scratchgdb, name+'_proj')
            obsout = os.path.join(self.scratchgdb, name+'out')
            #obsout = os.path.join(r"C:\GEE\visibility\visibility.gdb", 'obsot2')
            obs.save(obsout);

##            if(curwkid != self.wkidproc):
##                arcpy.AddMessage("Projecting observers...")
##                arcpy.AddMessage("projected observation fc: " + obsproj)
##                arcpy.Project_management(obs,obsproj,self.srProc)
##                obsout = obsproj
##            else:
##                obsout=obs
            h=self.height
            #arcpy.AddField_management(obsout, "OFFSETA", "DOUBLE", "", "", "", "", "NULLABLE", "NON_REQUIRED", "")
            #arcpy.CalculateField_management(obsout, "OFFSETA", h, "PYTHON", "")
            return obsout
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)

    def __makeBuffers__(self, radius):
        try:
            arcpy.AddMessage("Creating buffer...")
            bufferfc = os.path.join("in_memory", "buffers")
            arcpy.AddMessage("buffer fc: " + bufferfc)
            arcpy.Buffer_analysis(self.obsproc, bufferfc, radius, "FULL", "ROUND", "ALL")
            return bufferfc
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)

    def __CalculateCellSize__(self, ds):
        try:
            arcpy.AddMessage("Calculating cellsize...")
            width = arcpy.Describe(ds).extent.width
            height = arcpy.Describe(ds).extent.height
            #return max(float(max(width,height))/2000.0,30.0)
            return max(float(max(width,height))/250.0,10.0)
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)

    def __CreateISLayer__(self, service):
        try:
            arcpy.AddMessage("Creating image service layer...")
            outislyr=os.path.join("in_memory",'ras_dsm')
            #outislyr2 = os.path.join(r"C:\GEE\visibility", "outislyr2")

            arcpy.AddMessage("image service layer: " + outislyr)
            arcpy.MakeImageServerLayer_management(service, outislyr, self.buffer, "", "CLOSEST_TO_CENTER", "", "", "")
            #filteredraster = sa.FocalStatistics(outislyr, "", "MEAN", "")
            #newraster = Raster(outislyr)
            #newraster.save(outislyr2)
            #return filteredraster
            return outislyr
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)

    def __CreateMask__(self, jsonGeo):
        try:
            arcpy.AddMessage("Creating mask...")
            jsonPoly = json.loads(jsonGeo)
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
            wkid = jsonPoly['spatialReference']['wkid']
            polySrIn = arcpy.SpatialReference(wkid)
            polygon=arcpy.Polygon(rings,polySrIn)
            features = []
            masktmp = os.path.join("in_memory", 'masktmp')
            arcpy.AddMessage("mask fc: " + masktmp)
            #mask_proj = os.path.join(self.scratchgdb, 'maskproj')

            features.append(polygon)
            arcpy.CopyFeatures_management(features, masktmp)
##            if(wkid != self.wkidproc):
##                arcpy.AddMessage("Projecting mask...")
##                arcpy.AddMessage("projected mask fc: " + mask_proj)
##                arcpy.Project_management(masktmp, mask_proj, self.srProc)
##                mask = mask_proj
##            else:
##                mask = masktmp
            return masktmp
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)

    def createViewshed(self):
        try:
            tempEnvironment0 = arcpy.env.extent
            arcpy.env.extent = self.buffer
            tempEnvironment1 = arcpy.env.cellSize
            arcpy.env.cellSize = self.cellsize
            arcpy.AddMessage("cellsize: " + str(arcpy.env.cellSize))
            tempEnvironment2 = arcpy.env.mask
            arcpy.env.mask = self.buffer
            #outraster = sa.Viewshed(self.islyr, self.obsproc, 1, "FLAT_EARTH", 0.13)
            outraster = sa.Visibility(self.islyr, self.obsproc, analysis_type="FREQUENCY", nonvisible_cell_value="ZERO", z_factor=1, curvature_correction="CURVED_EARTH",refractivity_coefficient=0.13, observer_offset=self.height, outer_radius=self.radius, vertical_upper_angle=90, vertical_lower_angle=-90)
            #outrastertemp = os.path.join(r"C:\GEE\visibility", 'outvis')
            #outraster.save(outrastertemp)
            vshedtmp = os.path.join("in_memory", 'vshedtmp')
            vsheddis = os.path.join("in_memory", 'vsheddis')
            #vshed_proj = os.path.join(self.scratchgdb, 'vshedproj')
            arcpy.AddMessage("temp vshed fc:" + vshedtmp)
            arcpy.AddMessage("dissolved vshed fc: " + vsheddis)
            arcpy.env.extent = tempEnvironment0
            arcpy.env.cellSize = tempEnvironment1
            arcpy.env.mask = tempEnvironment2
            arcpy.RasterToPolygon_conversion(outraster, vshedtmp, "NO_SIMPLIFY", "VALUE")
            arcpy.Dissolve_management(vshedtmp, vsheddis, "gridcode", "", "MULTI_PART", "DISSOLVE_LINES")

##            if(self.wkidproc != self.wkidout):
##                arcpy.AddMessage("Projecting output vshed...")
##                arcpy.AddMessage("projected vshed fc: " + vshed_proj)
##                arcpy.Project_management(vsheddis, vshed_proj, self.srOut)
##                vshed=vshed_proj
##            else:
##                vshed=vsheddis
            #vistmp = os.path.join('in_memory', 'visibility')
            vis = os.path.join(self.scratchgdb, 'visibility')
            arcpy.AddMessage('creating output viewshed: ' + vis)
            arcpy.Clip_analysis(vsheddis, self.mask, vis, "")
            arcpy.AddMessage("Coppying to output...")
            #arcpy.CopyFeatures_management(vistmp, vis)
            fset = arcpy.FeatureSet()
            fset.load(vis)
            return fset
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)

    def __createFC__(self, points, sr, name):
        try:
            #'-34.04 68.5,-34.05'
            coordpairs = points.split(';')
            pointGeometryList = []
            has_z = False
            for coordpair in coordpairs:
                pt = arcpy.Point()
                coords = coordpair.split(' ')
                arcpy.AddMessage(coords)
                pt.X = float(coords[0])
                pt.Y = float(coords[1])

                if len(coords) > 2:
                    has_z = True
                    arcpy.AddMessage('adding z...')
                    pt.z = float(coords[2])
                pointGeometry = arcpy.PointGeometry(pt, sr, has_z)

                pointGeometryList.append(pointGeometry)
            #path = self.scratch + os.sep + 'scratch.gdb' + os.sep + name
            path=os.path.join("in_memory",name)
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

    def __appendZs__(self, points):
        try:

            newcoords = ""
            coordpairs = points.split(';')
            pointGeometryList = []
            fields=["SHAPE@X", "SHAPE@Y"]
            rows = arcpy.da.SearchCursor(points, fields)
            for row in rows:
                coordpair = str(row[0]) + ' ' + str(row[1])
                arcpy.AddMessage('coords: ' + coordpair)
                result = arcpy.GetCellValue_management(self.islyr, coordpair)
                e = result.getOutput(0)
                if e != 'NoData':
                    v = float(e) + self.height
                else:
                    v = self.height
                if len(newcoords)>0:
                    newcoords += ";" + coordpair + " " + str(v)
                else:
                    newcoords +=  coordpair + " " + str(v)
            del rows
            del row
            arcpy.AddMessage('newcoords: ' + newcoords)
            return newcoords
        except arcpy.ExecuteError:
            EH = ErrorHandling.ErrorHandling()
            line, filename, err = EH.trace()
            m = "Python error on " + line + " of " + __file__ + \
                " : with error - " + err
            arcpy.AddError(m)






