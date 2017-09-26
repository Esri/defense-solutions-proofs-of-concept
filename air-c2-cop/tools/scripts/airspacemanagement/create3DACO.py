# -*- coding: utf-8 -*-

###############################################################################
# Authors: Anthony Giles, Helyx SIS, Feb 2016
# 
###############################################################################

import arcpy

fc      = arcpy.GetParameterAsText(0)
elev    = arcpy.GetParameterAsText(1)
targetWS  = arcpy.GetParameterAsText(2)

extrusions = []

fields = ['OBJECTID','MIN_HEIGHT','MAX_HEIGHT','SHAPE@',"NAME"]

if arcpy.Exists('%s/AirC2_ACO_POLYGON_3D' % (targetWS)):
    arcpy.Delete_management('%s/ACO_POLYGON_3D' % (targetWS))      

with arcpy.da.SearchCursor(fc, fields) as cursor:
    for row in cursor:
    
        arcpy.AddMessage("Processing 3d Geometry for " + row[4])

        #need to create a feature class of a single feature to use within the extrude between process
        query = '"objectid" = {0}'.format(row[0])
        arcpy.Select_analysis(fc, r'in_memory\temp_feature', query)

        #need to project the geometry from Lat/Long to WMAS
        projected_extent = row[3].projectAs(arcpy.SpatialReference(102100))
        buffered_extent = projected_extent.buffer(500)
        extent = "{0} {1} {2} {3}".format(buffered_extent.extent.XMin, buffered_extent.extent.YMin, buffered_extent.extent.XMax, buffered_extent.extent.YMax)

        #clip original elevation to feature extent
        arcpy.Clip_management(elev, extent, "in_memory\clip", '#', '#', "NONE", "NO_MAINTAIN_EXTENT")

        #make sure the clipped elevation does not contain any NO_DATA
        arcpy.gp.RasterCalculator_sa("Con(IsNull('in_memory\clip'), 0, 'in_memory\clip')", "in_memory\clip_NoData")

        #add minimum height values to clipped elevation in metres not feet 
        raster_min = arcpy.sa.Plus(r"in_memory\clip_NoData",int(row[1]/3.2808399))

        #convert to tin to use in the extrude between process - tin cannot be created in_memory
        arcpy.ddd.RasterTin(raster_min , 'tin_min')
        
        #add maximum height values to clipped elevation in metres not feet
        raster_max = arcpy.sa.Plus("in_memory\clip_NoData",int(row[2]/3.2808399))

        #convert to tin to use in the extrude between process  - tin cannot be created in_memory
        arcpy.ddd.RasterTin(raster_max, 'tin_max')

        #ensure we have a unique name for the temp feature class
        outMP = arcpy.CreateUniqueName("extrusion", "in_memory")

        arcpy.ExtrudeBetween_3d('tin_min','tin_max', r'in_memory\temp_feature', outMP)

        #add feature class to array
        extrusions.append(outMP)

#merge all temp feature classes together into a single feature class        
arcpy.Merge_management(extrusions, '%s/AirC2_ACO_POLYGON_3D' % (targetWS))

#tidy up 
arcpy.Delete_management('tin_min')
arcpy.Delete_management('tin_max')

