package dev.binclub.falcon

import com.myclient.MyClientMod
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent

/**
 * @author cookiedragon234 31/Mar/2020
 */
@Mod(
	modid = FalconMod.MOD_ID,
	name = FalconMod.MOD_NAME,
	version = FalconMod.VERSION
)
class FalconMod {
	companion object {
		const val MOD_ID = "falcon"
		const val MOD_NAME = "Falcon"
		const val VERSION = "0.1"
	}
	
	val clientMod: MyClientMod by lazy {
		MyClientMod()
	}
	
	@Mod.EventHandler
	fun preinit(event: FMLPreInitializationEvent) {
		clientMod.preinit(event)
	}
	
	@Mod.EventHandler
	fun init(event: FMLInitializationEvent) {
		clientMod.init(event)
	}
	
	@Mod.EventHandler
	fun postinit(event: FMLPostInitializationEvent) {
		clientMod.postinit(event)
	}
}
