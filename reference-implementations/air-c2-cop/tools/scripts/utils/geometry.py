###############################################################################
# Author: Shaun Nicholson, Esri UK, May 2015
#
# (C) Copyright ESRI (UK) Limited 2011. All rights reserved
# ESRI (UK) Ltd, Millennium House, 65 Walton Street, Aylesbury, HP21 7QG
# Tel: +44 (0) 1296 745500  Fax: +44 (0) 1296 745544
###############################################################################

# Import required modules
import sys

class Point:
    _x = 0
    _y = 0

    def __init__(self, x, y):
        self._x = x
        self._y = y
        return

    def getX(self):
        return self._x

    def getY(self):
        return self._y

class Centroid:

    _minX = None
    _minY = None
    _maxX = None
    _maxY = None

    def __init__(self):
        return

    def addPoint(self, x, y):
        if self._minX == None and self._minY == None and self._maxX == None and self._maxY == None:
            self._minX = x
            self._minY = y
            self._maxX = x
            self._maxY = y
        else:
            if x < self._minX: self._minX = x
            if x > self._maxX: self._maxX = x
            if y < self._minY: self._minY = y
            if y > self._maxY: self._maxY = y

    def updateExtent(self, minX, minY, maxX, maxY):
        if self._minX == None and self._minY == None and self._maxX == None and self._maxY == None:
            self._minX = minX
            self._minY = minY
            self._maxX = maxX
            self._maxY = maxY
        else:
            if minX < self._minX: self._minX = minX
            if maxX > self._maxX: self._maxX = maxX
            if minY < self._minY: self._minY = minY
            if maxY > self._maxY: self._maxY = maxY

    def getCentroid(self):
        cenX = (self._minX + self._maxX) / 2
        cenY = (self._minY + self._maxY) / 2
        return Point(cenX, cenY)