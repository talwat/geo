package me.talwat.geo

import org.bukkit.plugin.java.JavaPlugin
import xyz.jpenilla.squaremap.api.Key
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class Geo : JavaPlugin() {
    override fun onEnable() {
        logger.info("Initializing...")

        // TODO: Handle possible exception.
        val image: BufferedImage = ImageIO.read(File(this.dataFolder.path, "borders.png"))
        val borderMap = BorderMap(image);

        val layerProvider = setupLayerProvider("Borders", "borders")!!

        logger.info("Tracing Shapes...")
        val shapes = borderMap.traceShapes();
        logger.info(shapes.entries.count().toString())

        for ((i, entry) in shapes.entries.withIndex()) {
            val color = entry.key;
            val polygon = areaToPolygon(borderMap, entry.value, logger);
            polygon.markerOptions(
                polygon.markerOptions()
                    .asBuilder()
                    .fillColor(color)
                    .strokeColor(color.brighter())
                    .clickTooltip("test")
            )

            layerProvider.addMarker(Key.key(String.format("borders_%s", i)), polygon)
        }

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
