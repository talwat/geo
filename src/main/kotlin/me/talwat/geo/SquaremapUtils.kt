package me.talwat.geo

import org.bukkit.Bukkit
import xyz.jpenilla.squaremap.api.*
import xyz.jpenilla.squaremap.api.marker.Polygon
import java.awt.geom.Area
import java.awt.geom.PathIterator
import java.awt.geom.Point2D
import java.util.logging.Logger

fun setupLayerProvider(label: String, id: String): SimpleLayerProvider? {
    val api = SquaremapProvider.get()
    val world = Bukkit.getWorld("world") ?: return null

    val identifier = BukkitAdapter.worldIdentifier(world)
    val mapWorld = api.getWorldIfEnabled(identifier).orElse(null) ?: return null

    val key = Key.key(id)

    if (mapWorld.layerRegistry().hasEntry(key)) {
        mapWorld.layerRegistry().unregister(key)
    }

    val layerProvider = SimpleLayerProvider.builder(label)
        .showControls(true)
        .defaultHidden(false)
        .layerPriority(5)
        .zIndex(250)
        .build()

    mapWorld.layerRegistry().register(key, layerProvider)

    layerProvider.clearMarkers()

    return layerProvider
}

fun areaToPolygon(map: BorderMap, area: Area): Polygon {
    val points: MutableList<Point> = ArrayList()
    val iterator: PathIterator = area.getPathIterator(null, 0.5)

    val coords = DoubleArray(6)
    while (!iterator.isDone) {
        val type = iterator.currentSegment(coords)
        if (type != PathIterator.SEG_CLOSE) {
            points.add(map.imageToMap(Point2D.Double(coords[0], coords[1])))
        }
        iterator.next()
    }

    return Polygon.polygon(points)
}