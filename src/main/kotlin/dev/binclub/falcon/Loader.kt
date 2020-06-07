package dev.binclub.falcon

import dev.binclub.falcon.antidump.CookieFuckery
import net.minecraft.launchwrapper.Launch
import net.minecraft.launchwrapper.LaunchClassLoader
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

/**
 * @author cookiedragon234 31/Mar/2020
 */
private const val clientUrl = "https://yourwebsite/client.jar"

fun load() {
	// NOTE: These WILL break when debug tools such as IntelliJ are attached to the process
	// THIS IS PURPOSEFUL
	CookieFuckery.checkLaunchFlags()
	CookieFuckery.disableJavaAgents()
	CookieFuckery.setPackageNameFilter()
	CookieFuckery.dissasembleStructs()
	
	println("Falcon is downloading classes...")
	
	val resourceCache = LaunchClassLoader::class.java.getDeclaredField("resourceCache").let {
		it.isAccessible = true
		it[Launch.classLoader] as MutableMap<String, ByteArray>
	}
	
	val stream = URL(clientUrl).openConnection().also {
		it.addRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.0)")
	}.getInputStream()
	
	ZipInputStream(stream).use { zipStream ->
		var zipEntry: ZipEntry?
		while (zipStream.nextEntry.also { zipEntry = it } != null) {
			var name = zipEntry!!.name
			if (name.endsWith(".class")) {
				name = name.removeSuffix(".class")
				name = name.replace('/', '.')
				
				resourceCache[name] = zipStream.readBytes()
			}
		}
	}
}
